/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.cluster

import org.junit.Test
import us.parr.animl.BaseTest
import us.parr.animl.data.DoubleVector
import kotlin.test.assertEquals

class TestKMeans: BaseTest() {
    @Test fun foo() : Unit {
        val grades = doubleArrayOf(92.65, 93.87, 74.06, 86.94, 92.26, 94.46, 92.94, 80.65, 92.86,
                85.94, 91.79, 95.23, 85.37, 87.85, 87.71, 93.03)
        val data = mutableListOf<DoubleVector>()
        for (grade in grades) {
            val v = DoubleVector(1)
            v[0] = grade
            data.add(v)
        }
        val means = doubleArrayOf(90.0, 87.5, 70.0)
        val centroids = mutableListOf<DoubleVector>()
        for (mean in means) {
            val v = DoubleVector(1)
            v[0] = mean
            centroids.add(v)
        }
        val k = 3
        val (new_centroids, clusters) = kmeans(data, centroids, k)

        assertEquals("[[93.23222222222222], [85.74333333333334], [74.06]]", new_centroids.toString())
//        println(new_centroids)
//        println(clusters)
    }
}