/*
 * ***** BEGIN LICENSE BLOCK ***** Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is JTransforms.
 * 
 * The Initial Developer of the Original Code is Piotr Wendykier, Emory
 * University. Portions created by the Initial Developer are Copyright (C)
 * 2007-2009 the Initial Developer. All Rights Reserved.
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or the
 * GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in which
 * case the provisions of the GPL or the LGPL are applicable instead of those
 * above. If you wish to allow use of your version of this file only under the
 * terms of either the GPL or the LGPL, and not to allow others to use your
 * version of this file under the terms of the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and other
 * provisions required by the GPL or the LGPL. If you do not delete the
 * provisions above, a recipient may use your version of this file under the
 * terms of any one of the MPL, the GPL or the LGPL.
 * 
 * ***** END LICENSE BLOCK *****
 */

package edu.emory.mathcs.jtransforms.fft;

// @formatter:off
/**
 * <p>
 * This is a set of utility methods for R/W access to data resulting from a call
 * to the Fourier transform of <em>real</em> data. Memory optimized methods,
 * namely
 * <ul>
 *   <li>{@link DoubleFFT_3D#realForward(double[])}</li>
 *   <li>{@link DoubleFFT_3D#realForward(double[][][])}</li>
 *   <li>{@link FloatFFT_3D#realForward(float[])}</li>
 *   <li>{@link FloatFFT_3D#realForward(float[][][])}</li>
 * </ul>
 * are implemented to handle this case specifically. However, packing of the
 * data in the data array is somewhat obscure. This class provides methods for
 * direct access to the data, without the burden of all necessary tests.
 * </p>
 * <h3>Example for Fourier Transform of real, double precision 1d data</h3>
 * <p>
 * <pre>
 *   DoubleFFT_3D fft = new DoubleFFT_2D(slices, rows, columns);
 *   double[] data = new double[2 * slices * rows * columns];
 *   ...
 *   fft.realForwardFull(data);
 *   data[(s1 * rows + r1) * 2 * columns + c1] = val1;
 *   val2 = data[(s2 * rows + r2) * 2 * columns + c2];
 * </pre>
 * is equivalent to
 * <pre>
 *   DoubleFFT_3D fft = new DoubleFFT_3D(slices, rows, columns);
 *   RealFFTUtils_3D unpacker = new RealFFTUtils_3D(slices, rows, columns);
 *   double[] data = new double[slices * rows * columns];
 *   ...
 *   fft.realForward(data);
 *   unpacker.pack(val1, s1, r1, c1, data);
 *   val2 = unpacker.unpack(s2, r2, c2, data, 0);
 * </pre>
 * Even (resp. odd) values of <code>c</code> correspond to the real (resp.
 * imaginary) part of the Fourier mode.
 * </p>
 * <h3>Example for Fourier Transform of real, double precision 3d data</h3>
 * <p>
 * <pre>
 *   DoubleFFT_3D fft = new DoubleFFT_3D(slices, rows, columns);
 *   double[][][] data = new double[slices][rows][2 * columns];
 *   ...
 *   fft.realForwardFull(data);
 *   data[s1][r1][c1] = val1;
 *   val2 = data[s2][r2][c2];
 * </pre>
 * is equivalent to
 * <pre>
 *   DoubleFFT_3D fft = new DoubleFFT_3D(slices, rows, columns);
 *   RealFFTUtils_3D unpacker = new RealFFTUtils_3D(slices, rows, columns);
 *   double[][][] data = new double[slices][rows][columns];
 *   ...
 *   fft.realForward(data);
 *   unpacker.pack(val1, s1, r1, c1, data);
 *   val2 = unpacker.unpack(s2, r2, c2, data, 0);
 * </pre>
 * Even (resp. odd) values of <code>c</code> correspond to the real (resp.
 * imaginary) part of the Fourier mode.
 * </p>
 * 
 * @author S&eacute;bastien Brisard
 * 
 */
// @formatter:on
public class RealFFTUtils_3D {
    /** The constant <code>int</code> value of 1. */
    private static final int ONE = 1;

    /** The constant <code>int</code> value of 2. */
    private static final int TWO = 2;

    /** The constant <code>int</code> value of 0. */
    private static final int ZERO = 0;

    /** The size of the data in the third direction. */
    private final int columns;

    /** The size of the data in the second direction. */
    private final int rows;

    /** The constant value of <code>2 * columns</code>. */
    private final int rowStride;

    /** The size of the data in the first direction. */
    private final int slices;

    /** The constant value of <code>2 * rows * columns</code>. */
    private final int sliceStride;

    /**
     * Creates a new instance of this class. The size of the underlying
     * {@link DoubleFFT_3D} or {@link FloatFFT_3D} must be specified.
     * 
     * @param slices
     *            number of slices
     * @param rows
     *            number of rows
     * @param columns
     *            number of columns
     */
    public RealFFTUtils_3D(final int slices, final int rows, final int columns) {
        this.slices = slices;
        this.rows = rows;
        this.columns = columns;
        this.rowStride = columns;
        this.sliceStride = rows * this.rowStride;
    }

    /**
     * <p>
     * Returns the 1d index of the specified 3d Fourier mode. In other words, if
     * <code>packed</code> contains the transformed data following a call to
     * {@link DoubleFFT_3D#realForwardFull(double[])} or
     * {@link FloatFFT_3D#realForward(float[])}, then the returned value
     * <code>index</code> gives access to the <code>[s][r][c]</code> Fourier
     * mode
     * <ul>
     * <li>if <code>index == {@link Integer#MIN_VALUE}</code>, then the Fourier
     * mode is zero,</li>
     * <li>if <code>index >= 0</code>, then the Fourier mode is
     * <code>packed[index]</code>,</li>
     * <li>if <code>index < 0</code>, then the Fourier mode is
     * <code>-packed[-index]</code>,</li>
     * </ul>
     * </p>
     * 
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @return the value of <code>index</code>
     */
    public int getIndex(final int s, final int r, final int c) {
        final int cmod2 = c & ONE;
        final int rmul2 = r << ONE;
        final int smul2 = s << ONE;
        final int ss = s == ZERO ? ZERO : slices - s;
        final int rr = r == ZERO ? ZERO : rows - r;
        if (c <= ONE) {
            if (r == ZERO) {
                if (s == ZERO) {
                    return c == ZERO ? ZERO : Integer.MIN_VALUE;
                } else if (smul2 < slices) {
                    return s * sliceStride + c;
                } else if (smul2 > slices) {
                    final int index = ss * sliceStride;
                    return cmod2 == ZERO ? index : -(index + ONE);
                } else {
                    return cmod2 == ZERO ? s * sliceStride : Integer.MIN_VALUE;
                }
            } else if (rmul2 < rows) {
                return s * sliceStride + r * rowStride + c;
            } else if (rmul2 > rows) {
                final int index = ss * sliceStride + rr * rowStride;
                return cmod2 == ZERO ? index : -(index + ONE);
            } else {
                if (s == ZERO) {
                    return cmod2 == ZERO ? r * rowStride : Integer.MIN_VALUE;
                } else if (smul2 < slices) {
                    return s * sliceStride + r * rowStride + c;
                } else if (smul2 > slices) {
                    final int index = ss * sliceStride + r * rowStride;
                    return cmod2 == ZERO ? index : -(index + ONE);
                } else {
                    final int index = s * sliceStride + r * rowStride;
                    return cmod2 == ZERO ? index : Integer.MIN_VALUE;
                }
            }
        } else if (c < columns) {
            return s * sliceStride + r * rowStride + c;
        } else if (c > columns + ONE) {
            final int cc = (columns << ONE) - c;
            final int index = ss * sliceStride + rr * rowStride + cc;
            return cmod2 == ZERO ? index : -(index + TWO);
        } else {
            if (r == ZERO) {
                if (s == ZERO) {
                    return cmod2 == ZERO ? ONE : Integer.MIN_VALUE;
                } else if (smul2 < slices) {
                    final int index = ss * sliceStride;
                    return cmod2 == ZERO ? index + ONE : -index;
                } else if (smul2 > slices) {
                    final int index = s * sliceStride;
                    return cmod2 == ZERO ? index + ONE : index;
                } else {
                    final int index = s * sliceStride;
                    return cmod2 == ZERO ? index + ONE : Integer.MIN_VALUE;
                }
            } else if (rmul2 < rows) {
                final int index = ss * sliceStride + rr * rowStride;
                return cmod2 == ZERO ? index + ONE : -index;
            } else if (rmul2 > rows) {
                final int index = s * sliceStride + r * rowStride;
                return cmod2 == ZERO ? index + ONE : index;
            } else {
                if (s == ZERO) {
                    final int index = r * rowStride + ONE;
                    return cmod2 == ZERO ? index : Integer.MIN_VALUE;
                } else if (smul2 < slices) {
                    final int index = ss * sliceStride + r * rowStride;
                    return cmod2 == ZERO ? index + ONE : -index;
                } else if (smul2 > slices) {
                    final int index = s * sliceStride + r * rowStride;
                    return cmod2 == ZERO ? index + ONE : index;
                } else {
                    final int index = s * sliceStride + r * rowStride;
                    return cmod2 == ZERO ? index + ONE : Integer.MIN_VALUE;
                }
            }
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link DoubleFFT_3D#realForward(double[])}.
     * 
     * @param val
     *            the new value of the <code>[s][r][c]</code> Fourier mode
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     */
    public void pack(final double val, final int s, final int r, final int c,
            final double[] packed, final int pos) {
        final int i = getIndex(s, r, c);
        if (i >= 0) {
            packed[pos + i] = val;
        } else if (i > Integer.MIN_VALUE) {
            packed[pos - i] = -val;
        } else {
            throw new IllegalArgumentException(String.format(
                    "[%d][%d][%d] component cannot be modified (always zero)",
                    s, r, c));
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link DoubleFFT_3D#realForward(double[][][])}.
     * 
     * @param val
     *            the new value of the <code>[s][r][c]</code> Fourier mode
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     */
    public void pack(final double val, final int s, final int r, final int c,
            final double[][][] packed) {
        final int i = getIndex(s, r, c);
        final int ii = Math.abs(i);
        final int ss = ii / sliceStride;
        final int remainder = ii % sliceStride;
        final int rr = remainder / rowStride;
        final int cc = remainder % rowStride;
        if (i >= 0) {
            packed[ss][rr][cc] = val;
        } else if (i > Integer.MIN_VALUE) {
            packed[ss][rr][cc] = -val;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "[%d][%d] component cannot be modified (always zero)",
                            r, c));
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link FloatFFT_3D#realForward(float[])}.
     * 
     * @param val
     *            the new value of the <code>[s][r][c]</code> Fourier mode
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     */
    public void pack(final float val, final int s, final int r, final int c,
            final float[] packed, final int pos) {
        final int i = getIndex(s, r, c);
        if (i >= 0) {
            packed[pos + i] = val;
        } else if (i > Integer.MIN_VALUE) {
            packed[pos - i] = -val;
        } else {
            throw new IllegalArgumentException(String.format(
                    "[%d][%d][%d] component cannot be modified (always zero)",
                    s, r, c));
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link FloatFFT_3D#realForward(float[][][])}.
     * 
     * @param val
     *            the new value of the <code>[s][r][c]</code> Fourier mode
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     */
    public void pack(final float val, final int s, final int r, final int c,
            final float[][][] packed) {
        final int i = getIndex(s, r, c);
        final int ii = Math.abs(i);
        final int ss = ii / sliceStride;
        final int remainder = ii % sliceStride;
        final int rr = remainder / rowStride;
        final int cc = remainder % rowStride;
        if (i >= 0) {
            packed[ss][rr][cc] = val;
        } else if (i > Integer.MIN_VALUE) {
            packed[ss][rr][cc] = -val;
        } else {
            throw new IllegalArgumentException(String.format(
                    "[%d][%d][%d] component cannot be modified (always zero)",
                    s, r, c));
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to {@link DoubleFFT_3D#realForward(double[])}.
     * 
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     * @return the value of the <code>[s][r][c]</code> Fourier mode
     */
    public double unpack(final int s, final int r, final int c,
            final double[] packed, final int pos) {
        final int i = getIndex(s, r, c);
        if (i >= 0) {
            return packed[pos + i];
        } else if (i > Integer.MIN_VALUE) {
            return -packed[pos - i];
        } else {
            return ZERO;
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to
     * {@link DoubleFFT_3D#realForward(double[][][])} .
     * 
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @return the value of the <code>[s][r][c]</code> Fourier mode
     */
    public double unpack(final int s, final int r, final int c,
            final double[][][] packed) {
        final int i = getIndex(s, r, c);
        final int ii = Math.abs(i);
        final int ss = ii / sliceStride;
        final int remainder = ii % sliceStride;
        final int rr = remainder / rowStride;
        final int cc = remainder % rowStride;
        if (i >= 0) {
            return packed[ss][rr][cc];
        } else if (i > Integer.MIN_VALUE) {
            return -packed[ss][rr][cc];
        } else {
            return ZERO;
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to {@link FloatFFT_3D#realForward(float[])} .
     * 
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     * @return the value of the <code>[s][r][c]</code> Fourier mode
     */
    public float unpack(final int s, final int r, final int c,
            final float[] packed, final int pos) {
        final int i = getIndex(s, r, c);
        if (i >= 0) {
            return packed[pos + i];
        } else if (i > Integer.MIN_VALUE) {
            return -packed[pos - i];
        } else {
            return ZERO;
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to {@link FloatFFT_3D#realForward(float[][][])}
     * .
     * 
     * @param s
     *            the slice index
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @return the value of the <code>[s][r][c]</code> Fourier mode
     */
    public float unpack(final int s, final int r, final int c,
            final float[][][] packed) {
        final int i = getIndex(s, r, c);
        final int ii = Math.abs(i);
        final int ss = ii / sliceStride;
        final int remainder = ii % sliceStride;
        final int rr = remainder / rowStride;
        final int cc = remainder % rowStride;
        if (i >= 0) {
            return packed[ss][rr][cc];
        } else if (i > Integer.MIN_VALUE) {
            return -packed[ss][rr][cc];
        } else {
            return ZERO;
        }
    }
}
