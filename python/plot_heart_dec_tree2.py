from sklearn import tree
import pandas
import pydotplus 
from sklearn.datasets.mldata import fetch_mldata

heart = pandas.read_table("../data/Heart-wo-NA.csv", header=0, sep=",")
print heart

clf = tree.DecisionTreeClassifier(criterion="entropy")
clf = clf.fit(heart.values, heart['AHD'])

names = ["id","Age","Sex","ChestPain","RestBP","Chol","Fbs","RestECG","MaxHR","ExAng","Oldpeak","Slope","Ca","Thal","AHD"]

dot_data = tree.export_graphviz(clf, feature_names=names, out_file="/tmp/heart.dot")

#graph = pydotplus.graph_from_dot_data(dot_data)
# graph.write_pdf("/tmp/heart.pdf")
