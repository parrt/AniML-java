/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import us.parr.animl.data.DoubleVector
import us.parr.animl.data.euclidean_distance
import us.parr.animl.data.sum
import java.lang.Math.*

/** Mean shift algorithm. Given a list of vectors, return a list of density
 *  estimate maxima, a mapping of data point index to cluster number 0..k-1,
 *  and k (number of clusters).
 *
 *  Note: you should normalize the range of your features since this function
 *  uses euclidean distance to compute data point density.
 */
fun meanShift(data : List<DoubleVector>, bandwidth : Double) : Triple<List<DoubleVector>, IntArray, Int> {
    var particles = data.toList() // start particles at all data points
    do {
        // update each particle, moving towards nearest density maximum
        val new_particles: List<DoubleVector> = particles.map { shift(it, data, bandwidth) }
        val done = particles == new_particles
        particles = new_particles
    } while ( !done )  // until we converge

    // At this point, particles[i] has converged on maxima for cluster k
    // and the goal is now to assign data[i] to cluster k

    // Maximas are unique values in particle list
    val uniqueMaxima: List<DoubleVector> = particles.distinct()
    val k = uniqueMaxima.size
    // Map those maxima to cluster numbers 0, 1, 2, 3, ...
    val maximaToClusterMap = mutableMapOf<DoubleVector, Int>()
    var cluster = 0
    uniqueMaxima.forEach { maximaToClusterMap[it] = cluster++ }

    val pointToCluster = IntArray(data.size)
    for (i in data.indices) {
        pointToCluster[i] = maximaToClusterMap.getOrDefault(particles[i],-1)
    }
    return Triple(uniqueMaxima,pointToCluster,k)
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

private fun gaussianKernel(d: Double, bandwidth: Double)
    = exp(-0.5 * pow(d / bandwidth, 2.0)) / (bandwidth * sqrt(2 * PI))

fun sum(data : List<Double>) : Double {
    return data.reduce { s, x -> s + x }
}

