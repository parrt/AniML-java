from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
from sklearn import preprocessing
from sklearn.utils import check_random_state
from sklearn.feature_extraction import DictVectorizer
from sklearn import tree

data = pandas.read_table("../data/Heart-wo-NA.csv", header=0, sep=",")
# print heart

# names = ["id","Age","Sex","ChestPain","RestBP","Chol","Fbs","RestECG","MaxHR","ExAng","Oldpeak","Slope","Ca","Thal","AHD"]

# encode target strings as int
data[['AHD']] = data[['AHD']].apply(lambda x : pandas.factorize(x)[0]) # encode target as int if string
# one hot encode other strings
data = pandas.get_dummies(data)

colnames = data.columns

v = data.values
# print type(v)
# print heart.columns
# print len(heart.columns)

dim = data.shape[1]
target_index = dim-1

X = v[:,0:target_index]
y = v[:,target_index]

random = 99 # pick reproducible pseudo-random sequence

clf = RandomForestClassifier(n_estimators=50, oob_score=True,
                             max_features="sqrt", bootstrap=True,
                             min_samples_leaf=20, criterion="entropy",
                             random_state=random)
clf = clf.fit(X, y)
oob_error = 1 - clf.oob_score_
tree.export_graphviz(clf.estimators_[0], out_file="/tmp/t0.dot", feature_names=colnames)

kfold = KFold(n_splits=5, shuffle=True, random_state=random)

avg_err = 0.0
for train_index, test_index in kfold.split(X):
    # print("TRAIN:", train_index, "TEST:", test_index)
    X_train, X_test = X[train_index], X[test_index]
    y_train, y_test = y[train_index], y[test_index]
    clf = RandomForestClassifier(n_estimators=50, oob_score=False,
                                 max_features="sqrt", bootstrap=True,
                                 min_samples_leaf=20, criterion="entropy",
                                 random_state=random)
    clf = clf.fit(X_train, y_train)
    # print "oob error", oob_error,
    cats = clf.predict(X_test)
    counts = collections.Counter(y_test==cats)
    err = counts[False] / float(len(y_test))
    avg_err += err
    # print "5-fold error:", counts[False], '/', len(y_test), err

print "oob %.5f" % oob_error, "kfold %5f" % (avg_err / 5.0)

