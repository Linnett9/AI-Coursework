import sys
import pandas as pd
from sklearn.model_selection import cross_val_score, KFold
from sklearn.preprocessing import StandardScaler, OneHotEncoder, OrdinalEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import TargetEncoder
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier, HistGradientBoostingClassifier
from sklearn.neural_network import MLPClassifier
import matplotlib.pyplot as plt
import numpy as np
from sklearn.model_selection import learning_curve


# Define your best hyperparameters found from Optuna for each model
model_params = {
    'LogisticRegression': {'C': 4.090020690841885, 'max_iter': 381, 'solver': 'newton-cg', 'multi_class': 'ovr'},
    'RandomForestClassifier': {'n_estimators': 247, 'max_depth': 29, 'min_samples_split': 4, 'min_samples_leaf': 1},
    'GradientBoostingClassifier': {'n_estimators': 252, 'learning_rate': 0.10284802432225608, 'max_depth': 10, 'min_samples_split': 6, 'min_samples_leaf': 1},
    'HistGradientBoostingClassifier': {'max_iter': 157, 'learning_rate': 0.14684647372707207, 'max_depth': 10, 'min_samples_leaf': 12},
    'MLPClassifier': {'hidden_layer_sizes': (50, 50), 'activation': 'relu', 'solver': 'adam', 'alpha': 0.01823719670834927, 'learning_rate_init': 0.00040698321428077675, 'max_iter': 246}
}


# Define a model selection function
def select_model(model_name, model_params):
    models = {
        'LogisticRegression': LogisticRegression(**model_params['LogisticRegression']),
        'RandomForestClassifier': RandomForestClassifier(**model_params['RandomForestClassifier']),
        'GradientBoostingClassifier': GradientBoostingClassifier(**model_params['GradientBoostingClassifier']),
        'HistGradientBoostingClassifier': HistGradientBoostingClassifier(**model_params['HistGradientBoostingClassifier']),
        'MLPClassifier': MLPClassifier(**model_params['MLPClassifier'])
    }
    return models.get(model_name, None)

# Define a preprocessing function
def build_preprocessor(num_preprocessing, cat_preprocessing, num_features, cat_features):
    numeric_transformer = Pipeline(steps=[
        ('imputer', SimpleImputer(strategy='median')),
        ('scaler', StandardScaler())]) if num_preprocessing == 'StandardScaler' else 'passthrough'

    if cat_preprocessing == 'OneHotEncoder':
        categorical_transformer = OneHotEncoder(handle_unknown='ignore')
    elif cat_preprocessing == 'OrdinalEncoder':
        categorical_transformer = OrdinalEncoder()
    elif cat_preprocessing == 'TargetEncoder':
        categorical_transformer = TargetEncoder()
    else:
        categorical_transformer = 'passthrough'

    preprocessor = ColumnTransformer(
        transformers=[
            ('num', numeric_transformer, num_features),
            ('cat', categorical_transformer, cat_features)],
        remainder='passthrough')

    return preprocessor

# Parse command line arguments
train_input, train_labels, test_input, num_preprocessing, cat_preprocessing, model_type, prediction_output = sys.argv[1:]

# Load and prepare data
data = pd.read_csv(train_input)  
y_train = data['status_group']  
X_train = data.drop('status_group', axis=1)  

# Ensure y_train is loaded correctly
train_labels_data = pd.read_csv(train_labels)
if 'status_group' in train_labels_data:
    y_train = train_labels_data['status_group']  
else:
    print("status_group column not found in train_labels data.")
    sys.exit(1)  

print("Length of X_train:", len(X_train))
print("Length of y_train:", len(y_train))

X_test = pd.read_csv(test_input)

# Identify feature types
num_features = X_train.select_dtypes(include=['int64', 'float64']).columns
cat_features = X_train.select_dtypes(include=['object', 'bool']).columns

# Preprocess data
preprocessor = build_preprocessor(num_preprocessing, cat_preprocessing, num_features, cat_features)
model = select_model(model_type, model_params)

# Create and evaluate pipeline
pipeline = Pipeline(steps=[('preprocessor', preprocessor), ('model', model)])
kf = KFold(n_splits=5, shuffle=True, random_state=1)
scores = cross_val_score(pipeline, X_train, y_train, cv=kf, scoring='accuracy')
print('CV Accuracy: {:.4f}'.format(scores.mean()))


# Fit and predict
pipeline.fit(X_train, y_train)

# Fit and predict
pipeline.fit(X_train, y_train)
y_pred = pipeline.predict(X_test)

# Check if the model has feature_importances_ attribute
if hasattr(model, 'feature_importances_'):
    importances = model.feature_importances_
    features = X_train.columns
    indices = np.argsort(importances)

    plt.figure(figsize=(10, 8))
    plt.title('Feature Importances')
    plt.barh(range(len(indices)), importances[indices], color='b', align='center')
    plt.yticks(range(len(indices)), [features[i] for i in indices], fontsize=7)
    plt.xlabel('Relative Importance')
    plt.tight_layout()
    plt.savefig(f'{model_type}_feature_importances.png')

# Generate learning curve
train_sizes, train_scores, test_scores = learning_curve(pipeline, X_train, y_train, cv=kf)
train_scores_mean = np.mean(train_scores, axis=1)
test_scores_mean = np.mean(test_scores, axis=1)

plt.figure(figsize=(8, 6))
plt.plot(train_sizes, train_scores_mean, label='Training score')
plt.plot(train_sizes, test_scores_mean, label='Cross-validation score')
plt.title('Learning Curves')
plt.xlabel('Training examples')
plt.ylabel('Score')
plt.legend(loc='best')
plt.grid()
plt.savefig(f'{model_type}_learning_curve.png')


# Predictions
predictions=pipeline.predict(X_test)

# Save Predictions
output = pd.DataFrame({'id': X_test['id'], 'status_group': predictions})
output.to_csv(prediction_output, index=False)

print('Saved predictions to {}'.format(prediction_output))