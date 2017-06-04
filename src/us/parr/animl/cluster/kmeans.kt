/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */
package us.parr.animl.cluster

import us.parr.animl.data.*

fun kmeans(data : List<FloatVector>, initialCentroids: List<FloatVector>, k : Int)
        : Pair<List<FloatVector>, List<List<FloatVector>>>
{
    var prev_centroids = initialCentroids
    var centroids = initialCentroids//listOf<FloatVector>()
    var clusters : List<List<FloatVector>> //(k, init = {listOf<FloatVector>()})
    do {
        prev_centroids = centroids
        clusters = assign(data, centroids, ::euclidean_distance)
        centroids = clusters.map { cluster -> centroid(cluster) }
    } while ( !isclose(prev_centroids, centroids) )
    return Pair(centroids,clusters)
}

/** Assign all vectors in data to a cluster associated with a centroid.
 *  Return the list of clusters.
 */
fun assign(data : List<FloatVector>,
           centroids : List<FloatVector>,
           distance : (FloatVector,FloatVector) -> Double)
        : List<List<FloatVector>>
{
    val k = centroids.size
    var clusters = List<MutableList<FloatVector>>(k, init = {mutableListOf()})
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
fun centroid(data : List<FloatVector>) : FloatVector {
    val c = FloatVector(data.size)
    for (i in data[0].elements.indices) {
        val col : List<Float> = data.map { v -> v[i] }
        c[i] = mean(FloatVector(col)).toFloat()
    }
    return c
}

/** Return index of vector in data closest to x */
fun closestVector(data : List<FloatVector>, x : FloatVector, distance : (FloatVector,FloatVector) -> Double) : Int {
    val distances : List<Double> = data.map {v -> distance(x,v)}
    return argmin(FloatVector(distances))
}

/** Return L2 euclidean distance between scalars or vectors x and y */
fun euclidean_distance(x : FloatVector, y : FloatVector) : Double {
    return Math.sqrt(sum(x minus y map { it * it }))
}

fun main(args: Array<String>) {
    val grades = doubleArrayOf(92.65, 93.87, 74.06, 86.94, 92.26, 94.46, 92.94, 80.65, 92.86,
            85.94, 91.79, 95.23, 85.37, 87.85, 87.71, 93.03)
    val data = mutableListOf<FloatVector>()
    for (grade in grades) {
        val v = FloatVector(1)
        v[0] = grade.toFloat()
        data.add(v)
    }
    val means = doubleArrayOf(90.0, 80.0, 70.0)
    val centroids = mutableListOf<FloatVector>()
    for (mean in means) {
        val v = FloatVector(1)
        v[0] = mean.toFloat()
        centroids.add(v)
    }
    val k = 3
    val (new_centroids, clusters) = kmeans(data, centroids, k)
    print(new_centroids)
}