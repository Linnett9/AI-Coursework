import pandas as pd
import optuna
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split

#[I 2024-03-11 21:08:19,658] Trial 60 finished with value: 0.7415824915824916 and parameters:
#{'C': 4.090020690841885, 'max_iter': 381, 'solver': 'newton-cg', 'multi_class': 'ovr'}. 
#Best is trial 60 with value: 0.7415824915824916.


# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')

# Separate features and target variable
X = train_df.drop('status_group', axis=1)
y = train_df['status_group']
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

# Define the objective function for the HPO
def objective(trial):
    C = trial.suggest_float('C', 1e-5, 100)
    max_iter = trial.suggest_int('max_iter', 100, 2000)
    solver = trial.suggest_categorical('solver', ['newton-cg', 'lbfgs', 'liblinear', 'sag', 'saga'])
    multi_class = trial.suggest_categorical('multi_class', ['auto', 'ovr', 'multinomial'])

    # Exclude the combination of 'liblinear' and 'multinomial'
    if solver == 'liblinear' and multi_class == 'multinomial':
        raise optuna.TrialPruned()

    log_reg = LogisticRegression(C=C, max_iter=max_iter, solver=solver, multi_class=multi_class, random_state=0)
    log_reg.fit(X_train, y_train)

    # Evaluate the model
    score = log_reg.score(X_val, y_val)
    return score

# Execute the optimization
study = optuna.create_study(direction='maximize')
study.optimize(objective, n_trials=100)  # Adjust the number of trials as needed

# Best hyperparameters
print('Best trial:', study.best_trial.params)

# Retrain your model using the best hyperparameters found
best_params = study.best_trial.params
log_reg_best = LogisticRegression(**best_params, random_state=42, n_jobs=-1)
log_reg_best.fit(X_train, y_train)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df

# Predict the labels for the test set using the optimized model
y_pred = log_reg_best.predict(X_test)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
y_pred_labels = [status_group_mapping_inv[label] for label in y_pred]

# Prepare the submission dataframe
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': y_pred_labels
})

# Save the submission file in the required format
submission_df.to_csv('log_optuna_submission.csv', index=False)
