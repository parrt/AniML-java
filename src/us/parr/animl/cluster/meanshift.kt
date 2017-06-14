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
 *  and k
 */
fun meanShift(data : List<DoubleVector>, bandwidth : Double) : Triple<List<DoubleVector>, IntArray, Int> {
    var particles = data.toList() // dup data list
    do { // until we converge
        // update each particle moving over the surface
        val new_particles: List<DoubleVector> = particles.map { p -> shift(p, data, bandwidth) }
        val maximaAsStrings : List<String> = particles.map { p -> p.toString() }
        val uniqueMaxima = maximaAsStrings.toSet()
        val new_maximaAsStrings : List<String> = new_particles.map { p -> p.toString() }
        val uniqueNewMaxima = new_maximaAsStrings.toSet()
        particles = new_particles
    } while ( uniqueMaxima.joinToString(",")!=uniqueNewMaxima.joinToString(",") )

    // Find cluster maxima by finding unique values in particle list, map to 1, 2, 3, ...
    // identify "same" maxima by converting to string with 2 decimal points
    val maximaAsStrings : List<String> = particles.map { p -> p.toString() }
    val uniqueMaxima = maximaAsStrings.toSet().toList()
    val k = uniqueMaxima.size
    println(uniqueMaxima)
    val pointToCluster = maximaAsStrings.map { m -> uniqueMaxima.indexOf(m) }
    println(pointToCluster)
    return Triple(particles,pointToCluster.toIntArray(),k)
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