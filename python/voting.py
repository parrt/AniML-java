from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
from sklearn import preprocessing
from sklearn.utils import check_random_state
from sklearn.feature_extraction import DictVectorizer
from sklearn import tree

data = pandas.read_table("data/house-votes-84.csv", header=0, sep=",")

# encode target strings as int
data[['party']] = data[['party']].apply(lambda x : pandas.factorize(x)[0]) # encode target as int if string
# one hot encode other strings

cvt = [u'handicapped-infants', u'water-project-cost-sharing',
       u'adoption-of-the-budget-resolution', u'physician-fee-freeze',
       u'el-salvador-aid', u'religious-groups-in-schools',
       u'anti-satellite-test-ban', u'aid-to-nicaraguan-contras', u'mx-missile',
       u'immigration', u'synfuels-corporation-cutback', u'education-spending',
       u'superfund-right-to-sue', u'crime', u'duty-free-exports',
       u'export-administration-act-south-africa']
dummied_data = pandas.get_dummies(data[cvt])
data = pandas.concat([dummied_data, data[['party']]], axis=1) # put party on the end
# print data

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

