/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data

class FloatVector(n : Int) {
    var data : FloatArray = kotlin.FloatArray(n)

    infix fun dot(b:FloatVector) : Double {
        var sum : Double = 0.0
        for(i in data.indices) {
            sum += data[i] * b.data[i]
        }
        return sum
    }
}

fun main(args: Array<String>) {
    val x = FloatVector(10)
    for (i in 0..9) x.data[i] = i.toFloat()
    val y = FloatVector(10)
    for (i in 0..9) y.data[i] = i.toFloat()
    val z = x dot y
    print(z)
}
