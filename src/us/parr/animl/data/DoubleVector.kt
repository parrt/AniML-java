/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.data

import java.math.BigDecimal
import java.math.RoundingMode

val NUM_DECIMALS_TOLERANCE_FOR_EQUALS = 9

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
    override fun equals(other: Any?): Boolean = equals(other, 1e-9)

    fun equals(other: Any?, tolerance: Double): Boolean {
        return other is DoubleVector &&
                this.dims()==other.dims() &&
                isclose(this, other, tolerance)
    }

//    fun isclose(other: DoubleVector, ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) : Boolean {
//        for (i in elements.indices) {
//            if ( !isclose(this[i], other[i], ndec) ) return false
//        }
//        return true
//    }


    /** Hash of this vector is derived from element values rounded to ndec decimal places */
    override fun hashCode(): Int = hashCode(NUM_DECIMALS_TOLERANCE_FOR_EQUALS)

    fun hashCode(ndec : Int): Int {
        var hash : Int = 1
        for (element in elements) {
            var rounded = BigDecimal(element)
            rounded = rounded.setScale(ndec, RoundingMode.HALF_UP)
            val bits = java.lang.Double.doubleToLongBits(rounded.toDouble())
            hash = 31 * hash + (bits xor (bits ushr 32)).toInt()
        }

        return hash
    }

    fun rounded(ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) : DoubleVector {
        val dup = DoubleVector(this)
        dup.round(ndec)
        return dup
    }

    /** Round to ndec decimals rounding to nearest "neighbor" */
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

    fun dims() = elements.size

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

    fun abs() : DoubleVector {
        return DoubleVector(elements.map { Math.abs(it) })
    }

    infix fun map(transform: (Double) -> Double): DoubleVector {
        val result = DoubleVector(dims())
        for (i in this.elements.indices) {
            result.elements[i] = transform(this.elements[i])
        }
        return result
    }

    fun toString(ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS) =
        '[' +
            elements.joinToString(", ",
            transform = {
                e -> BigDecimal(e).setScale(ndec,RoundingMode.HALF_UP).toString()
            }
            ) +
        ']'

    override fun toString() = toString(NUM_DECIMALS_TOLERANCE_FOR_EQUALS)
}

fun sum(v : DoubleVector) = v.sum()

fun sum(data : List<DoubleVector>) : DoubleVector {
    return data.reduce { s, x -> s + x }
}

fun mean(v : DoubleVector) = v.sum() / v.dims()

/** Return L2 euclidean distance between scalars or vectors x and y */
fun euclidean_distance(x : DoubleVector, y : DoubleVector) : Double {
//    var sum : Double = 0.0
//    for (i in x.elements.indices) { // for each dimension
//        val d = x[i]-y[i]
//        sum += d*d
//    }
    return Math.sqrt(sum((x - y) map { it * it }))
}

fun norm(x : DoubleVector) : Double {
    return Math.sqrt(sum(x.map { it * it }))
}

/** Default is that a, b must be same within about 9 decimal digits.
 *  Handles NaN and Inf cases.
 */
fun isclose(a : Double, b : Double, tolerance: Double = 1e-9) : Boolean {
    if ( a.isNaN() ) return b.isNaN()
    if ( a.isInfinite() ) return b.isInfinite()
    val close = Math.abs(a - b) <= tolerance
//    println("$a==$b is ${close} with tolerance $tolerance")
    return close
}

/** Are a and b the same to ndec decimal points? Checks for NaN and Inf equality too. */
//fun isclose(a : Double, b : Double, tolerance: Double = 1e-9) : Boolean {
//    if ( a.isNaN() && b.isNaN() ) return true
//    if ( a.isInfinite() && b.isInfinite() ) return true
//    // ok, we have real (finite) values to compare.
//    val abig = BigDecimal(a).setScale(ndec, RoundingMode.HALF_UP)
//    val bbig = BigDecimal(b).setScale(ndec, RoundingMode.HALF_UP)
//    println("$abig==$bbig is ${abig==bbig} with scale $ndec")
//    return abig == bbig
//}

fun isclose(a : DoubleVector, b: DoubleVector, tolerance: Double = 1e-9) : Boolean {
    if ( a.dims() != b.dims() ) return false
    for (i in a.elements.indices) {
        if ( !isclose(a[i], b[i], tolerance) ) return false
    }
    return true
}

fun isclose(a : List<DoubleVector>, b : List<DoubleVector>, tolerance: Double = 1e-9) : Boolean {
    if ( a.size != b.size ) return false
    for (i in a.indices) {
        if ( !(isclose(a[i], b[i], tolerance)) ) return false
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
    val p = data[0].dims() // number of dimensions
    val transposed = List<DoubleVector>(p, init = {DoubleVector(data.size)})
    for (row in data.indices) {
        for (col in 0..p-1) {
            transposed[col][row] = data[row][col]
        }
    }
    return transposed
}

fun distinct(data : List<DoubleVector>, ndec : Int = NUM_DECIMALS_TOLERANCE_FOR_EQUALS)
    : Set<DoubleVector>
{
    val uniq = mutableSetOf<DoubleVector>()
    for (v in data) {
        uniq.add(v.rounded(ndec))
    }
    return uniq
}

fun main(args: Array<String>) {
    val x = DoubleVector(5.2321, 32.021341)
    val y = DoubleVector(5.2421, 32.02134100003)
    println(x.hashCode())
    println(y.hashCode())
    println(x == y)
}