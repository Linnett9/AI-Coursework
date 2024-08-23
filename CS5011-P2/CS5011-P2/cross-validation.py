from sklearn.model_selection import cross_val_score, KFold
from sklearn.metrics import confusion_matrix, plot_confusion_matrix, roc_curve, auc
import matplotlib.pyplot as plt
import seaborn as sns

# Perform 5-Fold Cross Validation
kf = KFold(n_splits=5, random_state=42, shuffle=True)
log_reg_scores = cross_val_score(log_reg, X_train, y_train, cv=kf, scoring='accuracy')

# Plotting the distribution of accuracies across folds
plt.figure(figsize=(10, 6))
sns.boxplot(log_reg_scores)
plt.title('Logistic Regression Accuracy Distribution Across 5-Fold CV')
plt.show()

# Comparing mean accuracies in a bar chart
# Assuming you have accuracies for other models as well
mean_accuracies = {'Logistic Regression': log_reg_scores.mean(), 'OtherModel': other_model_scores.mean()}
plt.figure(figsize=(10, 6))
plt.bar(range(len(mean_accuracies)), list(mean_accuracies.values()), align='center')
plt.xticks(range(len(mean_accuracies)), list(mean_accuracies.keys()))
plt.title('Mean CV Accuracies of Different Models')
plt.show()

# Confusion Matrix for Logistic Regression
plot_confusion_matrix(log_reg, X_test, y_pred_labels)
plt.title('Confusion Matrix for Logistic Regression')
plt.show()
