/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data

class DoubleVector {
    var elements: kotlin.DoubleArray

    constructor(n : Int) {
        elements = kotlin.DoubleArray(n)
    }
    constructor(v : DoubleVector) {
        elements = v.elements.copyOf()
    }
    constructor(x : List<Number>) {
        elements = kotlin.DoubleArray(x.size)
        for (i in elements.indices) {
            elements[i] = x[i].toDouble()
        }
    }
    constructor(x : DoubleArray) {
        elements = x.copyOf()
    }

    operator fun get(i : Int) : Double = elements.get(i)

    operator fun set(i : Int, v : Double) { elements[i] = v }

    fun size() = elements.size

    infix fun dot(b:DoubleVector) : Double {
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

    operator infix fun plus(b:DoubleVector) : DoubleVector {
        val r = DoubleVector(b)
        for(i in elements.indices) {
            r.elements[i] = elements[i] + b.elements[i]
        }
        return r
    }

    operator infix fun minus(b:DoubleVector) : DoubleVector {
        val r = DoubleVector(b)
        for(i in elements.indices) {
            r.elements[i] = elements[i] - b.elements[i]
        }
        return r
    }

    infix fun map(transform: (Double) -> Double): DoubleVector {
        val result = DoubleVector(size())
        for (i in this.elements.indices) {
            result.elements[i] = transform(this.elements[i])
        }
        return result
    }

    infix fun isclose(b : DoubleVector) : Boolean {
        for (i in elements.indices) {
            if ( !isclose(this[i],b[i]) ) return false
        }
        return true
    }

    override fun toString() = '[' + elements.joinToString(", ") + ']'
}

fun sum(v : DoubleVector) = v.sum()

fun mean(v : DoubleVector) = v.sum() / v.size()

/** Using logic from https://www.python.org/dev/peps/pep-0485/#proposed-implementation */
fun isclose(a : Double, b : Double) : Boolean {
    val rel_tol=1e-09
    val abs_tol=0.0
    return Math.abs(a - b) <= Math.max(rel_tol * Math.max(Math.abs(a), Math.abs(b)), abs_tol)
}

fun isclose(a : List<DoubleVector>, b : List<DoubleVector>) : Boolean {
    if ( a.size != b.size ) return false
    for (i in a.indices) {
        if ( !(a[i] isclose b[i]) ) return false
    }
    return true
}

fun argmin(v : DoubleVector) : Int {
    var min_i = -1
    var min_value = Double.MAX_VALUE
    for (i in v.elements.indices) {
        if ( v.elements[i]<min_value ) {
            min_i = i
            min_value = v.elements[i]
        }
    }
    return min_i
}