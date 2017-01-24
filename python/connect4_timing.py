"""
$ python python/connect4_timing.py 50 20
"""

from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections
from sklearn import preprocessing
from sklearn.utils import check_random_state
from sklearn.feature_extraction import DictVectorizer
from sklearn import tree
import time
import sys

data = pandas.read_table("data/connect-4.csv", header=0, sep=",")

cvt = data.columns
targetcol = cvt[-1] # last col
cvt = cvt[0:-1]     # don't convert last to dummy

# print heart
# cvt = [u'id', u'Age', u'Sex', u'ChestPain', u'RestBP', u'Chol', u'Fbs',
#        u'RestECG', u'MaxHR', u'ExAng', u'Oldpeak', u'Slope', u'Ca', u'Thal']

# encode target strings as int
data[[targetcol]] = data[[targetcol]].apply(lambda x : pandas.factorize(x)[0]) # encode target as int if string
# one hot encode other strings
dummied_data = pandas.get_dummies(data[cvt])
data = pandas.concat([dummied_data, data[[targetcol]]], axis=1) # put party on the end

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

n_estimators = int(sys.argv[1])
min_samples_leaf = int(sys.argv[2])

start = time.clock()
clf = RandomForestClassifier(n_estimators=n_estimators, oob_score=False,
                             max_features="sqrt", bootstrap=True,
                             min_samples_leaf=min_samples_leaf, criterion="entropy",
                             random_state=random)
clf = clf.fit(X, y)
stop = time.clock()

print "Fitting %d estimators %d min leaf size %f seconds\n" % (n_estimators,min_samples_leaf,stop-start)
