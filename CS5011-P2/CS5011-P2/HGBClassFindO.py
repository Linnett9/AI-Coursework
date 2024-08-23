import pandas as pd
import optuna
from sklearn.experimental import enable_hist_gradient_boosting  # noqa
from sklearn.ensemble import HistGradientBoostingClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
#[I 2024-03-10 20:57:14,927] Trial 32 finished with value: 0.793939393939394 and parameters: 
#{'max_iter': 157, 'learning_rate': 0.14684647372707207, 'max_depth': 10, 'min_samples_leaf': 12}. 
#Best is trial 32 with value: 0.793939393939394.

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')

# Separate features and target variable
X = train_df.drop('status_group', axis=1)
y = train_df['status_group']
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

# Define the objective function for the HPO
def objective(trial):
    # Define the hyperparameter space
    max_iter = trial.suggest_int('max_iter', 10, 300)
    learning_rate = trial.suggest_float('learning_rate', 0.01, 0.3)
    max_depth = trial.suggest_int('max_depth', 3, 10)
    min_samples_leaf = trial.suggest_int('min_samples_leaf', 1, 20)
    # Other parameters can be added here as needed
    
    # Initialize and train the model
    hist_gb_clf = HistGradientBoostingClassifier(
        max_iter=max_iter, 
        learning_rate=learning_rate,
        max_depth=max_depth,
        min_samples_leaf=min_samples_leaf, 
        random_state=42
    )
    hist_gb_clf.fit(X_train, y_train)
    
    # Predict and evaluate the model
    y_pred = hist_gb_clf.predict(X_val)
    accuracy = accuracy_score(y_val, y_pred)
    
    return accuracy

# Execute the optimization
study = optuna.create_study(direction='maximize')
study.optimize(objective, n_trials=100)  

# Best hyperparameters
print('Best trial:', study.best_trial.params)

# Retrain your model using the best hyperparameters found
best_params = study.best_trial.params
hist_gb_clf_best = HistGradientBoostingClassifier(**best_params, random_state=42)
hist_gb_clf_best.fit(X_train, y_train)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df

# Predict the labels for the test set using the optimized model
y_pred = hist_gb_clf_best.predict(X_test)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
y_pred_labels = [status_group_mapping_inv[label] for label in y_pred]

# Prepare the submission dataframe
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': y_pred_labels
})

# Save the submission file in the required format
submission_df.to_csv('HGB_optuna_submission.csv', index=False)
