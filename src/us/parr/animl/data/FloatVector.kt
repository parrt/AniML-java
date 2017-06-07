/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data

import java.lang.Math.abs
import java.lang.Math.max

/** A simple vector to learn some Kotlin */
class FloatVector {
    var elements: kotlin.FloatArray

    constructor(n : Int) {
        elements = kotlin.FloatArray(n)
    }
    constructor(v : FloatVector) {
        elements = v.elements.copyOf()
    }
    constructor(x : List<Number>) {
        elements = kotlin.FloatArray(x.size)
        for (i in elements.indices) {
            elements[i] = x[i].toFloat()
        }
    }

    operator fun get(i : Int) : Float = elements.get(i)

    operator fun set(i : Int, v : Float) { elements[i] = v }

    fun size() = elements.size

    infix fun dot(b:FloatVector) : Double {
        var sum : Double = 0.0
        for(i in elements.indices) {
            sum += elements[i] * b.elements[i]
        }
        return sum
    }

    fun sum() : Double {
        var sum : Double = 0.0
        for(i in elements.indices) {
            sum += elements[i]
        }
        return sum
    }

    operator infix fun plus(b:FloatVector) : FloatVector {
        val r = FloatVector(b)
        for(i in elements.indices) {
            r.elements[i] = elements[i] + b.elements[i]
        }
        return r
    }

    operator infix fun minus(b:FloatVector) : FloatVector {
        val r = FloatVector(b)
        for(i in elements.indices) {
            r.elements[i] = elements[i] - b.elements[i]
        }
        return r
    }

    infix fun map(transform: (Float) -> Float): FloatVector {
        val result = FloatVector(size())
        for (i in this.elements.indices) {
            result.elements[i] = transform(this.elements[i])
        }
        return result
    }

    infix fun isclose(b : FloatVector) : Boolean {
        for (i in elements.indices) {
            if ( !isclose(this[i],b[i]) ) return false
        }
        return true
    }

    override fun toString() = '[' + elements.joinToString(", ") + ']'
}

fun sum(v : FloatVector) = v.sum()

fun mean(v : FloatVector) = v.sum() / v.size()

fun argmin(v : FloatVector) : Int {
    var min_i = -1
    var min_value = Float.MAX_VALUE
    for (i in v.elements.indices) {
        if ( v.elements[i]<min_value ) {
            min_i = i
            min_value = v.elements[i]
        }
    }
    return min_i
}

/** Using logic from https://www.python.org/dev/peps/pep-0485/#proposed-implementation */
fun isclose(a : Float, b : Float) : Boolean {
    val rel_tol=1e-09
    val abs_tol=0.0
    return abs(a-b) <= max(rel_tol * max(abs(a), abs(b)), abs_tol)
}

fun isclose(a : List<FloatVector>, b : List<FloatVector>) : Boolean {
    if ( a.size != b.size ) return false
    for (i in a.indices) {
        if ( !(a[i] isclose b[i]) ) return false
    }
    return true
}

fun main(args: Array<String>) {
    val x = FloatVector(10)
    for (i in 0..9) x.elements[i] = i.toFloat()
    val y = FloatVector(10)
    for (i in 0..9) y.elements[i] = i.toFloat()
    val z = x + y
    println(z)
    println(z - y)
    println(z dot y)
}
