import pandas as pd
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')
y_train = train_df['status_group'].values
X_train = train_df.drop(columns=['status_group', 'id'])  # Ensure 'id' is not used as a feature

# Scale features as MLP is sensitive to feature scaling
scaler = StandardScaler()
X_train_scaled = scaler.fit_transform(X_train)

# Initialize the MLPClassifier
mlp_clf = MLPClassifier(hidden_layer_sizes=(100,), activation='relu', solver='adam', 
                        alpha=0.0001, batch_size='auto', learning_rate='constant', 
                        learning_rate_init=0.001, max_iter=200, random_state=42)

# Fit the model on the scaled training data
mlp_clf.fit(X_train_scaled, y_train)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df.drop(columns=['id'])  
X_test_scaled = scaler.transform(X_test) 

# Predict the labels for the test set
test_predictions = mlp_clf.predict(X_test_scaled)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
test_predictions_labels = [status_group_mapping_inv[label] for label in test_predictions]

# Prepare the submission DataFrame
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': test_predictions_labels
})

# Save the submission DataFrame to a CSV file
submission_df.to_csv('MLPSubmission.csv', index=False)
