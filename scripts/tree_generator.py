import os
import sys

import pandas as pd
import numpy as np
from sklearn import metrics
from sklearn import tree
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split

DATASET_NAME = sys.argv[1]
DATASET_PATH = sys.argv[2]
DATASET_TRAIN_PERCENT = int(sys.argv[3])
TREE_QUANTITY = int(sys.argv[4])

if sys.argv[5] == 0:
    MAX_DEPTH = None
else:
    MAX_DEPTH = int(sys.argv[5])

def normalize_dataset(df, min_val, max_val):
    result = df.copy()
    for col in result.columns:
        if col == 'target':
            continue

        if not pd.api.types.is_numeric_dtype(result[col]):
            continue

        col_min = result[col].min()
        col_max = result[col].max()
        if col_max == col_min:
            continue

        scale = (max_val - min_val) / (col_max - col_min)
        normalized = (result[col] - col_min) * scale + min_val
        mask = result[col].isin([0, 1])
        result[col] = np.where(mask, result[col], normalized)
    return result

def normalize_dataset_int(df, min_val, max_val):
    result = df.copy()
    for col in result.columns:
        if col == 'target':
            continue
        if not pd.api.types.is_numeric_dtype(result[col]):
            continue

        col_min = result[col].min()
        col_max = result[col].max()
        if col_max == col_min:
            result[col] = min_val
            continue

        scale = (max_val - min_val) / (col_max - col_min)
        normalized = (result[col] - col_min) * scale + min_val
        result[col] = normalized.round().astype(int)
    return result




dataset = pd.read_csv(f"{DATASET_PATH}/datasets/{DATASET_NAME}")

column_names = list(dataset)
target_column_name = column_names[len(column_names) - 1]
dataset.rename(columns={target_column_name: "target"}, inplace=True)

# IEE75416
# dataset = normalize_dataset(dataset=dataset, min_val=-65504, max_val=65504)

# dataset = normalize_dataset(dataset=dataset, min_val=-448, max_val=448)
# dataset.to_csv(DATASET_NAME)


X = dataset.drop(["target"], axis=1)
Y = dataset["target"]

X_train, X_test, Y_train, Y_test = train_test_split(X, Y, train_size=(DATASET_TRAIN_PERCENT / 100))

clf = RandomForestClassifier(n_estimators=TREE_QUANTITY, max_depth=MAX_DEPTH)

clf.fit(X_train, Y_train)
Y_pred = clf.predict(X_test)

accuracy = metrics.accuracy_score(Y_test, Y_pred)
print("accuracy:", accuracy, "\n")

directory = DATASET_NAME
tree_path = DATASET_PATH + "/trees/" + DATASET_NAME
tree_folder_path = DATASET_PATH + "/trees"

if os.path.exists(tree_folder_path):
    if os.path.exists(tree_path):
        folder = os.listdir(tree_path)
        for file in folder:
            os.remove(tree_path + "/" + file)
    else:
        os.mkdir(tree_path)
else:
    os.mkdir(tree_folder_path)
    os.mkdir(tree_path)

counter = 0
for a in clf.estimators_:
    text = tree.export_graphviz(a)
    fileName = "tree" + str(counter) + ".txt"
    fileTree = open(tree_path + "/" + fileName, 'w')
    fileTree.write(text)
    fileTree.close()
    print(f"generating decision tree{counter}")
    counter += 1
