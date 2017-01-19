from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
from sklearn import preprocessing
from sklearn.utils import check_random_state

heart = pandas.read_table("../data/Heart-wo-NA.csv", header=0, sep=",")
# print heart

# names = ["id","Age","Sex","ChestPain","RestBP","Chol","Fbs","RestECG","MaxHR","ExAng","Oldpeak","Slope","Ca","Thal","AHD"]

# label encode strings
heart = heart[heart.columns].apply(lambda x : pandas.factorize(x)[0])


v = heart.values
# print type(v)
# print heart.columns
# print len(heart.columns)

le = preprocessing.LabelEncoder()

X = v[:,0:14]
y = v[:,14]

random = 99 # pick reproducible pseudo-random sequence

kfold = KFold(n_splits=5, shuffle=True, random_state=random)

avg_err = 0.0
for train_index, test_index in kfold.split(X):
    # print("TRAIN:", train_index, "TEST:", test_index)
    X_train, X_test = X[train_index], X[test_index]
    y_train, y_test = y[train_index], y[test_index]
    clf = RandomForestClassifier(n_estimators=50, oob_score=False,
                                 min_samples_leaf=20, criterion="entropy",
                                 random_state=random)
    clf = clf.fit(X_train, y_train)
    # print "oob error", oob_error,
    cats = clf.predict(X_test)
    counts = collections.Counter(y_test==cats)
    err = counts[False] / float(len(y_test))
    avg_err += err
    # print "5-fold error:", counts[False], '/', len(y_test), err

clf = RandomForestClassifier(n_estimators=50, oob_score=True,
                             min_samples_leaf=20, criterion="entropy",
                             random_state=random)
clf = clf.fit(X, y)
oob_error = 1 - clf.oob_score_
print "oob %.5f" % oob_error, "kfold %5f" % (avg_err / 5.0)