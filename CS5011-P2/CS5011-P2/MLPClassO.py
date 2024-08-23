import pandas as pd
import optuna
from sklearn.neural_network import MLPClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

#[I 2024-03-11 19:46:09,505] Trial 21 finished with value: 0.7712121212121212 and parameters:
#{'hidden_layer_sizes': '50_50', 'activation': 'relu', 'solver': 'adam', 'alpha': 0.01823719670834927, 
#'learning_rate_init': 0.00040698321428077675, 'max_iter': 246}. Best is trial 21 with value: 0.7712121212121212.

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')

# Separate features and target variable
X = train_df.drop('status_group', axis=1)
y = train_df['status_group']
X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.2, random_state=42)

# Scale features
scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_val_scaled = scaler.transform(X_val)

# Define the objective function for the HPO
def objective(trial):
    # Define the hyperparameter space
    hidden_layer_sizes = trial.suggest_categorical('hidden_layer_sizes', ['50', '100', '50_50', '100_100'])
    if '_' in hidden_layer_sizes:
        hidden_layer_sizes = tuple(map(int, hidden_layer_sizes.split('_')))
    else:
        hidden_layer_sizes = (int(hidden_layer_sizes),)
    activation = trial.suggest_categorical('activation', ['relu', 'tanh'])
    solver = trial.suggest_categorical('solver', ['adam', 'sgd'])
    alpha = trial.suggest_float('alpha', 1e-5, 1e-1, log=True)
    learning_rate_init = trial.suggest_float('learning_rate_init', 1e-5, 1e-1, log=True)
    max_iter = trial.suggest_int('max_iter', 100, 1000)

    # Initialize and train the model
    mlp = MLPClassifier(
        hidden_layer_sizes=hidden_layer_sizes,
        activation=activation,
        solver=solver,
        alpha=alpha,
        learning_rate_init=learning_rate_init,
        max_iter=max_iter,
        random_state=42
    )
    mlp.fit(X_train_scaled, y_train)
    
    # Predict and evaluate the model
    y_pred = mlp.predict(X_val_scaled)
    accuracy = accuracy_score(y_val, y_pred)
    
    return accuracy

# Execute the optimization
study = optuna.create_study(direction='maximize')
study.optimize(objective, n_trials=50)  

# Best hyperparameters
print('Best trial:', study.best_trial.params)

# Retrain your model using the best hyperparameters found
best_params = study.best_trial.params
if 'hidden_layer_sizes' in best_params and isinstance(best_params['hidden_layer_sizes'], str):
    best_params['hidden_layer_sizes'] = tuple(map(int, best_params['hidden_layer_sizes'].split('_')))

mlp_best = MLPClassifier(**best_params, random_state=42)

# We must scale the entire dataset before training the final model
X_scaled = scaler.fit_transform(X)
mlp_best.fit(X_scaled, y)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df
X_test_scaled = scaler.transform(X_test)  # Scale the test data with the same scaler

# Predict the labels for the test set using the optimized model
y_pred = mlp_best.predict(X_test_scaled)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
y_pred_labels = [status_group_mapping_inv[label] for label in y_pred]

# Prepare the submission dataframe
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': y_pred_labels
})

# Save the submission file in the required format
submission_df.to_csv('MLPClassOsubmission.csv', index=False)