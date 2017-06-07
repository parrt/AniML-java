/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */
package us.parr.animl.cluster

import us.parr.animl.data.*

fun kmeans(data : List<DoubleVector>, initialCentroids: List<DoubleVector>, k : Int)
        : Pair<List<DoubleVector>, List<List<DoubleVector>>>
{
    var prev_centroids = initialCentroids
    var centroids = initialCentroids//listOf<DoubleVector>()
    var clusters : List<List<DoubleVector>> //(k, init = {listOf<DoubleVector>()})
    do {
        prev_centroids = centroids
        clusters = assign(data, centroids, ::euclidean_distance)
        centroids = clusters.map(::centroid)// { cluster -> centroid(cluster) }
    } while ( !isclose(prev_centroids, centroids) )
    return Pair(centroids,clusters)
}

/** Assign all vectors in data to a cluster associated with a centroid.
 *  Return the list of clusters.
 */
fun assign(data : List<DoubleVector>,
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
fun centroid(data : List<DoubleVector>) : DoubleVector {
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
fun closestVector(data : List<DoubleVector>, x : DoubleVector, distance : (DoubleVector,DoubleVector) -> Double) : Int {
    val distances : List<Double> = data.map {v -> distance(x,v)}
    return argmin(DoubleVector(distances))
}

/** Return L2 euclidean distance between scalars or vectors x and y */
fun euclidean_distance(x : DoubleVector, y : DoubleVector) : Double {
    return Math.sqrt(sum((x - y) map { it * it }))
}

fun main(args: Array<String>) {
    val grades = doubleArrayOf(92.65, 93.87, 74.06, 86.94, 92.26, 94.46, 92.94, 80.65, 92.86,
            85.94, 91.79, 95.23, 85.37, 87.85, 87.71, 93.03)
    val data = mutableListOf<DoubleVector>()
    for (g in grades) {
        val v = DoubleVector(1)
        v[0] = g
        data.add(v)
    }
    val means = doubleArrayOf(90.0, 80.0, 70.0)
    val centroids = mutableListOf<DoubleVector>()
    for (mean in means) {
        val v = DoubleVector(1)
        v[0] = mean
        centroids.add(v)
    }
    val k = 3
    val (new_centroids, clusters) = kmeans(data, centroids, k)
    print(new_centroids)
}