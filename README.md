# AniML machine learning library in Java

I started out building a random forest implementation for fun but finally
decided that this might be the start of a nice little machine learning
library in Java. My emphasis will be on easy to understand code rather than
performance.

Damn thing seems pretty good. Same or better accuracy on my tests than scikit-learn but infinitely easier to understand. Also faster on bigger elements sets.

I'm going to start a series of clustering routines for fun. k-means, k-mediod, mean shift, mediod shift.  Then I can use random forests to transform input space and cluster that with traditional methods.

To learn [Kotlin](https://kotlinlang.org), I'm building some of the code in Kotlin.

## Notes

### Random Forest(tm) in Java

[codebuff](https://github.com/antlr/codebuff) could really use a random forest so I'm playing with an implementation here.

Notes as I try to implement this properly. There's a lot of handwaving out there as well as incorrect implementations. grrr.

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
which is what the random forest is doing. sweet.  Hmm... OOB error is huge.
Jeremy clarified: "*Use one-hot encoding if cardinality <=5, otherwise treat it like an int.*"

log likelihood or p(1-p) per category

each node as predicted var, cutoff val

nodes are binary decisions

find optimal split point exhaustively.

for discrete, try all (or treat as continuous)

for continuous variables, sort by that variable and choose those split points at each unique value. each split has low variance of dependent variable

I'm going to try encoding floats as int so int[] can always
be the elements type of a row.

<img src="whiteboard.jpg" width=300>

More Jeremy notes from Jan 17, 2017:

* Sorting is a huge bottleneck so choose perhaps 20 elements from the complete list associated with a particular node. make it a parameter.
* Map dependent variable categories to 0..n-1 contiguous category encodings a priori; this lets us use a simple array for counting sets per category.
* Don't need to sort actual elements; you can divide a column of independent variables into those values that are less than and greater than equal to the split. As you scan the column, can move values to the appropriate region. Hmm... still sounds like modifying the elements but Jeremy claims that you can do this with one array holding the column values all the way through building a decision tree.
* use min leaf size of like 20 (as it's about where t-distribution looks gaussian)
* definitely use category probabilities when making ensemble classification; ok to average probabilities and pick one
* don't worry about optimizations that help with creating nodes near top of tree; there are few of them. worry about leaves and last decision layer

**References**

[ironmanMA](https://github.com/ironmanMA/Random-Forest) has some nice notes.

[Breiman's RF site](https://www.stat.berkeley.edu/~breiman/RandomForests/cc_home.htm)

[patricklamle's python impl](http://www.patricklamle.com/Tutorials/Decision%20tree%20python/tuto_decision%20tree.html)

Russell and Norvig's AI book says:

> When an attribute has many possible values, the information
gain measure gives an inappropriate indication of the attribute’s usefulness. In the extreme
case, an attribute such as ExactTime has a different value for every example,
which means each subset of examples is a singleton with a unique classification, and
the information gain measure would have its highest value for this attribute. But choosing
this split first is unlikely to yield the best tree. One solution is to use the gain ratio
(Exercise 18.10). Another possibility is to allow a Boolean test of the form A = v_k, that
is, picking out just one of the possible values for an attribute, leaving the remaining
values to possibly be tested later in the tree.  

I don't think I have this problem if I limit to binary decision nodes as it can't create singled subsets, one per discrete var value.

Also:

> Efficient methods exist for finding good split points: start by sorting the values
of the attribute, and then consider only split points that are between two examples in
sorted order that have different classifications, while keeping track of the running totals
of positive and negative examples on each side of the split point.

From *An Introduction to Statistical Learning* (Gareth James, Daniela Witten, Trevor Hastie, Robert Tibshirani)

1st RF tweak to decision trees. The following explains why we bootstrap a new elements set for each tree in forest (*bagging*): 

> Hence a natural way to reduce the variance and hence increase the prediction
accuracy of a statistical learning method is to take many training sets
from the population, build a separate prediction model using each training
set, and average the resulting predictions.

The 2nd RF tweak over decision trees is to use a subset of the possible variables when trying to find a split variable at each node.

> ... each time a split in a tree is considered, a random sample of
m predictors is chosen as split candidates from the full set of p predictors.
The split is allowed to use only one of those m predictors. ... Suppose
that there is one very strong predictor in the elements set, along with a number
of other moderately strong predictors. Then in the collection of bagged 
trees, most or all of the trees will use this strong predictor in the top split.
Consequently, all of the bagged trees will look quite similar to each other.

### Mean shift

In my mind, it's an expensive but straightforward algorithm that associates cluster centroids with density function maxima. We use kernel density estimation with a Gaussian kernel, derived from the original data, and fix that for all time. Now launch a swarm of particles on the surface that seek maxima. Where do you start the particles? At each data original point. The algorithm terminates when no particle is making much progress, possibly oscillating around a maximum.

The problem with gradient ascent/descent is that we need the partial derivatives of the surface function in each dimension, which can get hairy for complex kernels. Fortunately, we can ignore the density estimate itself and go straight to the gradient per [Mean Shift: A robust approach toward future space analysis](http://web.eecs.umich.edu/~silvio/teaching/EECS598/papers/mean_shift.pdf). They show that a "shadow kernel" is proportional to the gradient of the kernel used for kernel density estimation. And the good news is that the shadow of a Gaussian kernel is a Gaussian kernel.  If we choose a "top hat" flat/uniform kernel for the density estimate, we use a Epanichnikov kernel function as an estimate of the gradient. Note: the Comaniciu and Meer paper appears to have a boo-boo cut-and-paste error. Their equation (20) on page 606 says y<sub>j+1</sub> = blah where blah is not a function of y<sub>j</sub>. As the plain x in blah is not defined, I assume this should be y<sub>j</sub>. That formula blah is a cut-and-paste of (17) so likely they forgot to update it.

**Problem**: do we update original data points or use separate particles that shift?

I've been looking at a lot of algorithms and a few bits of code to implement mean shift. The majority of people compute mean shift vectors based upon the means, the "mean" particles that move through the data space. In another words they compute distance from the current mean to X points but then update X for the next iteration. It's unclear to me that it gets the same clusters that a simple gradient ascent on the kernel density estimate would get. It seems like one should keep the density estimate fixed and compute the difference between a "mean particle" that moves around to the *original* data points, not the other particles.  

I looked at [Saravanan Thirumuruganathan's blog](https://saravananthirumuruganathan.wordpress.com/2010/04/01/introduction-to-mean-shift-algorithm/), which fits with my intuition about the algorithm.  Then I noticed that his code actually does do what I would expect.  Quoting the blog here:

1. Fix a window around each data point. 
2. Compute the mean of data within the window. 
3. Shift the window to the mean and repeat till convergence.

Notice that it says it's computing the meaning of the data within the window not the meaning of the means. His Matlab code confirms:

```matlab
	function [origDataPoints,dataPoints] = doMeanShift(dataPoints,useKNNToGetH)
		[numSamples,numFeatures] = size(dataPoints);

		origDataPoints = dataPoints;

		for i = 1:numSamples
			diffBetweenIterations = 10;

			while (diffBetweenIterations > threshold)
				curDataPoint = dataPoints(i,:);
				euclideanDist = sqdist(curDataPoint',origDataPoints');
				bandwidth = getBandWith(origDataPoints(i,:),origDataPoints,euclideanDist,useKNNToGetH);
				kernelDist = exp(-euclideanDist ./ (bandwidth^2));
				numerator = kernelDist * origDataPoints;
				denominator = sum(kernelDist);
				newDataPoint = numerator/denominator;
				dataPoints(i,:) = newDataPoint;
				diffBetweenIterations = abs(curDataPoint - newDataPoint);
			end
		end
		
		[clusterCentroids,pointsToClusters] = getClusters(origDataPoints,dataPoints);
		plotPoints(clusterCentroids,pointsToClusters,origDataPoints);
	end
```

I note that the [formula in this stackexchange.com answer](https://stats.stackexchange.com/questions/61743/understanding-the-mean-shift-algorithm-with-gaussian-kernel) also computes the next particle location using the original data, not the particles.

Ah! In [Mean Shift, Mode Seeking, and Clustering](https://members.loria.fr/MOBerger/Enseignement/Master2/Exposes/meanShiftCluster.pdf), I see that they move the data points, not a separate set of particles. But, they go on to say (T are the particles and S are the data points):

> When T is S, the mean shift algorithm is called a blurring process, indicating the successive blurring of the data set, S. The original mean shift process proposed in [1], [3] is a blurring process, in which T = S. In Definition 2, it is generalized so that T and S may be separate sets with S fixed through the process, although the initial T may be a copy of S.

where [1] is the original work on mean shift [The estimation of the gradient of a density function, with applications in pattern recognition](https://www.researchgate.net/publication/3082499_Hostetler_LD_The_estimation_of_the_gradient_of_a_density_function_with_applications_in_pattern_recognition_IEEE_Transactions_on_Information_Theory_IT-21_32-40) and [3] is [Conceptual clustering in knowledge organization](https://www.ncbi.nlm.nih.gov/pubmed/21869296).

In [A review of mean-shift algorithms for clustering](https://pdfs.semanticscholar.org/399e/00c8a1cc5c3d98d3ce76747d3e0fe57c88f5.pdf), we see: "*Clustering by blurring mean-shift (BMS): smooth the data Here, each point x<sub>m</sub> of the dataset actually moves to the point f(x<sub>m</sub>)*...". <u>It looks like his figure 2 has the blurring on the wrong algorithm. haha.</u> (Actually, it's correct.) Also in that paper the author says:

> As will be shown below, Gaussian BMS [blurred mean shift] can be seen as an iterated filtering (in the signal processing sense) that **eventually leads to a dataset with all points coincident for any starting dataset and bandwidth.** However, before that happens, the dataset quickly collapses into meaningful, tight clusters which depend on σ (see fig. 6), and then these point-like clusters continue to move towards each other relatively slowly.

In other words, the blurred mean shift would *not* converge and stop at the density function maxima. After we think it is found the maxima, we have to artificially stop the iteration.
