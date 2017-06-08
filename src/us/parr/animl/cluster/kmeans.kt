/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */
package us.parr.animl.cluster

import us.parr.animl.data.*

/** Given a list of vectors, initial centroids, and number of desired clusters k,
 *  return a list of k centroids and a list of clusters (lists of vectors).
 */
fun kmeans(data : List<DoubleVector>, initialCentroids: List<DoubleVector>, k : Int)
        : Pair<List<DoubleVector>, List<List<DoubleVector>>>
{
    var prev_centroids = initialCentroids
    var centroids = initialCentroids
    var clusters : List<List<DoubleVector>>
    do {
        prev_centroids = centroids
        clusters = `reassign vectors to clusters`(data, centroids, ::euclidean_distance)
        centroids = clusters.map(::centroid)
    } while ( !isclose(prev_centroids, centroids) )
    return Pair(centroids,clusters)
}

/** Assign all vectors in data to a cluster associated with a centroid.
 *  Return the list of clusters.
 */
private fun `reassign vectors to clusters`(data : List<DoubleVector>,
                                           centroids : List<DoubleVector>,
                                           distance : (DoubleVector,DoubleVector) -> Double)
        : List<List<DoubleVector>>
{
    val k = centroids.size
    var clusters = List<MutableList<DoubleVector>>(k, init = {mutableListOf()})
    for (v in data) {
        val i = closestVector(centroids, v, distance)
        clusters[i].add(v)
    }
    return clusters
}

/** Given a list of vectors, return a vector that is the mean of all vectors.
 *  The ith value of result is the mean of ith column, data[][i], for
 *  all vectors.
 */
private fun centroid(data : List<DoubleVector>) : DoubleVector {
    val colIndices = data[0].elements.indices
    val ncols = colIndices.count()
    val centroid = DoubleVector(ncols)
    for (i in colIndices) {
        val col : List<Double> = data.map { v -> v[i] }
        centroid[i] = mean(DoubleVector(col))
    }
    return centroid
}

/** Return index of vector in data closest to x */
private fun closestVector(data : List<DoubleVector>, x : DoubleVector, distance : (DoubleVector,DoubleVector) -> Double) : Int {
    val distances : List<Double> = data.map {v -> distance(x,v)}
    return argmin(DoubleVector(distances))
}
