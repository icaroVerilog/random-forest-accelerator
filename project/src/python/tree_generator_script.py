import os
import sys

import pandas as pd
from sklearn import metrics
from sklearn import tree
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split

DATASET_NAME = sys.argv[1]
DATASET_PATH = sys.argv[2]
DATASET_TEST_PERCENT = int(sys.argv[3])
TREE_QUANTITY = int(sys.argv[4])
PRECISION = sys.argv[5]

if sys.argv[6] == "None" or sys.argv[6] == "none":
    MAX_DEPTH = None
else:
    MAX_DEPTH = int(sys.argv[6])

dataset = pd.read_csv(f"{DATASET_PATH}/project/assets/datasets/{DATASET_NAME}.csv")


def max_decimal_places(value):
    # Esta função retorna o número máximo de casas decimais em um valor
    if isinstance(value, float):
        decimal_part = str(value).split('.')[-1]
        return len(decimal_part)
    return 0


def process_column(column):
    # Esta função processa uma única coluna
    max_decimals = column.apply(max_decimal_places).max()
    multiplier = 10 ** max_decimals
    return (column * multiplier).astype(int), max_decimals


def process_dataset(dataset):
    for column in dataset.columns:
        if dataset[column].dtype in ['float64', 'float32']:
            dataset[column], max_decimals = process_column(dataset[column])
            # print(f"Coluna '{column}' processada com {max_decimals} casas decimais")
    return dataset


if PRECISION == "integer":
    dataset = process_dataset(dataset)

column_names = list(dataset)
target_column_name = column_names[len(column_names) - 1]
dataset.rename(columns={target_column_name: "target"}, inplace=True)

X = dataset.drop(["target"], axis=1)
Y = dataset["target"]

X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=(DATASET_TEST_PERCENT / 100))

clf = RandomForestClassifier(n_estimators=TREE_QUANTITY, max_depth=MAX_DEPTH)

clf.fit(X_train, Y_train)
Y_pred = clf.predict(X_test)

print("accuracy:", metrics.accuracy_score(Y_test, Y_pred), "\n")

directory = DATASET_NAME
tree_path = DATASET_PATH + "/project/assets/trees/" + DATASET_NAME
tree_folder_path = DATASET_PATH + "/project/assets/trees"

if os.path.exists(tree_folder_path):
    if os.path.exists(tree_path):
        folder = os.listdir(tree_path)
        for file in folder:
            os.remove(tree_path + "/" + file)
    else:
        os.mkdir(tree_path)
else:
    os.mkdir(tree_folder_path)

counter = 0
for a in clf.estimators_:
    text = tree.export_graphviz(a)
    fileName = "tree" + str(counter) + ".txt"
    fileTree = open(tree_path + "/" + fileName, 'w')
    fileTree.write(text)
    fileTree.close()
    print(f"generating decision tree{counter}")
    counter += 1
