from sklearn import datasets
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
import numpy as np
import pandas

iris = datasets.load_iris()

X = iris.data
y = iris.target

# print iris

kfold = KFold(n_splits=5, shuffle=True)
print kfold

for train_index, test_index in kfold.split(X):
    # print("TRAIN:", train_index, "TEST:", test_index)
    X_train, X_test = X[train_index], X[test_index]
    y_train, y_test = y[train_index], y[test_index]
    clf = RandomForestClassifier(n_estimators=20, oob_score=True, min_samples_leaf=20, criterion="entropy")
    clf = clf.fit(X_train, y_train)
    oob_error = 1 - clf.oob_score_
    print "oob error", oob_error,
    cats = clf.predict(X_test)
    counts = collections.Counter(y_test==cats)
    print "5-fold error:", counts[False], '/', len(y_test), counts[False]/float(len(y_test))