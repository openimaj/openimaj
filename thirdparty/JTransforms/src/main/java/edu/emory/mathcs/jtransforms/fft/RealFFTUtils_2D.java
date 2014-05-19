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
 *   <li>{@link DoubleFFT_2D#realForward(double[])}</li>
 *   <li>{@link DoubleFFT_2D#realForward(double[][])}</li>
 *   <li>{@link FloatFFT_2D#realForward(float[])}</li>
 *   <li>{@link FloatFFT_2D#realForward(float[][])}</li>
 * </ul>
 * are implemented to handle this case specifically. However, packing of the
 * data in the data array is somewhat obscure. This class provides methods for
 * direct access to the data, without the burden of all necessary tests.
 * </p>
 * <h3>Example for Fourier Transform of real, double precision 1d data</h3>
 * <p>
 * <pre>
 *   DoubleFFT_2D fft = new DoubleFFT_2D(rows, columns);
 *   double[] data = new double[2 * rows * columns];
 *   ...
 *   fft.realForwardFull(data);
 *   data[r1 * 2 * columns + c1] = val1;
 *   val2 = data[r2 * 2 * columns + c2];
 * </pre>
 * is equivalent to
 * <pre>
 *   DoubleFFT_2D fft = new DoubleFFT_2D(rows, columns);
 *   RealFFTUtils_2D unpacker = new RealFFTUtils_2D(rows, columns);
 *   double[] data = new double[rows * columns];
 *   ...
 *   fft.realForward(data);
 *   unpacker.pack(val1, r1, c1, data);
 *   val2 = unpacker.unpack(r2, c2, data, 0);
 * </pre>
 * Even (resp. odd) values of <code>c</code> correspond to the real (resp.
 * imaginary) part of the Fourier mode.
 * </p>
 * <h3>Example for Fourier Transform of real, double precision 2d data</h3>
 * <p>
 * <pre>
 *   DoubleFFT_2D fft = new DoubleFFT_2D(rows, columns);
 *   double[][] data = new double[rows][2 * columns];
 *   ...
 *   fft.realForwardFull(data);
 *   data[r1][c1] = val1;
 *   val2 = data[r2][c2];
 * </pre>
 * is equivalent to
 * <pre>
 *   DoubleFFT_2D fft = new DoubleFFT_2D(rows, columns);
 *   RealFFTUtils_2D unpacker = new RealFFTUtils_2D(rows, columns);
 *   double[][] data = new double[rows][columns];
 *   ...
 *   fft.realForward(data);
 *   unpacker.pack(val1, r1, c1, data);
 *   val2 = unpacker.unpack(r2, c2, data, 0);
 * </pre>
 * Even (resp. odd) values of <code>c</code> correspond to the real (resp.
 * imaginary) part of the Fourier mode.
 * </p>
 * 
 * @author S&eacute;bastien Brisard
 * 
 */
// @formatter:on
public class RealFFTUtils_2D {
    /** The constant <code>int</code> value of 1. */
    private static final int ONE = 1;

    /** The constant <code>int</code> value of 2. */
    private static final int TWO = 2;

    /** The constant <code>int</code> value of 0. */
    private static final int ZERO = 0;

    /** The size of the data in the second direction. */
    private final int columns;

    /** The size of the data in the first direction. */
    private final int rows;

    /**
     * Creates a new instance of this class. The size of the underlying
     * {@link DoubleFFT_2D} or {@link FloatFFT_2D} must be specified.
     * 
     * @param rows
     *            number of rows
     * @param columns
     *            number of columns
     */
    public RealFFTUtils_2D(final int rows, final int columns) {
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * <p>
     * Returns the 1d index of the specified 2d Fourier mode. In other words, if
     * <code>packed</code> contains the transformed data following a call to
     * {@link DoubleFFT_2D#realForward(double[])} or
     * {@link FloatFFT_2D#realForward(float[])}, then the returned value
     * <code>index</code> gives access to the <code>[r][c]</code> Fourier mode
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
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @return the value of <code>index</code>
     */
    public int getIndex(final int r, final int c) {
        final int cmod2 = c & ONE;
        final int rmul2 = r << ONE;
        if (r != ZERO) {
            if (c <= ONE) {
                if (rmul2 == rows) {
                    if (cmod2 == ONE) {
                        return Integer.MIN_VALUE;
                    }
                    return ((rows * columns) >> ONE);
                } else if (rmul2 < rows) {
                    return columns * r + cmod2;
                } else {
                    if (cmod2 == ZERO) {
                        return columns * (rows - r);
                    } else {
                        return -(columns * (rows - r) + ONE);
                    }
                }
            } else if ((c == columns) || (c == columns + ONE)) {
                if (rmul2 == rows) {
                    if (cmod2 == ONE) {
                        return Integer.MIN_VALUE;
                    }
                    return ((rows * columns) >> ONE) + ONE;
                } else if (rmul2 < rows) {
                    if (cmod2 == ZERO) {
                        return columns * (rows - r) + ONE;
                    } else {
                        return -(columns * (rows - r));
                    }
                } else {
                    return columns * r + ONE - cmod2;
                }
            } else if (c < columns) {
                return columns * r + c;
            } else {
                if (cmod2 == ZERO) {
                    return columns * (rows + TWO - r) - c;
                } else {
                    return -(columns * (rows + TWO - r) - c + TWO);
                }
            }
        } else {
            if ((c == ONE) || (c == columns + ONE)) {
                return Integer.MIN_VALUE;
            } else if (c == columns) {
                return ONE;
            } else if (c < columns) {
                return c;
            } else {
                if (cmod2 == ZERO) {
                    return (columns << ONE) - c;
                } else {
                    return -((columns << ONE) - c + TWO);
                }
            }
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link DoubleFFT_2D#realForward(double[])}.
     * 
     * @param val
     *            the new value of the <code>[r][c]</code> Fourier mode
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     */
    public void pack(final double val, final int r, final int c,
            final double[] packed, final int pos) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            packed[pos + index] = val;
        } else if (index > Integer.MIN_VALUE) {
            packed[pos - index] = -val;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "[%d][%d] component cannot be modified (always zero)",
                            r, c));
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link DoubleFFT_2D#realForward(double[][])}.
     * 
     * @param val
     *            the new value of the <code>[r][c]</code> Fourier mode
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     */
    public void pack(final double val, final int r, final int c,
            final double[][] packed) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            packed[index / columns][index % columns] = val;
        } else if (index > Integer.MIN_VALUE) {
            packed[(-index) / columns][(-index) % columns] = -val;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "[%d][%d] component cannot be modified (always zero)",
                            r, c));
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link FloatFFT_2D#realForward(float[])}.
     * 
     * @param val
     *            the new value of the <code>[r][c]</code> Fourier mode
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     */
    public void pack(final float val, final int r, final int c,
            final float[] packed, final int pos) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            packed[pos + index] = val;
        } else if (index > Integer.MIN_VALUE) {
            packed[pos - index] = -val;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "[%d][%d] component cannot be modified (always zero)",
                            r, c));
        }
    }

    /**
     * Sets the specified Fourier mode of the transformed data. The data array
     * results from a call to {@link FloatFFT_2D#realForward(float[][])}.
     * 
     * @param val
     *            the new value of the <code>[r][c]</code> Fourier mode
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     */
    public void pack(final float val, final int r, final int c,
            final float[][] packed) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            packed[index / columns][index % columns] = val;
        } else if (index > Integer.MIN_VALUE) {
            packed[(-index) / columns][(-index) % columns] = -val;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "[%d][%d] component cannot be modified (always zero)",
                            r, c));
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to {@link DoubleFFT_2D#realForward(double[])}.
     * 
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     * @return the value of the <code>[r][c]</code> Fourier mode
     */
    public double unpack(final int r, final int c, final double[] packed,
            final int pos) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            return packed[pos + index];
        } else if (index > Integer.MIN_VALUE) {
            return -packed[pos - index];
        } else {
            return ZERO;
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to {@link DoubleFFT_2D#realForward(double[][])}
     * .
     * 
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @return the value of the <code>[r][c]</code> Fourier mode
     */
    public double unpack(final int r, final int c, final double[][] packed) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            return packed[index / columns][index % columns];
        } else if (index > Integer.MIN_VALUE) {
            return -packed[(-index) / columns][(-index) % columns];
        } else {
            return ZERO;
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to {@link FloatFFT_2D#realForward(float[])}
     * .
     * 
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @param pos
     *            index of the first element in array <code>packed</code>
     * @return the value of the <code>[r][c]</code> Fourier mode
     */
    public float unpack(final int r, final int c, final float[] packed,
            final int pos) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            return packed[pos + index];
        } else if (index > Integer.MIN_VALUE) {
            return -packed[pos - index];
        } else {
            return ZERO;
        }
    }

    /**
     * Returns the specified Fourier mode of the transformed data. The data
     * array results from a call to {@link FloatFFT_2D#realForward(float[][])} .
     * 
     * @param r
     *            the row index
     * @param c
     *            the column index
     * @param packed
     *            the transformed data
     * @return the value of the <code>[r][c]</code> Fourier mode
     */
    public float unpack(final int r, final int c, final float[][] packed) {
        final int index = getIndex(r, c);
        if (index >= 0) {
            return packed[index / columns][index % columns];
        } else if (index > Integer.MIN_VALUE) {
            return -packed[(-index) / columns][(-index) % columns];
        } else {
            return ZERO;
        }
    }
}
