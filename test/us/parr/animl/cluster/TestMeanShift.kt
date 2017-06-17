/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import org.junit.Test
import us.parr.animl.BaseTest
import us.parr.animl.data.DoubleVector
import us.parr.lib.ParrtStats

class TestMeanShift : BaseTest() {
    @Test fun testSimple2DSmallLinearClusters(): Unit {
        val n = 10
        val cluster1 = listOf<DoubleVector>(
                DoubleVector(-0.298743,0.799992),
                DoubleVector(-0.299058,0.799996),
                DoubleVector(-0.299372,0.799998),
                DoubleVector(-0.299686,0.8)
        )
        val cluster2 = listOf<DoubleVector>(
                DoubleVector(-0.31,0.79),
                DoubleVector(-0.31,0.789686),
                DoubleVector(-0.309998,0.789372),
                DoubleVector(-0.309996,0.789058)
                )
        val data = cluster1 + cluster2
        val (new_centroids, clusters, k) = meanShift(data, 2.5)
        println("centroids: "+new_centroids.joinToString(separator = "\n"))
    }

    @Test fun testGaussianClouds2Dk2(): Unit {
        val n = 10
        val cluster1: List<DoubleVector> = (1..n).map { DoubleVector(ParrtStats.normal(0.0, 1.0), ParrtStats.normal(0.0, 1.0)) }
        val cluster2: List<DoubleVector> = (1..n).map { DoubleVector(ParrtStats.normal(6.0, 1.5), ParrtStats.normal(4.0, 1.5)) }
        val data = cluster1 + cluster2
        val (new_centroids, clusters) = meanShift(data, 2.5)
        println(new_centroids.joinToString(separator = "\n"))
    }
}
