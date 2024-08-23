import pandas as pd
import optuna
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
#[I 2024-03-10 16:02:16,898] Trial 12 finished with value: 0.8057239057239057 and parameters:
#{'n_estimators': 252, 'learning_rate': 0.10284802432225608, 'max_depth': 10, 'min_samples_split': 6, 'min_samples_leaf': 1}. 
#Best is trial 12 with value: 0.8057239057239057.
#Stopped at trial 40 as it took 5 hours to run
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
    learning_rate = trial.suggest_float('learning_rate', 0.01, 0.3)
    max_depth = trial.suggest_int('max_depth', 3, 10)
    min_samples_split = trial.suggest_int('min_samples_split', 2, 14)
    min_samples_leaf = trial.suggest_int('min_samples_leaf', 1, 14)
    
    # Initialize and train the model
    gb_clf = GradientBoostingClassifier(
        n_estimators=n_estimators, 
        learning_rate=learning_rate,
        max_depth=max_depth,
        min_samples_split=min_samples_split,
        min_samples_leaf=min_samples_leaf, 
        random_state=42
    )
    gb_clf.fit(X_train, y_train)
    
    # Predict and evaluate the model
    y_pred = gb_clf.predict(X_val)
    accuracy = accuracy_score(y_val, y_pred)
    
    return accuracy

# Execute the optimization
study = optuna.create_study(direction='maximize')
study.optimize(objective, n_trials=100)  # Adjust the number of trials as needed

# Best hyperparameters
print('Best trial:', study.best_trial.params)

# Retrain your model using the best hyperparameters found
best_params = study.best_trial.params
gb_clf_best = GradientBoostingClassifier(**best_params, random_state=42)
gb_clf_best.fit(X_train, y_train)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df.drop(columns=['id'])  # Make sure to exclude non-feature columns

# Predict the labels for the test set using the optimized model
y_pred = gb_clf_best.predict(X_test)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
y_pred_labels = [status_group_mapping_inv[label] for label in y_pred]

# Prepare the submission dataframe
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': y_pred_labels
})

# Save the submission file in the required format
submission_df.to_csv('GB_optuna_submission.csv', index=False)
