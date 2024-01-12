from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn import metrics
from sklearn import tree

import pandas as pd
import sys
import os


dataset_name = sys.argv[1]
dataset_path = sys.argv[2]

dataset = pd.read_csv(dataset_path + "/project/assets/datasets/" + dataset_name + ".csv")

print("starting training")

column_names = list(dataset)
target_column_name = column_names[len(column_names) - 1]
dataset.rename(columns={target_column_name: "target"}, inplace=True)

X = dataset.drop(["target"], axis=1)
Y = dataset["target"]
X_train, X_test, Y_train, Y_test = train_test_split(X, Y, test_size=0.3)  # 70% training and 30% test

clf = RandomForestClassifier(n_estimators=100)

clf.fit(X_train, Y_train)
y_pred = clf.predict(X_test)

print("accuracy:", metrics.accuracy_score(Y_test, y_pred))

directory = dataset_name
tree_path = dataset_path + "/project/assets/trees/" + dataset_name
tree_folder_path = dataset_path + "/project/assets/trees"

if (os.path.exists(tree_folder_path)):
    if os.path.exists(tree_path):
        pass
    else:
        os.mkdir(tree_path)
else:
    os.mkdir(tree_folder_path)

i = 0
for t in clf.estimators_:
    text = tree.export_graphviz(t)
    fileName = "tree" + str(i) + ".txt"
    fileTree = open(tree_path + "/" + fileName, 'w')
    fileTree.write(text)
    fileTree.close()
    print(f"generating decision tree{i}")
    i += 1
