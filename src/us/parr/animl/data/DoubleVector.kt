/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data

import java.math.BigDecimal
import java.math.RoundingMode

val NUM_DECIMALS_TOLERANCE_FOR_EQUALS = 2

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
    constructor(vararg x : Double) {
        elements = x.copyOf()
    }

    /** Two vectors are equal if their elements are isclose() */
    override fun equals(other: Any?): Boolean {
        return other is DoubleVector && this.isclose(other)
    }

    infix fun isclose(other: DoubleVector) : Boolean {
        for (i in elements.indices) {
            if ( !isclose(this[i], other[i]) ) return false
        }
        return true
    }

    override fun hashCode(): Int {
        var hash : Int = 1
        for (element in elements) {
            var rounded = BigDecimal(element)
            rounded = rounded.setScale(NUM_DECIMALS_TOLERANCE_FOR_EQUALS, RoundingMode.HALF_UP)
            val bits = java.lang.Double.doubleToLongBits(rounded.toDouble())
            hash = 31 * hash + (bits xor (bits ushr 32)).toInt()
        }

        return hash
    }

    fun rounded() : DoubleVector {
        val dup = DoubleVector(this)
        dup.round()
        return dup
    }

    /** Round to ndec rounding to nearest "neighbor" */
    fun round(ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) {
        for (i in elements.indices) {
            var d = BigDecimal(elements[i])
            d = d.setScale(ndec, RoundingMode.HALF_UP)
            elements[i] = d.toDouble()
        }
    }

    operator fun get(i : Int) : Double = elements.get(i)

    operator fun set(i : Int, v : Double) { elements[i] = v }

    fun copy() : DoubleVector = DoubleVector(elements.toList())

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

    operator infix fun times(b:Double) : DoubleVector {
        return DoubleVector(elements.map { it * b })
    }

    operator infix fun div(b:Double) : DoubleVector {
        return DoubleVector(elements.map { it / b })
    }

    operator fun unaryMinus() : DoubleVector {
        return DoubleVector(elements.map { -it })
    }

    infix fun map(transform: (Double) -> Double): DoubleVector {
        val result = DoubleVector(size())
        for (i in this.elements.indices) {
            result.elements[i] = transform(this.elements[i])
        }
        return result
    }

    override fun toString() = '[' + elements.joinToString(", ", transform = {e -> String.format("%.2f",e)}) + ']'
}

fun sum(v : DoubleVector) = v.sum()

fun sum(data : List<DoubleVector>) : DoubleVector {
    return data.reduce { s, x -> s + x }
}

fun mean(v : DoubleVector) = v.sum() / v.size()

/** Return L2 euclidean distance between scalars or vectors x and y */
fun euclidean_distance(x : DoubleVector, y : DoubleVector) : Double {
    return Math.sqrt(sum((x - y) map { it * it }))
}

fun norm(x : DoubleVector) : Double {
    return Math.sqrt(sum(x.map { it * it }))
}

/** Using logic from https://www.python.org/dev/peps/pep-0485/#proposed-implementation.
 *  default is that a, b must be same within 9 decimal digits
 */
//fun isclose(a : Double, b : Double, rel_tol : Double = pow(10.0,NUM_DECIMALS_TOLERANCE_FOR_EQUALS.toDouble())) : Boolean {
//    val abs_tol=0.0
//    return Math.abs(a - b) <= Math.max(rel_tol * Math.max(Math.abs(a), Math.abs(b)), abs_tol)
//}

/** Are a and b the same to ndec decimal points? Checks for NaN and Inf equality too. */
fun isclose(a : Double, b : Double, ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) : Boolean {
    if ( a.isNaN() && b.isNaN() ) return true
    if ( a.isInfinite() && b.isInfinite() ) return true
    // ok, we have real (finite) values to compare.
    val abig = BigDecimal(a).setScale(ndec, RoundingMode.HALF_UP)
    val bbig = BigDecimal(b).setScale(ndec, RoundingMode.HALF_UP)
    return abig == bbig
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

/** Take list of p-dimensional data n-vectors and make list of n-dimensional vectors of len n.
 *  Each output list is a column of the data.
 */
fun transpose(data : List<DoubleVector>) : List<DoubleVector> {
    if ( data.isEmpty() ) return emptyList()
    val p = data[0].size() // number of dimensions
    val transposed = List<DoubleVector>(p, init = {DoubleVector(data.size)})
    for (row in data.indices) {
        for (col in 0..p-1) {
            transposed[col][row] = data[row][col]
        }
    }
    return transposed
}

fun main(args: Array<String>) {
    val x = DoubleVector(5.2321, 32.021341)
    val y = DoubleVector(5.2421, 32.02134100003)
    println(x.hashCode())
    println(y.hashCode())
    println(x == y)
}