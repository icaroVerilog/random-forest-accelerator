from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn import metrics
from sklearn import tree

import pandas as pd
import sys
import os


dataset_name = sys.argv[1]
path = sys.argv[2]

dataset = pd.read_csv(path + "/project/assets/datasets/" + dataset_name + ".csv")

print("starting training")


def remove_id(dataset):
    dataset = dataset.rename(columns=lambda name: name.lower())
    column_names = list(dataset)

    for index in range(len(column_names)):
        if column_names[index] == "id":
            dataset = dataset.drop(columns="id", axis=1)
            break

    return dataset


def parse_qualitative_variables(dataset):
    dataset = pd.get_dummies(dataset, drop_first=True)
    return dataset


column_names = list(dataset)
target_column_name = column_names[len(column_names) - 1]
dataset.rename(columns={target_column_name: "target"}, inplace=True)

dataset = remove_id(dataset)

X = dataset.drop(["target"], axis=1)
X = parse_qualitative_variables(X)
Y = dataset["target"]
X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.3)  # 70% training and 30% test

clf = RandomForestClassifier(n_estimators=100)

clf.fit(X_train, Y_train)
y_pred = clf.predict(X_test)

print("accuracy:", metrics.accuracy_score(Y_test, y_pred))

directory = dataset_name
tree_path = path + "/project/assets/trees/" + dataset_name

if os.path.exists(tree_path):
    pass
else:
    os.mkdir(tree_path)

i = 0
for t in clf.estimators_:
    text = tree.export_graphviz(t)
    fileName = "tree" + str(i) + ".txt"
    fileTree = open(tree_path + "/" + fileName, 'w')
    fileTree.write(text)
    fileTree.close()
    print(f"generating decision tree{i}")
    i += 1
