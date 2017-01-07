# Simple Random Forest(tm) in Java

[codebuff](https://github.com/antlr/codebuff) could really use a random forest so I'm playing with an implementation here.

**Limitations**

All `int` values but supports categorical and numerical values.

**Notes from conversation with** [Jeremy Howard](https://www.usfca.edu/data-institute/about-us/researchers)

select m = sqrt(M) vars at each node

for discrete/categorical vars, split in even sizes, as pure as possible.
I ended up treating cat vars like continuous. We're separating hyperplanes not
relying on two cat vars being less than or greater. It's like
grouping cat vars: easiest thing is to sort them. Or, like
binary search looking for a specific value. The int comparison
relationship is arbitrary but useful nonetheless for searching,
which is what the random forest is doing. sweet.

log likelihood or p(1-p) per category

each node as predicted var, cutoff val

nodes are binary decisions

find optimal split point exhaustively.

for discrete, try all (or treat as continuous)

for continuous variables, sort by that variable and choose those split points at each unique value. each split has low variance of dependent variable

I'm going to try encoding floats as int so int[] can always
be the data type of a row.

<img src="whiteboard.jpg" width=300>

**References**

[ironmanMA](https://github.com/ironmanMA/Random-Forest) has some nice notes.

[Breiman's RF site](https://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm)

[patricklamle's python impl](http://www.patricklamle.com/Tutorials/Decision%20tree%20python/tuto_decision%20tree.html)

Russell and Norvig's AI book says:

> When an attribute has many possible values, the information
gain measure gives an inappropriate indication of the attribute’s usefulness. In the extreme
case, an attribute such as ExactTime has a different value for every example,
which means each subset of examples is a singleton with a unique classification, and
the information gain measure would have its highest value for this attribute. But choosGAIN
RATIO ing this split first is unlikely to yield the best tree. One solution is to use the gain ratio
(Exercise 18.10). Another possibility is to allow a Boolean test of the form A = vk, that
is, picking out just one of the possible values for an attribute, leaving the remaining
values to possibly be tested later in the tree.  

I don't think I have this problem if I limit to binary decision nodes as it can't create singled subsets, one per discrete var value.

Also:

> Efficient methods exist for finding good split points: start by sorting the values
of the attribute, and then consider only split points that are between two examples in
sorted order that have different classifications, while keeping track of the running totals
of positive and negative examples on each side of the split point.

From *An Introduction to Statistical Learning* (Gareth James, Daniela Witten, Trevor Hastie, Robert Tibshirani)

1st RF tweak to decision trees. The following explains why we bootstrap a new data set for each tree in forest (*bagging*): 

> Hence a natural way to reduce the variance and hence increase the prediction
accuracy of a statistical learning method is to take many training sets
from the population, build a separate prediction model using each training
set, and average the resulting predictions.

The 2nd RF tweak over decision trees is to use a subset of the possible variables when trying to find a split variable at each node.

> ... each time a split in a tree is considered, a random sample of
m predictors is chosen as split candidates from the full set of p predictors.
The split is allowed to use only one of those m predictors. ... Suppose
that there is one very strong predictor in the data set, along with a number
of other moderately strong predictors. Then in the collection of bagged 
trees, most or all of the trees will use this strong predictor in the top split.
Consequently, all of the bagged trees will look quite similar to each other.