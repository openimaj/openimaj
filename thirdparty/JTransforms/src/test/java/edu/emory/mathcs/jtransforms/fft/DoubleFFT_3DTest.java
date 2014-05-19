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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_3D;
import edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * <p>
 * This is a test of the class {@link DoubleFFT_3D}. In this test, a very crude
 * 3d FFT method is implemented (see {@link #complexForward(double[][][])}),
 * assuming that {@link DoubleFFT_1D} and {@link DoubleFFT_2D} have been fully
 * tested and validated. This crude (unoptimized) method is then used to
 * establish <em>expected</em> values of <em>direct</em> Fourier transforms.
 * </p>
 * <p>
 * For <em>inverse</em> Fourier transforms, the test assumes that the
 * corresponding <em>direct</em> Fourier transform has been tested and
 * validated.
 * </p>
 * <p>
 * In all cases, the test consists in creating a random array of data, and
 * verifying that expected and actual values of its Fourier transform coincide
 * within a specified accuracy.
 * </p>
 *
 * @author S&eacute;bastien Brisard
 *
 */
@RunWith(value = Parameterized.class)
public class DoubleFFT_3DTest {
    /** Base message of all exceptions. */
    public static final String DEFAULT_MESSAGE = "%d-threaded FFT of size %dx%dx%d: ";

    /** The constant value of the seed of the random generator. */
    public static final int SEED = 20110625;

    @Parameters
    public static Collection<Object[]> getParameters() {
        final int[] size = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32,
                64, 100, 128 };

        final ArrayList<Object[]> parameters = new ArrayList<Object[]>();

        for (int i = 0; i < size.length; i++) {
            for (int j = 0; j < size.length; j++) {
                for (int k = 0; k < size.length; k++) {
                    parameters.add(new Object[] { size[i], size[j], size[k], 1,
                            SEED });
                    parameters.add(new Object[] { size[i], size[j], size[k], 4,
                            SEED });
                }
            }
        }
        return parameters;
    }

    /** The object to be tested. */
    private final DoubleFFT_3D fft;

    /** Number of columns of the data arrays to be Fourier transformed. */
    private final int numCols;

    /** Number of rows of the data arrays to be Fourier transformed. */
    private final int numRows;

    /** Number of slices of the data arrays to be Fourier transformed. */
    private final int numSlices;

    /** For the generation of the data arrays. */
    private final Random random;

    /** Fourier transform of the slices. */
    private final DoubleFFT_2D sfft;

    /** Fourier transform in the first direction (perpendicular to slices). */
    private final DoubleFFT_1D xfft;

    /**
     * Creates a new instance of this test.
     *
     * @param numSlices
     *            number of slices
     * @param numRows
     *            number of rows
     * @param numColumns
     *            number of columns
     * @param numThreads
     *            the number of threads to be used
     * @param seed
     *            the seed of the random generator
     */
    public DoubleFFT_3DTest(final int numSlices, final int numRows,
            final int numColumns, final int numThreads, final long seed) {
        this.numSlices = numSlices;
        this.numRows = numRows;
        this.numCols = numColumns;
        this.fft = new DoubleFFT_3D(numSlices, numRows, numColumns);
        this.xfft = new DoubleFFT_1D(numSlices);
        this.sfft = new DoubleFFT_2D(numRows, numColumns);
        this.random = new Random(seed);
        ConcurrencyUtils.setNumberOfThreads(numThreads);
        ConcurrencyUtils.setThreadsBeginN_3D(4);
    }

    /**
     * A crude implementation of 3d complex FFT.
     *
     * @param a
     *            the data to be transformed
     */
    public void complexForward(final double[][][] a) {
        for (int s = 0; s < numSlices; s++) {
            sfft.complexForward(a[s]);
        }
        final double[] buffer = new double[2 * numSlices];
        for (int c = 0; c < numCols; c++) {
            for (int r = 0; r < numRows; r++) {
                for (int s = 0; s < numSlices; s++) {
                    buffer[2 * s] = a[s][r][2 * c];
                    buffer[2 * s + 1] = a[s][r][2 * c + 1];
                }
                xfft.complexForward(buffer);
                for (int s = 0; s < numSlices; s++) {
                    a[s][r][2 * c] = buffer[2 * s];
                    a[s][r][2 * c + 1] = buffer[2 * s + 1];
                }
            }
        }
    }

    public FloatingPointEqualityChecker createEqualityChecker(final double rel,
            final double abs) {
        final String msg = String.format(DEFAULT_MESSAGE,
                ConcurrencyUtils.getNumberOfThreads(), numSlices, numRows,
                numCols);
        return new FloatingPointEqualityChecker(msg, rel, abs, 0f, 0f);
    }

    /** A test of {@link DoubleFFT_3D#complexForward(double[])}. */
    @Test
    public void testComplexForward1dInput() {
        final FloatingPointEqualityChecker checker = createEqualityChecker(
                Math.ulp(1d), 0d);
        final double[] actual = new double[2 * numSlices * numRows * numCols];
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final int index = 2 * numCols * (r + numRows * s) + c;
                    final double rnd = random.nextDouble();
                    actual[index] = rnd;
                    expected[s][r][c] = rnd;
                }
            }
        }
        fft.complexForward(actual);
        complexForward(expected);
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final int index = 2 * numCols * (r + numRows * s) + c;
                    final double exp = expected[s][r][c];
                    final double act = actual[index];
                    checker.assertEquals("[" + index + "]", exp, act);
                }
            }
        }
    }

    /** A test of {@link DoubleFFT_3D#complexForward(double[][][])}. */
    @Test
    public void testComplexForward3dInput() {
        final FloatingPointEqualityChecker checker = createEqualityChecker(
                Math.ulp(1d), 0d);
        final double[][][] actual = new double[numSlices][numRows][2 * numCols];
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double rnd = random.nextDouble();
                    actual[s][r][c] = rnd;
                    expected[s][r][c] = rnd;
                }
            }
        }
        fft.complexForward(actual);
        complexForward(expected);
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double exp = expected[s][r][c];
                    final double act = actual[s][r][c];
                    checker.assertEquals("[" + s + "][" + r + "][" + c + "]",
                            exp, act);
                }
            }
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#complexInverse(double[], boolean)}, with
     * the second parameter set to <code>true</code>.
     */
    @Test
    public void testComplexInverseScaled1dInput() {
        double rel = 1E-8;
        double x0 = 5E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] expected = new double[2 * numSlices * numRows * numCols];
        final double[] actual = new double[2 * numSlices * numRows * numCols];
        for (int i = 0; i < actual.length; i++) {
            final double rnd = random.nextDouble();
            actual[i] = rnd;
            expected[i] = rnd;
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, true);
        for (int i = 0; i < actual.length; i++) {
            final double exp = expected[i];
            final double act = actual[i];
            checker.assertEquals("[" + i + "]", exp, act);
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#complexInverse(double[][][], boolean)},
     * with the second parameter set to <code>true</code>.
     */
    @Test
    public void testComplexInverseScaled3dInput() {
        double rel = 1E-8;
        double x0 = 5E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        final double[][][] actual = new double[numSlices][numRows][2 * numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double rnd = random.nextDouble();
                    actual[s][r][c] = rnd;
                    expected[s][r][c] = rnd;
                }
            }
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, true);
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double exp = expected[s][r][c];
                    final double act = actual[s][r][c];
                    checker.assertEquals("[" + s + "][" + r + "][" + c + "]",
                            exp, act);
                }
            }
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#complexInverse(double[], boolean)}, with
     * the second parameter set to <code>false</code>.
     */
    @Test
    public void testComplexInverseUnscaled1dInput() {
        double rel = 1E-8;
        double x0 = 5E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] expected = new double[2 * numSlices * numRows * numCols];
        final double[] actual = new double[2 * numSlices * numRows * numCols];
        for (int i = 0; i < actual.length; i++) {
            final double rnd = random.nextDouble();
            actual[i] = rnd;
            expected[i] = rnd;
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, false);
        final double scaling = numSlices * numRows * numCols;
        for (int i = 0; i < actual.length; i++) {
            final double exp = scaling * expected[i];
            final double act = actual[i];
            checker.assertEquals("[" + i + "]", exp, act);
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#complexInverse(double[][][], boolean)},
     * with the second parameter set to <code>false</code>.
     */
    @Test
    public void testComplexInverseUnscaled3dInput() {
        double rel = 1E-8;
        double x0 = 5E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        final double[][][] actual = new double[numSlices][numRows][2 * numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double rnd = random.nextDouble();
                    actual[s][r][c] = rnd;
                    expected[s][r][c] = rnd;
                }
            }
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, false);
        final double scaling = numSlices * numRows * numCols;
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double exp = scaling * expected[s][r][c];
                    final double act = actual[s][r][c];
                    checker.assertEquals("[" + s + "][" + r + "][" + c + "]",
                            exp, act);
                }
            }
        }
    }

    /** A test of {@link DoubleFFT_3D#realForward(double[])}. */
    @Test
    public void testRealForward1dInput() {
        if (!ConcurrencyUtils.isPowerOf2(numRows)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numCols)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numSlices)) {
            return;
        }
        double rel = 1E-8;
        double x0 = 1E-5;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        int index;
        final double[] actual = new double[numSlices * numRows * numCols];
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        final boolean[] checked = new boolean[numSlices * numRows * numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    index = c + numCols * (r + numRows * s);
                    final double rnd = random.nextDouble();
                    actual[index] = rnd;
                    expected[s][r][2 * c] = rnd;
                    expected[s][r][2 * c + 1] = 0d;
                    checked[index] = false;
                }
            }
        }
        fft.realForward(actual);
        complexForward(expected);
        double exp, act;

        final ArrayList<int[]> list = new ArrayList<int[]>();
        int[] aux;

        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 2; c < numCols; c++) {
                    index = c + numCols * (r + numRows * s);
                    act = actual[index];
                    exp = expected[s][r][c];
                    checker.assertEquals("[" + index + "]", exp, act);
                    checked[index] = true;
                }
            }

            for (int r = 1; r < numRows / 2; r++) {
                list.clear();
                list.add(new int[] { s, r, 0, s, r, 0 });
                list.add(new int[] { s, r, 1, s, r, 1 });
                final int rr = numRows - r;
                list.add(new int[] { s, rr, 1, s, rr, numCols });
                list.add(new int[] { s, rr, 0, s, rr, numCols + 1 });

                for (int i = 0; i < list.size(); i++) {
                    aux = list.get(i);
                    index = aux[2] + numCols * (aux[1] + numRows * aux[0]);
                    act = actual[index];
                    exp = expected[aux[3]][aux[4]][aux[5]];
                    checker.assertEquals("[" + index + "]", exp, act);
                    checked[index] = true;
                }
            }
        }
        for (int s = 1; s < numSlices / 2; s++) {
            list.clear();
            list.add(new int[] { s, 0, 0, s, 0, 0 });
            list.add(new int[] { s, 0, 1, s, 0, 1 });
            list.add(new int[] { s, numRows / 2, 0, s, numRows / 2, 0 });
            list.add(new int[] { s, numRows / 2, 1, s, numRows / 2, 1 });

            final int ss = numSlices - s;
            list.add(new int[] { ss, 0, 1, ss, 0, numCols });
            list.add(new int[] { ss, 0, 0, ss, 0, numCols + 1 });
            list.add(new int[] { ss, numRows / 2, 1, ss, numRows / 2, numCols });
            list.add(new int[] { ss, numRows / 2, 0, ss, numRows / 2,
                    numCols + 1 });

            for (int i = 0; i < list.size(); i++) {
                aux = list.get(i);
                index = aux[2] + numCols * (aux[1] + numRows * aux[0]);
                act = actual[index];
                exp = expected[aux[3]][aux[4]][aux[5]];
                checker.assertEquals("[" + index + "]", exp, act);
                checked[index] = true;
            }
        }
        list.clear();
        list.add(new int[] { 0, 0, 0, 0, 0, 0 });
        list.add(new int[] { 0, 0, 1, 0, 0, numCols });
        list.add(new int[] { 0, numRows / 2, 0, 0, numRows / 2, 0 });
        list.add(new int[] { 0, numRows / 2, 1, 0, numRows / 2, numCols });
        list.add(new int[] { numSlices / 2, 0, 0, numSlices / 2, 0, 0 });
        list.add(new int[] { numSlices / 2, 0, 1, numSlices / 2, 0, numCols });
        list.add(new int[] { numSlices / 2, numRows / 2, 0, numSlices / 2,
                numRows / 2, 0 });
        list.add(new int[] { numSlices / 2, numRows / 2, 1, numSlices / 2,
                numRows / 2, numCols });
        for (int i = 0; i < list.size(); i++) {
            aux = list.get(i);
            index = aux[2] + numCols * (aux[1] + numRows * aux[0]);
            act = actual[index];
            exp = expected[aux[3]][aux[4]][aux[5]];
            checker.assertEquals("[" + index + "]", exp, act);
            checked[index] = true;
        }
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    index = c + numCols * (r + numRows * s);
                    Assert.assertTrue(String.format("[%d][%d][%d]", s, r, c),
                            checked[index]);
                }
            }
        }
    }

    /** A test of {@link DoubleFFT_3D#realForward(double[][][])}. */
    @Test
    public void testRealForward3dInput() {
        if (!ConcurrencyUtils.isPowerOf2(numRows)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numCols)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numSlices)) {
            return;
        }
        double rel = 1E-8;
        double x0 = 1E-5;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[][][] actual = new double[numSlices][numRows][numCols];
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        final boolean[][][] checked = new boolean[numSlices][numRows][numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    final double rnd = random.nextDouble();
                    actual[s][r][c] = rnd;
                    expected[s][r][2 * c] = rnd;
                    expected[s][r][2 * c + 1] = 0d;
                    checked[s][r][c] = false;
                }
            }
        }
        fft.realForward(actual);
        complexForward(expected);
        double exp, act;

        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 2; c < numCols; c++) {
                    act = actual[s][r][c];
                    exp = expected[s][r][c];
                    checker.assertEquals("[" + s + "][" + r + "][" + c + "]",
                            exp, act);
                    checked[s][r][c] = true;
                }
            }

            for (int r = 1; r < numRows / 2; r++) {
                final ArrayList<int[]> list = new ArrayList<int[]>();
                list.add(new int[] { s, r, 0, s, r, 0 });
                list.add(new int[] { s, r, 1, s, r, 1 });
                final int rr = numRows - r;
                list.add(new int[] { s, rr, 1, s, rr, numCols });
                list.add(new int[] { s, rr, 0, s, rr, numCols + 1 });

                for (int i = 0; i < list.size(); i++) {
                    final int[] index = list.get(i);
                    act = actual[index[0]][index[1]][index[2]];
                    exp = expected[index[3]][index[4]][index[5]];
                    checker.assertEquals("[" + index[0] + "][" + index[1]
                            + "][" + index[2] + "]", exp, act);
                    checked[index[0]][index[1]][index[2]] = true;
                }
            }
        }
        for (int s = 1; s < numSlices / 2; s++) {
            final ArrayList<int[]> list = new ArrayList<int[]>();
            list.add(new int[] { s, 0, 0, s, 0, 0 });
            list.add(new int[] { s, 0, 1, s, 0, 1 });
            list.add(new int[] { s, numRows / 2, 0, s, numRows / 2, 0 });
            list.add(new int[] { s, numRows / 2, 1, s, numRows / 2, 1 });

            final int ss = numSlices - s;
            list.add(new int[] { ss, 0, 1, ss, 0, numCols });
            list.add(new int[] { ss, 0, 0, ss, 0, numCols + 1 });
            list.add(new int[] { ss, numRows / 2, 1, ss, numRows / 2, numCols });
            list.add(new int[] { ss, numRows / 2, 0, ss, numRows / 2,
                    numCols + 1 });

            for (int i = 0; i < list.size(); i++) {
                final int[] index = list.get(i);
                act = actual[index[0]][index[1]][index[2]];
                exp = expected[index[3]][index[4]][index[5]];
                checker.assertEquals("[" + index[0] + "][" + index[1] + "]["
                        + index[2] + "]", exp, act);
                checked[index[0]][index[1]][index[2]] = true;
            }
        }
        final ArrayList<int[]> list = new ArrayList<int[]>();
        list.add(new int[] { 0, 0, 0, 0, 0, 0 });
        list.add(new int[] { 0, 0, 1, 0, 0, numCols });
        list.add(new int[] { 0, numRows / 2, 0, 0, numRows / 2, 0 });
        list.add(new int[] { 0, numRows / 2, 1, 0, numRows / 2, numCols });
        list.add(new int[] { numSlices / 2, 0, 0, numSlices / 2, 0, 0 });
        list.add(new int[] { numSlices / 2, 0, 1, numSlices / 2, 0, numCols });
        list.add(new int[] { numSlices / 2, numRows / 2, 0, numSlices / 2,
                numRows / 2, 0 });
        list.add(new int[] { numSlices / 2, numRows / 2, 1, numSlices / 2,
                numRows / 2, numCols });
        for (int i = 0; i < list.size(); i++) {
            final int[] index = list.get(i);
            act = actual[index[0]][index[1]][index[2]];
            exp = expected[index[3]][index[4]][index[5]];
            checker.assertEquals("[" + index[0] + "][" + index[1] + "]["
                    + index[2] + "]", exp, act);
            checked[index[0]][index[1]][index[2]] = true;
        }
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    Assert.assertTrue(String.format("[%d][%d][%d]", s, r, c),
                            checked[s][r][c]);
                }
            }
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#realInverse(double[], boolean)}, with the
     * second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseScaled1dInput() {
        if (!ConcurrencyUtils.isPowerOf2(numRows)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numCols)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numSlices)) {
            return;
        }
        double rel = 1E-9;
        double x0 = 3E-7;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[numRows * numCols * numSlices];
        final double[] expected = new double[actual.length];
        for (int i = 0; i < actual.length; i++) {
            final double rnd = random.nextDouble();
            actual[i] = rnd;
            expected[i] = rnd;
        }
        fft.realForward(actual);
        fft.realInverse(actual, true);
        for (int i = 0; i < actual.length; i++) {
            final double exp = expected[i];
            final double act = actual[i];
            checker.assertEquals("[" + i + "]", exp, act);
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#realInverse(double[][][], boolean)}, with
     * the second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseScaled3dInput() {
        if (!ConcurrencyUtils.isPowerOf2(numRows)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numCols)) {
            return;
        }
        if (!ConcurrencyUtils.isPowerOf2(numSlices)) {
            return;
        }
        double rel = 1E-9;
        double x0 = 3E-7;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[][][] actual = new double[numSlices][numRows][numCols];
        final double[][][] expected = new double[numSlices][numRows][numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    final double rnd = random.nextDouble();
                    actual[s][r][c] = rnd;
                    expected[s][r][c] = rnd;
                }
            }
        }
        fft.realForward(actual);
        fft.realInverse(actual, true);
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    final double exp = expected[s][r][c];
                    final double act = actual[s][r][c];
                    checker.assertEquals("[" + s + "][" + r + "][" + c + "]",
                            exp, act);
                }
            }
        }
    }

    /** A test of {@link DoubleFFT_3D#realForwardFull(double[])}. */
    @Test
    public void testRealForwardFull1dInput() {
        double rel = 1E-7;
        double x0 = 5E-6;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final int n = numSlices * numRows * numCols;
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int index = 0; index < n; index++) {
            final double rnd = random.nextDouble();
            actual[index] = rnd;
            expected[2 * index] = rnd;
            expected[2 * index + 1] = 0d;
        }
        // TODO If the two following lines are permuted, this causes an array
        // index out of bounds exception.
        fft.complexForward(expected);
        fft.realForwardFull(actual);
        for (int i = 0; i < actual.length; i++) {
            final double exp = expected[i];
            final double act = actual[i];
            checker.assertEquals("[" + i + "]", exp, act);
        }
    }

    /** A test of {@link DoubleFFT_3D#realForwardFull(double[][][]). */
    @Test
    public void testRealForwardFull3dInput() {
        double rel = 1E-7;
        double x0 = 5E-6;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[][][] actual = new double[numSlices][numRows][2 * numCols];
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    final double rnd = random.nextDouble();
                    actual[s][r][c] = rnd;
                    expected[s][r][2 * c] = rnd;
                    expected[s][r][2 * c + 1] = 0d;
                }
            }
        }
        // TODO If the two following lines are permuted, this causes an array
        // index out of bounds exception.
        fft.complexForward(expected);
        fft.realForwardFull(actual);
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double exp = expected[s][r][c];
                    final double act = actual[s][r][c];
                    checker.assertEquals("[" + s + "][" + r + "][" + c + "]",
                            exp, act);
                }
            }
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#realInverseFull(double[], boolean)} with
     * the second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseFullScaled1dInput() {
        final double rel = 1E-14;
        final double abs = 1E-15;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final int n = numSlices * numRows * numCols;
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int index = 0; index < n; index++) {
            final double rnd = random.nextDouble();
            actual[index] = rnd;
            expected[2 * index] = rnd;
            expected[2 * index + 1] = 0d;
        }
        // TODO If the two following lines are permuted, this causes an array
        // index out of bounds exception.
        fft.complexInverse(expected, true);
        fft.realInverseFull(actual, true);
        for (int i = 0; i < actual.length; i++) {
            final double exp = expected[i];
            final double act = actual[i];
            checker.assertEquals("[" + i + "]", exp, act);
        }
    }

    /**
     * A test of {@link DoubleFFT_3D#realInverseFull(double[][][], boolean)},
     * with the second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseFullScaled3dInput() {
        final double rel = 1E-14;
        final double abs = 1E-15;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[][][] actual = new double[numSlices][numRows][2 * numCols];
        final double[][][] expected = new double[numSlices][numRows][2 * numCols];
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < numCols; c++) {
                    final double rnd = random.nextDouble();
                    actual[s][r][c] = rnd;
                    expected[s][r][2 * c] = rnd;
                    expected[s][r][2 * c + 1] = 0d;
                }
            }
        }
        // TODO If the two following lines are permuted, this causes an array
        // index out of bounds exception.
        fft.complexInverse(expected, true);
        fft.realInverseFull(actual, true);
        for (int s = 0; s < numSlices; s++) {
            for (int r = 0; r < numRows; r++) {
                for (int c = 0; c < 2 * numCols; c++) {
                    final double exp = expected[s][r][c];
                    final double act = actual[s][r][c];
                    checker.assertEquals("[" + s + "][" + r + "][" + c + "]",
                            exp, act);
                }
            }
        }
    }
}
