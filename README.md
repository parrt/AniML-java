# Simple Random Forest(tm) in Java

[codebuff](https://github.com/antlr/codebuff) could really use a random forest so I'm playing with an implementation here.

**Limitations**

All `int` values but supports categorical and numerical values.

**Notes from conversation with** [Jeremy Howard](https://www.usfca.edu/data-institute/about-us/researchers)

select m = sqrt(M) vars at each node

for discrete/categorical vars, split in even sizes, as pure as possible.
 
log likelihood or p(1-p) per category

each node as predicted var, cutoff val

nodes are binary decisions

find optimal split point exhaustively.

for discrete, try all (or treat as continuous)

for continuous variables, sort by that variable and choose those split points at each unique value. each split has low variance of dependent variable

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

and

> Efficient methods exist for finding good split points: start by sorting the values
of the attribute, and then consider only split points that are between two examples in
sorted order that have different classifications, while keeping track of the running totals
of positive and negative examples on each side of the split point.