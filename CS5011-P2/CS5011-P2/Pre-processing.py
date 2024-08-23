import pandas as pd
import numpy as np
#import sci-kitlearn
from sklearn.preprocessing import TargetEncoder
from sklearn.preprocessing import StandardScaler


# Load the data into a DataFrame
df = pd.read_csv('TrainingSetValues.csv')
# Load the target values
df_target = pd.read_csv('FunctionalValues.csv')

# Merge the datasets
df = pd.merge(df, df_target, on='id')

# Drop the columns
df = df.drop('date_recorded', axis=1) # No logical insight 
df = df.drop('payment_type', axis=1) # Duplicate Variable 
df = df.drop('wpt_name', axis=1) # Using ID instead 
df = df.drop('source', axis=1) # using source_type instead 
df = df.drop('source_class', axis=1) # using source_type instead 
df = df.drop('waterpoint_type_group', axis=1) #using waterpoint_type 
df = df.drop('management_group', axis=1) # using management instead
df = df.drop('scheme_name', axis=1) # using management instead
df = df.drop('scheme_management', axis=1) # using management instead
df = df.drop('recorded_by', axis=1) # useless 
df = df.drop('subvillage', axis=1) # using other location metrics 
df = df.drop('region', axis=1) # using other location metrics  
df = df.drop('lga', axis=1) # using other location metrics
df = df.drop('ward', axis=1) # using other location metrics
df = df.drop('extraction_type_group', axis=1) # duplicate variable
df = df.drop('extraction_type_class', axis=1) # duplicate variable 
df = df.drop('quality_group', axis=1) # duplicate variable  
df = df.drop('quantity_group', axis=1) #    duplicate variable
df = df.drop('source_type', axis=1) #
df = df.drop('num_private', axis=1) #

#  Convert target value to numerical value 
status_group_mapping = {'functional': 2, 'functional needs repair': 1, 'non functional': 0}
df['status_group'] = df['status_group'].map(status_group_mapping)


#  Dealing with amount_tsh 0.0

# Replace zeros in 'amount_tsh' with 1.0
df.loc[df['amount_tsh'] == 0, 'amount_tsh'] = 1.0

print(df['funder'].unique())

# Function to apply target encoding to a given column in the DataFrame
def encode_categorical_column(df, column, target):
    # Initialize the TargetEncoder
    te = TargetEncoder()
    
    # Fill missing values in the column
    df[column] = df[column].fillna('Unknown')
    
    # Apply fit_transform to the column
    encoded_column = te.fit_transform(df[[column]], target)
    
    # Create new column names for the encoded variables
    for i in range(encoded_column.shape[1]):
        df[f'{column}_encoded_{i}'] = encoded_column[:, i]
    
    # Drop the original column using inplace=True to ensure the column is dropped from the dataframe
    df.drop(column, axis=1, inplace=True)
    
    return df, te

# Make sure 'status_group' is a Series
y = df['status_group']
if isinstance(y, pd.DataFrame):
    y = y.squeeze()  # Converts a single-column DataFrame to a Series

# List of categorical columns to encode
categorical_columns = ['funder', 'installer', 'basin', 'extraction_type', 'management', 'payment', 
                       'water_quality', 'quantity', 'waterpoint_type']

# Apply target encoding to each categorical column
target_encoders = {}  
for column in categorical_columns:
    df, target_encoders[column] = encode_categorical_column(df, column, y)

# Converting T/F to 1/0 for training
true_false_columns = ['public_meeting', 'permit']  

for column in true_false_columns:
    df[column] = df[column].map({True: 1, False: 0}).fillna(-1)
    

# Define the numerical columns for scaling
numerical_columns_for_scale = [
    'amount_tsh', 'gps_height', 'longitude', 'latitude', 
    'region_code', 'district_code', 'population', 'construction_year'
]

# Scale only the specified numerical columns
scaler = StandardScaler()
df[numerical_columns_for_scale] = scaler.fit_transform(df[numerical_columns_for_scale])

# Save the DataFrame with the encoded columns back to CSV
df.to_csv('CleanedTrainingSetValues_encoded1.csv', index=False)



# Load the test data into a DataFrame
df_test = pd.read_csv('TestSetValues.csv')

# Drop the unnecessary columns
columns_to_drop = [
    'date_recorded', 'payment_type', 'wpt_name', 'source', 'source_class',
    'waterpoint_type_group', 'management_group', 'scheme_name', 'scheme_management',
    'recorded_by', 'subvillage', 'region', 'lga', 'ward', 'extraction_type_group',
    'extraction_type_class', 'quality_group', 'quantity_group', 'source_type', 'num_private'
]
df_test = df_test.drop(columns_to_drop, axis=1)

# Handle missing values for categorical columns (assuming you have done the same for the training set)
for column in categorical_columns:
    df_test[column] = df_test[column].fillna('Unknown')

# Convert T/F to 1/0 for boolean columns
for column in true_false_columns:
    df_test[column] = df_test[column].map({True: 1, False: 0}).fillna(-1)  # Use -1 

# For each categorical column, apply the fitted TargetEncoder transformation
for column in categorical_columns:
    # applying the transform method, not fit_transform
    te = target_encoders[column]  
    encoded_test = te.transform(df_test[[column]])
    df_test.drop(column, axis=1, inplace=True)
    for i in range(encoded_test.shape[1]):
        df_test[f'{column}_encoded_{i}'] = encoded_test[:, i]

# Apply the fitted StandardScaler transformation to the numerical columns
df_test[numerical_columns_for_scale] = scaler.transform(df_test[numerical_columns_for_scale])

# Save the processed test DataFrame to CSV
df_test.to_csv('CleanedTestSetValues_encoded.csv', index=False)
