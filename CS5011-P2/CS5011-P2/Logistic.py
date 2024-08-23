import pandas as pd
from sklearn.linear_model import LogisticRegression

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')

# Load the processed test data (without the true 'status_group' values)
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')

# Separate features for training and test data
X_train = train_df.drop('status_group', axis=1)
y_train = train_df['status_group']

X_test = test_df  # Test dataframe should not have the 'status_group' column

# Initialize the LogisticRegression model
log_reg = LogisticRegression(max_iter=10000, solver='saga', multi_class='multinomial')

# Fit the model with the training data
log_reg.fit(X_train, y_train)

# Predict the labels for the test set
y_pred = log_reg.predict(X_test)

# Convert numerical predictions back to the original- labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
y_pred_labels = [status_group_mapping_inv[label] for label in y_pred]

# Prepare the submission dataframe
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': y_pred_labels
})

# Save the submission file in the required format
submission_df.to_csv('logsubmissionlessiter.csv', index=False)