import pandas as pd
from sklearn.ensemble import GradientBoostingClassifier

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')
y_train = train_df['status_group'].values
X_train = train_df.drop(columns=['status_group', 'id'])  # Ensure 'id' is not used as a feature

# Define the best hyperparameters
best_params = {
    'n_estimators': 252,
    'learning_rate': 0.10284802432225608,
    'max_depth': 10,
    'min_samples_split': 6,
    'min_samples_leaf': 1
}

# Initialize the GradientBoostingClassifier with the best hyperparameters
gb_clf_best = GradientBoostingClassifier(**best_params, random_state=42)

# Fit the model on the training data
gb_clf_best.fit(X_train, y_train)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df.drop(columns=['id'])  #  exclude non-feature columns

# Predict the labels for the test set using the optimized model
y_pred = gb_clf_best.predict(X_test)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
y_pred_labels = [status_group_mapping_inv[label] for label in y_pred]

# Prepare the submission DataFrame
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': y_pred_labels
})

# Save the submission DataFrame to a CSV file
submission_df.to_csv('GBOsubmission.csv', index=False)
