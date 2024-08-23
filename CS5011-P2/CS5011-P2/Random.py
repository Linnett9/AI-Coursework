import pandas as pd
from sklearn.ensemble import RandomForestClassifier

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')

# Load the processed test data (without the true 'status_group' values)
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')

# Separate features for training and test data
X_train = train_df.drop('status_group', axis=1)
y_train = train_df['status_group']

X_test = test_df  # Test dataframe should not have the 'status_group' column

# Initialize the RandomForestClassifier model
rf_clf = RandomForestClassifier(n_estimators=100, random_state=42)

# Fit the model with the training data
rf_clf.fit(X_train, y_train)

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
submission_df.to_csv('randomsubmission.csv', index=False)