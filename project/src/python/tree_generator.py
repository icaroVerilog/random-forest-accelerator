from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn import metrics
from sklearn import tree

import pandas as pd
import sys
import os

# dataset_name = sys.argv[1]
dataset_name = "Iris"


dataset = pd.read_csv("../../assets/datasets/" + dataset_name + ".csv")
# dataset.head()

column_names = list(dataset)
target_column_name = column_names[len(column_names) - 1]
dataset.rename(columns={target_column_name: "Target"}, inplace=True)

# criando a matriz de características X (dataframe) com os valores originais das variáveis, desconsiderando a última
# coluna (target)
X = dataset.drop(["Target"], axis=1)
# criando o vetor de rótulos (saída esperada do modelo para cada amostra)
Y = dataset["Target"]

# Split dataset into training set and test set
X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=0.3)  # 70% training and 30% test

clf = RandomForestClassifier(n_estimators=100)

# Train the model using the training sets y_pred=clf.predict(X_test)
clf.fit(X_train, y_train)

y_pred = clf.predict(X_test)

print("Accuracy:", metrics.accuracy_score(y_test, y_pred))

directory = dataset_name
parent_dir = "../../assets/trees/"
path = os.path.join(parent_dir, directory)

i = 0
for t in clf.estimators_:
    text = tree.export_graphviz(t)
    fileName = "tree" + str(i) + ".txt"
    fileTree = open("../../assets/trees/" + directory + "/" + fileName, 'w')
    fileTree.write(text)
    fileTree.close()
    print("Tree ", i, " succefully generated")
    i += 1
