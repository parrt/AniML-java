from sklearn import tree
import pandas
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import KFold
import collections

heart = pandas.read_table("../data/Heart-wo-NA.csv", header=0, sep=",")
# print heart

# names = ["id","Age","Sex","ChestPain","RestBP","Chol","Fbs","RestECG","MaxHR","ExAng","Oldpeak","Slope","Ca","Thal","AHD"]

print heart.columns
print len(heart.columns)

clf = RandomForestClassifier(n_estimators=20, oob_score=True, min_samples_leaf=20,
                             criterion="entropy")
clf = clf.fit(heart[0:15], heart["AHD"])
