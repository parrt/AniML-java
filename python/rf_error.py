"""
Given a csv file with header row, number of estimators, min leaf size, and k folds
compute print the out-of-bag error for a RandomForest classifier and
then the average k-fold error. Sample invocation:

$ python rf_error.py ../data/Heart-wo-NA.csv 50 20 5
"""
from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
from sklearn import preprocessing
from sklearn.utils import check_random_state
import sys

filename = sys.argv[1] # e.g., "../data/Heart-wo-NA.csv"
n_estimators = int(sys.argv[2])
min_samples_leaf = int(sys.argv[3])
k = int(sys.argv[4])

data = pandas.read_table(filename, header=0, sep=",")
dim = len(data.columns)
target_index = dim-1

# label encode strings

# TODO:
# import pandas as pd
# s = pd.Series(list('abca'))
data = data[data.columns].apply(lambda x : pandas.factorize(x)[0])

# convert to ndarray from pandas DataFrame
data = data.values

X = data[:, 0:target_index]
y = data[:, target_index]

random = 99 # pick reproducible pseudo-random sequence

kfold = KFold(n_splits=k, shuffle=True, random_state=random)

avg_err = 0.0
for train_index, test_index in kfold.split(X):
    # print("TRAIN:", train_index, "TEST:", test_index)
    X_train, X_test = X[train_index], X[test_index]
    y_train, y_test = y[train_index], y[test_index]
    clf = RandomForestClassifier(n_estimators=n_estimators, oob_score=False,
                                 min_samples_leaf=min_samples_leaf, criterion="entropy",
                                 random_state=random)
    clf = clf.fit(X_train, y_train)
    # print "oob error", oob_error,
    cats = clf.predict(X_test)
    counts = collections.Counter(y_test==cats)
    err = counts[False] / float(len(y_test))
    avg_err += err
    # print "5-fold error:", counts[False], '/', len(y_test), err

clf = RandomForestClassifier(n_estimators=n_estimators, oob_score=True,
                             min_samples_leaf=min_samples_leaf, criterion="entropy",
                             random_state=random)
clf = clf.fit(X, y)
oob_error = 1 - clf.oob_score_
print "oob %.5f" % oob_error, "kfold %5f" % (avg_err / 5.0)

exit(0)
