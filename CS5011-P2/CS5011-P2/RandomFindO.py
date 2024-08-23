import pandas as pd
import optuna
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split

#Best trial: {'n_estimators': 247, 'max_depth': 29, 'min_samples_split': 4, 'min_samples_leaf': 1}

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')

# Separate features and target variable
X = train_df.drop('status_group', axis=1)
y = train_df['status_group']
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

# Define the objective function for the HPO
def objective(trial):
    # Define the hyperparameter space
    n_estimators = trial.suggest_int('n_estimators', 50, 300)
    max_depth = trial.suggest_int('max_depth', 5, 30)
    min_samples_split = trial.suggest_int('min_samples_split', 2, 15)
    min_samples_leaf = trial.suggest_int('min_samples_leaf', 1, 14)
    
    # Initialize and train the model
    clf = RandomForestClassifier(n_estimators=n_estimators, max_depth=max_depth,
                                 min_samples_split=min_samples_split,
                                 min_samples_leaf=min_samples_leaf, random_state=42)
    clf.fit(X_train, y_train)
    
    # Predict and evaluate the model
    y_pred = clf.predict(X_val)
    accuracy = accuracy_score(y_val, y_pred)
    
    return accuracy

# Execute the optimization
study = optuna.create_study(direction='maximize')
study.optimize(objective, n_trials=100)  # You can adjust the number of trials

# Best hyperparameters
print('Best trial:', study.best_trial.params)

# Retrain your model using the best hyperparameters found
best_params = study.best_trial.params
rf_clf = RandomForestClassifier(**best_params, random_state=42)
rf_clf.fit(X_train, y_train)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df  # Test dataframe should not have the 'status_group' column

# Predict the labels for the test set
y_pred = rf_clf.predict(X_test)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
y_pred_labels = [status_group_mapping_inv[label] for label in y_pred]

# Prepare the submission dataframe
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': y_pred_labels
})

# Save the submission file in the required format
submission_df.to_csv('randomOsubmission.csv', index=False)