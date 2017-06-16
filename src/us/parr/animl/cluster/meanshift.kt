/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import us.parr.animl.data.DoubleVector
import us.parr.animl.data.distinct
import us.parr.animl.data.euclidean_distance
import us.parr.animl.data.isclose
import java.lang.Math.*
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Mean shift algorithm. Given a list of vectors, return a list of density
 *  estimate maxima, a mapping of data point index to cluster number 0..k-1,
 *  and k (number of clusters).
 *
 *  The blurred meaning-shift mechanism rapidly converges but it's
 *  harder to tell when to stop iterating (using particle deltas)
 *  because the points will eventually merge together.  My approach is
 *  to use the blurred shift to get good approximations as a head start
 *  for the particles. Then, using the regular mean-shift, iterate more
 *  stably to the cluster maxima. We don't actually need very precise
 *  Maxima estimates because all we really care about is assigning
 *  vectors to clusters. If the maxima are off even by as much as 0.01,
 *  that's probably still good enough to cluster. That said, if the
 *  cluster maxima are very close together, then a higher tolerance should be
 *  used.
 *
 *  Note: you should normalize the range of your features since this function
 *  uses euclidean distance to compute data point density.
 *
 *  Note: From "A review of mean-shift algorithms for clustering" by
 *  Miguel Á. Carreira-Perpińan
 *  https://pdfs.semanticscholar.org/399e/00c8a1cc5c3d98d3ce76747d3e0fe57c88f5.pdf
 *  "KDEs break down in high dimensions ... Indeed, most successful
 *  applications of mean-shift have been in low-dimensional problems,
 *  in particular image segmentation (using a few features
 *  per pixel, such as color in LAB space)"
 */
fun meanShift(data : List<DoubleVector>,
              bandwidth : Double,
              tolerance : Double = 1e-2,
              mergeTolerance : Double = 1e-2)
        : Triple<List<DoubleVector>, IntArray, Int>
{
    val start = System.nanoTime()
    // first use blurred mean-shift with max_blurred_iterations iterations to get faster
    // initial movement of particles. See comments on that method
    var particles = data.toList() // start particles at all data points
    var count = 0
    val max_blurred_iterations = 10
    if (max_blurred_iterations > 0) {
        do {
            count++
            val new_particles: List<DoubleVector> = particles.map { shift(it, particles, bandwidth) }
            println("num distinct particles "+ distinct(particles, 3).size)
            val done = count == max_blurred_iterations ||
                       isclose(particles, new_particles, tolerance = tolerance)
            particles = new_particles
        } while (!done)  // until we converge
//    println("Iterations "+count)
//    println("blurred left on here: "+particles.distinct())
    }

    count = 0
    do {
        count++
        // update each particle, moving towards nearest density maximum
        val new_particles: List<DoubleVector> = particles.map { shift(it, data, bandwidth) }
//        println("distinct particles "+ distinct(particles, 3))
        // Keep refining when particles move by at least tolerance; they slow down as they approach maxima
        val done = isclose(particles, new_particles, tolerance = tolerance)
        particles = new_particles
    } while (!done)  // until we converge
    val stop = System.nanoTime()
    println("Iterations " + count+", time "+(stop-start)/1_000_000+"ms")

    // At this point, particles[i] has converged on maxima for cluster k
    // and the goal is now to assign data[i] to cluster k

    // merge cluster maxima that are within mergeTolerance; unlike the tolerance for
    // continued iteration above, we want to merge maxima that are pretty close.
    return mapVectorsToClusters(particles, data, ndecimals = round(-log10(mergeTolerance)).toInt())
}

fun parallelMeanShift(data : List<DoubleVector>,
                      bandwidth : Double,
                      tolerance : Double = 1e-2,
                      mergeTolerance : Double = 1e-2)
        : Triple<List<DoubleVector>, IntArray, Int>
{
    val n = data.size
    val ncpu = Runtime.getRuntime().availableProcessors()
    val chunkSize = n / ncpu
    val pool = Executors.newFixedThreadPool(ncpu - 1)
    val start = System.nanoTime()

    // first use blurred mean-shift with max_blurred_iterations iterations to get faster
    // initial movement of particles. See comments on that method
    var particles = data.toMutableList() // start particles at all data points
    var count = 0
    val max_blurred_iterations = 10
    if (max_blurred_iterations > 0) {
        // we operate on particles and create new_particles list ala pure functional
        var new_particles: MutableList<DoubleVector> = data.toMutableList()
        do {
            count++
            val jobs = ArrayList<Callable<Unit>>()
            for (i in 0..n-1 step chunkSize) {
                val job = Callable<Unit> {
                    try {
                        val end = min(n-1, i+chunkSize)
//                        println("blurred j = $i..$end")
                        for (j in i..end) {
                            new_particles[j] = shift(particles[j], particles, bandwidth)
                        }
                    }
                    catch (t : Throwable) {
                        t.printStackTrace(System.err)
                    }
                }
                jobs.add(job)
            }
            pool.invokeAll<Unit>(jobs)

//            val new_particles: List<DoubleVector> = particles.map { shift(it, particles, bandwidth) }
            println("num distinct particles "+ distinct(new_particles, 3).size)
            val done = count == max_blurred_iterations ||
                       isclose(particles, new_particles, tolerance = tolerance)
            // We can't point particles at new_particles (same list) since then we'd be
            // updating the particles as we compute density from them during parallel computation
            particles = new_particles.toMutableList() // dup
        } while (!done)  // until we converge
        println("Iterations "+count)
//    println("blurred left on here: "+particles.distinct())
    }

    count = 0
    // we operate on particles and create new_particles list ala pure functional
    var new_particles: MutableList<DoubleVector> = data.toMutableList()
    do {
        count++
        // update each particle, moving towards nearest density maximum
        val jobs = ArrayList<Callable<Unit>>()
        for (i in 0..n-1 step chunkSize) {
            val job = Callable<Unit> {
                try {
                    val end = min(n-1, i+chunkSize)
//                    println("j = $i..$end")
                    for (j in i..end) {
                        new_particles[j] = shift(particles[j], data, bandwidth)
                    }
                }
                catch (t : Throwable) {
                    t.printStackTrace(System.err)
                }
            }
            jobs.add(job)
        }
        pool.invokeAll<Unit>(jobs)

        println("distinct particles "+ distinct(particles, 3).size)
        // Keep refining when particles move by at least tolerance; they slow down as they approach maxima
        val done = isclose(particles, new_particles, tolerance = tolerance)
        particles = new_particles.toMutableList() // dup
    } while (!done)  // until we converge
    val stop = System.nanoTime()
    println("Iterations " + count+", time "+(stop-start)/1_000_000+"ms")

    pool.shutdown()
    pool.awaitTermination(60, TimeUnit.MINUTES)

    // At this point, particles[i] has converged on maxima for cluster k
    // and the goal is now to assign data[i] to cluster k

    // merge cluster maxima that are within mergeTolerance; unlike the tolerance for
    // continued iteration above, we want to merge maxima that are pretty close.
    return mapVectorsToClusters(particles, data, ndecimals = round(-log10(mergeTolerance)).toInt())
}

/** Blurred mean shift algorithm that computes density on particles in
 *  motion not the static original data points. This converges much faster
 *  than vanilla mean-shift by an order of magnitude since the particles
 *  are collapsing together into a "gravity well."
 *
 *  From "A review of mean-shift algorithms for clustering" by
 *  Miguel Á. Carreira-Perpińan
 *  https://pdfs.semanticscholar.org/399e/00c8a1cc5c3d98d3ce76747d3e0fe57c88f5.pdf
 *
 *  "Gaussian BMS can be seen as an iterated filtering
 *   (in the signal processing sense) that eventually leads to a dataset
 *   with all points coincident for any starting dataset and bandwidth.
 *   However, before that happens, the dataset quickly collapses into
 *   meaningful, tight clusters which depend on [bandwidth] sigma."
 *
 *  So we terminate iteration when particles are within tolerance of each other or
 *  when we hit arg iterations.
 *
 *  Given a list of vectors, return a list of density
 *  estimate maxima, a mapping of data point index to cluster number 0..k-1,
 *  and k (number of clusters). It is the same as meanShift except that
 *  we compute density based upon the particles as we move around, not the
 *  original data set.
 *
 *  Note: you should normalize the range of your features since this function
 *  uses euclidean distance to compute data point density.
 */
fun blurredMeanShift(data : List<DoubleVector>,
                     bandwidth : Double,
                     tolerance : Double = 1e-2,
                     mergeTolerance : Double = 1e-2,
                     iterations : Int = 30)
        : Triple<List<DoubleVector>, IntArray, Int>
{
    var particles = data.toList() // start particles at all data points
    var count = 0
    do {
        count++
        // update each particle, moving towards nearest density maximum
        val new_particles: List<DoubleVector> = particles.map { shift(it, particles, bandwidth) }
        println("${particles.distinct().size} ${new_particles.distinct().size}")
        // Iterate only until numbers match up to 3 decimals; even 4 seems
        // to be too high of a tolerance. It keeps iterating very slowly
        // shifting one or more clusters
        val done = count==iterations ||
                   isclose(particles, new_particles, tolerance = tolerance)
        particles = new_particles
    } while ( !done )  // until we converge
    println("Iterations "+count)

    // At this point, particles[i] has converged on maxima for cluster k
    // and the goal is now to assign data[i] to cluster k

    return mapVectorsToClusters(particles, data, ndecimals = round(-log10(mergeTolerance)).toInt())
}

/** Return and shift in particle that is the weighted mean relative to
 *  the particle.
 *
 *  The weighted mean of particle vector within data set is the
 *
 *  Sum over i gaussian(x_i - particle) * x_i
 *  ----------------------------------
 *  Sum over i gaussian(x_i - particle)
 *
 *  For example, if we replace the Gaussian with 1, then we
 *  our computing the unweighted centroid of all data points.
 *  (Not a good idea of course but illustrates that is just a
 *  weighted average where the distance falls off to zero as you
 *  move away from the particle.
 *
 *  We compute this all in a single loop over the data for efficiency.
 */
private fun shift(particle: DoubleVector, data: List<DoubleVector>, bandwidth : Double) : DoubleVector {
    var normalizing_weight = 0.0
    var weighted_vector = DoubleVector(particle.size())
    data.forEach {
        val d = gaussianKernel(euclidean_distance(particle, it), bandwidth)
        normalizing_weight += d
        weighted_vector += it * d
    }
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
//    println(Arrays.toString(pointToCluster))
    return Triple(uniqueMaxima.toList(), pointToCluster, k)
}

private fun gaussianKernel(d: Double, bandwidth: Double)
        = exp(-0.5 * pow(d / bandwidth, 2.0))// / (bandwidth * sqrt(2 * PI))

fun sum(data : List<Double>) : Double {
    return data.reduce { s, x -> s + x }
}

