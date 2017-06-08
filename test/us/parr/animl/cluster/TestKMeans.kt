/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import org.junit.Test
import us.parr.animl.BaseTest
import us.parr.animl.data.DoubleVector
import us.parr.lib.ParrtStats.normal
import kotlin.test.assertEquals

class TestKMeans: BaseTest() {
    @Test fun testGrades1Dk3() : Unit {
        val grades = doubleArrayOf(92.65, 93.87, 74.06, 86.94, 92.26, 94.46, 92.94, 80.65, 92.86,
                85.94, 91.79, 95.23, 85.37, 87.85, 87.71, 93.03)
        val data = grades.map { g -> DoubleVector(g) }
        val means  = doubleArrayOf(90.0, 87.5, 70.0)
        val centroids = means.map { m -> DoubleVector(m) }
        val (new_centroids, clusters) = kmeans(data, centroids, k=3)

        assertEquals("[[93.23222222222222], [85.74333333333334], [74.06]]", new_centroids.toString())
//        println(new_centroids)
//        println(clusters)
    }

    @Test fun testGaussianClouds2Dk2() : Unit {
        val n = 1000
        val cluster1: List<DoubleVector> = (1..n).map { DoubleVector(normal(0.0, 1.0), normal(0.0, 1.0)) }
        val cluster2: List<DoubleVector> = (1..n).map { DoubleVector(normal(6.0, 1.5), normal(4.0, 1.5)) }
        val data = cluster1 + cluster2

        val centroids = mutableListOf<DoubleVector>(DoubleVector(0.1,0.1), DoubleVector(4.0,9.0))
        val (new_centroids, clusters) = kmeans(data, centroids, k=2)
        println(new_centroids)
    }

}