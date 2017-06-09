/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import us.parr.animl.data.DoubleVector
import us.parr.animl.data.euclidean_distance
import us.parr.animl.data.sum
import us.parr.lib.ParrtStats.sum
import java.lang.Math.exp

/** Mean shift algorithm. Given a list of vectors, return a list of density
 *  estimate maxima and a list of clusters (lists of vectors).
 */
fun meanShift(data : List<DoubleVector>, bandwidth : Double) : Pair<List<DoubleVector>, List<List<DoubleVector>>> {
    var clusters = listOf<List<DoubleVector>>()
    var particles = data.toMutableList()
    repeat(30) {
//    while ( true ) { // until we converge
        // update each particle moving over the surface
        val new_particles = particles.map { shift(it, data, bandwidth) }
        particles = new_particles.toMutableList()
    }
    return Pair(particles,clusters)
}

private fun shift(particle: DoubleVector, data: List<DoubleVector>, bandwidth : Double) : DoubleVector {
    val distances: List<Double> = data.map { x -> euclidean_distance(particle, x) }
    val gauss: List<Double> = distances.map { d -> exp(-d / bandwidth) }
    val weighted_vector = sum(data.mapIndexed { i, x -> x * gauss[i] })
    val normalizing_weight = sum(gauss.toDoubleArray())
    return weighted_vector.map { x -> x / normalizing_weight }
}

fun sum(data : List<Double>) : Double {
    return data.reduce { s, x -> s + x }
}