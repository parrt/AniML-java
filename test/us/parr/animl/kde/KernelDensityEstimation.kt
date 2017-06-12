/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl.kde

/** Return a function mapping a DoubleVector to a density estimate at
 *  that point in multi-dimensional space.
 *
 *  The gaussian kernel bandwidth, h, is computed using Scott's Rule:
 *  n**(-1./(d+4)) for n vectors of d dimensions.
 */
//fun gaussian_kde(data : List<DoubleVector>) : (DoubleVector)->Double {
//}