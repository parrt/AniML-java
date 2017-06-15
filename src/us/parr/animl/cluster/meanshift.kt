/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import us.parr.animl.data.*
import java.lang.Math.*
import java.util.*

/** Mean shift algorithm. Given a list of vectors, return a list of density
 *  estimate maxima, a mapping of data point index to cluster number 0..k-1,
 *  and k (number of clusters).
 *
 *  Note: you should normalize the range of your features since this function
 *  uses euclidean distance to compute data point density.
 */
fun meanShift(data : List<DoubleVector>, bandwidth : Double) : Triple<List<DoubleVector>, IntArray, Int> {
    var particles = data.toList() // start particles at all data points
    var iterations = 0
    do {
        iterations++
        // update each particle, moving towards nearest density maximum
        val new_particles: List<DoubleVector> = particles.map { shift(it, data, bandwidth) }
        // Keep refiningwhen particles move even a little bit; they slow down as they approach maxima
        val done = isclose(particles, new_particles, tolerance = 1e-6)
        particles = new_particles
    } while ( !done )  // until we converge
    println("Iterations "+iterations)

    // At this point, particles[i] has converged on maxima for cluster k
    // and the goal is now to assign data[i] to cluster k

    // merge cluster maxima that are within 0.001; unlike the tolerance for
    // continued iteration above, we want to merge maxima that are pretty close.
    // ndecimals could be a parameter I guess
    return mapVectorsToClusters(particles, data, ndecimals = 3)
}

/** Mean shift algorithm. Given a list of vectors, return a list of density
 *  estimate maxima, a mapping of data point index to cluster number 0..k-1,
 *  and k (number of clusters). It is the same as meanShift except that
 *  we compute density based upon the particles as we move around, not the
 *  original data set.
 *
 *  Note: you should normalize the range of your features since this function
 *  uses euclidean distance to compute data point density.
 */
fun blurredMeanShift(data : List<DoubleVector>, bandwidth : Double) : Triple<List<DoubleVector>, IntArray, Int> {
    var particles = data.toList() // start particles at all data points
    var iterations = 0
    do {
        iterations++
        // update each particle, moving towards nearest density maximum
        val new_particles: List<DoubleVector> = particles.map { shift(it, particles, bandwidth) }
        println("${particles.distinct().size} ${new_particles.distinct().size}")
        val done = isclose(particles, new_particles, 1e04)
        particles = new_particles
    } while ( !done )  // until we converge
    println("Iterations "+iterations)

    // At this point, particles[i] has converged on maxima for cluster k
    // and the goal is now to assign data[i] to cluster k

    return mapVectorsToClusters(particles, data, 4)
}



private fun shift(particle: DoubleVector, data: List<DoubleVector>, bandwidth : Double) : DoubleVector {
    // Compute distance divided by standard deviation
    val distances: List<Double> = data.map { x -> euclidean_distance(particle, x) }
    val gauss:     List<Double> = distances.map { d -> gaussianKernel(d, bandwidth) }
    // Weight each data point per its proximity using gaussian kernel
    var gradient = mutableListOf<DoubleVector>()
    for (i in data.indices) {
        gradient.add(data[i] * gauss[i])
    }
    val weighted_vector    = sum(gradient)
    val normalizing_weight = sum(gauss)
    return weighted_vector.map { x -> x / normalizing_weight }
}

private fun mapVectorsToClusters(particles: List<DoubleVector>, data: List<DoubleVector>, ndecimals : Int)
    : Triple<List<DoubleVector>, IntArray, Int>
{
    // Maximas are unique values in particle list
    val uniqueMaxima: Set<DoubleVector> = distinct(particles, ndecimals)
    val k = uniqueMaxima.size
    // Map those maxima to cluster numbers 0, 1, 2, 3, ...
    val maximaToClusterMap = mutableMapOf<DoubleVector, Int>()
    var cluster = 0
    uniqueMaxima.forEach { maximaToClusterMap[it] = cluster++ }

    val pointToCluster = IntArray(data.size)
    for (i in data.indices) {
        pointToCluster[i] = maximaToClusterMap.getOrDefault(particles[i].rounded(ndecimals), -1)
    }
    println(uniqueMaxima)
    println(Arrays.toString(pointToCluster))
    return Triple(uniqueMaxima.toList(), pointToCluster, k)
}

private fun gaussianKernel(d: Double, bandwidth: Double)
    = exp(-0.5 * pow(d / bandwidth, 2.0)) / (bandwidth * sqrt(2 * PI))

fun sum(data : List<Double>) : Double {
    return data.reduce { s, x -> s + x }
}

