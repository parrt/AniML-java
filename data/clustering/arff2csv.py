from scipy.io import arff
import sys
import numpy
import os
import pandas

fname = sys.argv[1]
basename = os.path.splitext(fname)
f = open(fname)
data, meta = arff.loadarff(f)
print meta
#numpy.savetxt(basename[0]+".csv", data, delimiter=",")
print basename
#data.tofile(basename[0]+".csv", sep=',', format='%10.5f')

df = pandas.DataFrame(data)
df.to_csv(basename[0]+".csv", index=False)
