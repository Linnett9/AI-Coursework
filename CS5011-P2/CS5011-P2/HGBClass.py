import pandas as pd
from sklearn.experimental import enable_hist_gradient_boosting 
from sklearn.ensemble import HistGradientBoostingClassifier

# Load the processed training data
train_df = pd.read_csv('CleanedTrainingSetValues_encoded1.csv')
y_train = train_df['status_group'].values
X_train = train_df.drop(columns=['status_group', 'id'])  # Ensure 'id' is not used as a feature

# Initialize the HistGradientBoostingClassifier
hist_grad_boost_clf = HistGradientBoostingClassifier(max_iter=100, 
                                                     learning_rate=0.1, 
                                                     max_depth=10, 
                                                     random_state=42, 
                                                     early_stopping=True, 
                                                     validation_fraction=0.1, 
                                                     n_iter_no_change=10)

# Fit the model on the training data
hist_grad_boost_clf.fit(X_train, y_train)

# Load the processed test data
test_df = pd.read_csv('CleanedTestSetValues_encoded.csv')
X_test = test_df.drop(columns=['id'])  

# Predict the labels for the test set
test_predictions = hist_grad_boost_clf.predict(X_test)

# Convert numerical predictions back to the original labels
status_group_mapping_inv = {2: 'functional', 1: 'functional needs repair', 0: 'non functional'}
test_predictions_labels = [status_group_mapping_inv[label] for label in test_predictions]

# Prepare the submission DataFrame
submission_df = pd.DataFrame({
    'id': test_df['id'],
    'status_group': test_predictions_labels
})

# Save the submission DataFrame to a CSV file
submission_df.to_csv('HGBSubmission.csv', index=False)