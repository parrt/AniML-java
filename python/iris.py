from sklearn import datasets
from sklearn.ensemble import RandomForestClassifier
iris = datasets.load_iris()

X = iris.data
Y = iris.target

print iris

clf = RandomForestClassifier(n_estimators=10,
                               oob_score=True)
clf = clf.fit(X, Y)
oob_error = 1 - clf.oob_score_
print oob_error