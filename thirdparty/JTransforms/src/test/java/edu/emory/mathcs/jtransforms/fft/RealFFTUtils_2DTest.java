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

import edu.emory.mathcs.utils.ConcurrencyUtils;

/**
 * Test of the utility class {@link RealFFTUtils_2D}.
 *
 * @author S&eacute;bastien Brisard
 *
 */
@RunWith(value = Parameterized.class)
public class RealFFTUtils_2DTest {
    /** Base message of all exceptions. */
    public static final String DEFAULT_MESSAGE = "FFT of size %dx%d: ";

    /** The constant value of the seed of the random generator. */
    public static final int SEED = 20110624;

    @Parameters
    public static Collection<Object[]> getParameters() {
        final int[] size = { 16, 32, 64, 128 };

        final ArrayList<Object[]> parameters = new ArrayList<Object[]>();

        for (int i = 0; i < size.length; i++) {
            for (int j = 0; j < size.length; j++) {
                parameters.add(new Object[] { size[i], size[j], 1, SEED });
                parameters.add(new Object[] { size[i], size[j], 4, SEED });
            }
        }
        return parameters;
    }

    /** Number of columns of the data arrays to be Fourier transformed. */
    private final int columns;

    /** To perform FFTs on double precision data. */
    private final DoubleFFT_2D fft2d;

    /** To perform FFTs on single precision data. */
    private final FloatFFT_2D fft2f;

    /** For the generation of the data arrays. */
    private final Random random;

    /** Number of rows of the data arrays to be Fourier transformed. */
    private final int rows;

    /** The object to be tested. */
    private final RealFFTUtils_2D unpacker;

    /**
     * Creates a new instance of this test.
     *
     * @param rows
     *            number of rows
     * @param columns
     *            number of columns
     * @param numThreads
     *            the number of threads to be used
     * @param seed
     *            the seed of the random generator
     */
    public RealFFTUtils_2DTest(final int rows, final int columns,
            final int numThreads, final long seed) {
        this.rows = rows;
        this.columns = columns;
        this.fft2d = new DoubleFFT_2D(rows, columns);
        this.fft2f = new FloatFFT_2D(rows, columns);
        this.random = new Random(seed);
        this.unpacker = new RealFFTUtils_2D(rows, columns);
        ConcurrencyUtils.setNumberOfThreads(numThreads);
    }

    public FloatingPointEqualityChecker createEqualityChecker(final double rel,
            final double abs) {
        final String msg = String.format(DEFAULT_MESSAGE, rows, columns);
        return new FloatingPointEqualityChecker(msg, rel, abs, 0f, 0f);
    }

    public FloatingPointEqualityChecker createEqualityChecker(final float rel,
            final float abs) {
        final String msg = String.format(DEFAULT_MESSAGE, rows, columns);
        return new FloatingPointEqualityChecker(msg, 0d, 0d, rel, abs);
    }

    @Test
    public void testUnpack1dInput() {
        double rel = 1E-8;
        double x0 = 5E-3;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[rows * columns];
        final double[][] expected = new double[rows][2 * columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                final double rnd = random.nextDouble();
                actual[r * columns + c] = rnd;
                expected[r][2 * c] = rnd;
                expected[r][2 * c + 1] = 0d;
            }
        }
        fft2d.complexForward(expected);
        fft2d.realForward(actual);

        double exp, act;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final String msg = String.format("[%d][%d]", r, c);
                exp = expected[r][c];
                act = unpacker.unpack(r, c, actual, 0);
                checker.assertEquals(msg, exp, act);
            }
        }
    }

    @Test
    public void testUnpack1fInput() {
        float rel = 1E-1f;
        float x0 = 5E-3f;
        final float abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final float[] actual = new float[rows * columns];
        final float[][] expected = new float[rows][2 * columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                final float rnd = random.nextFloat();
                actual[r * columns + c] = rnd;
                expected[r][2 * c] = rnd;
                expected[r][2 * c + 1] = 0f;
            }
        }
        fft2f.complexForward(expected);
        fft2f.realForward(actual);

        float exp, act;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final String msg = String.format("[%d][%d]", r, c);
                exp = expected[r][c];
                act = unpacker.unpack(r, c, actual, 0);
                checker.assertEquals(msg, exp, act);
            }
        }
    }

    @Test
    public void testUnpack2dInput() {
        double rel = 1E-8;
        double x0 = 5E-3;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[][] actual = new double[rows][columns];
        final double[][] expected = new double[rows][2 * columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                final double rnd = random.nextDouble();
                actual[r][c] = rnd;
                expected[r][2 * c] = rnd;
                expected[r][2 * c + 1] = 0d;
            }
        }
        fft2d.complexForward(expected);
        fft2d.realForward(actual);

        double exp, act;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final String msg = String.format("[%d][%d]", r, c);
                exp = expected[r][c];
                act = unpacker.unpack(r, c, actual);
                checker.assertEquals(msg, exp, act);

                exp = expected[r][c];
                act = unpacker.unpack(r, c, actual);
            }
        }
    }

    @Test
    public void testUnpack2fInput() {
        float rel = 1E-1f;
        float x0 = 5E-3f;
        final float abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final float[][] actual = new float[rows][columns];
        final float[][] expected = new float[rows][2 * columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                final float rnd = random.nextFloat();
                actual[r][c] = rnd;
                expected[r][2 * c] = rnd;
                expected[r][2 * c + 1] = 0f;
            }
        }
        fft2f.complexForward(expected);
        fft2f.realForward(actual);

        float exp, act;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final String msg = String.format("[%d][%d]", r, c);
                exp = expected[r][c];
                act = unpacker.unpack(r, c, actual);
                checker.assertEquals(msg, exp, act);

                exp = expected[r][c];
                act = unpacker.unpack(r, c, actual);
            }
        }
    }

    @Test
    public void testPack1dInput() {
        final double[] data = new double[rows * columns];
        String msg = String.format(DEFAULT_MESSAGE, rows, columns) + "[%d][%d]";
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final double expected = random.nextDouble();
                try {
                    unpacker.pack(expected, r, c, data, 0);
                    final double actual = unpacker.unpack(r, c, data, 0);
                    Assert.assertEquals(String.format(msg, r, c), expected,
                            actual, 0.);
                } catch (IllegalArgumentException e) {
                    // Do nothing
                }
            }
        }
    }

    @Test
    public void testPack1fInput() {
        final float[] data = new float[rows * columns];
        String msg = String.format(DEFAULT_MESSAGE, rows, columns) + "[%d][%d]";
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final float expected = random.nextFloat();
                try {
                    unpacker.pack(expected, r, c, data, 0);
                    final float actual = unpacker.unpack(r, c, data, 0);
                    Assert.assertEquals(String.format(msg, r, c), expected,
                            actual, 0.);
                } catch (IllegalArgumentException e) {
                    // Do nothing
                }
            }
        }
    }

    @Test
    public void testPack2dInput() {
        final double[][] data = new double[rows][columns];
        String msg = String.format(DEFAULT_MESSAGE, rows, columns) + "[%d][%d]";
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final double expected = random.nextDouble();
                try {
                    unpacker.pack(expected, r, c, data);
                    final double actual = unpacker.unpack(r, c, data);
                    Assert.assertEquals(String.format(msg, r, c), expected,
                            actual, 0.);
                } catch (IllegalArgumentException e) {
                    // Do nothing
                }
            }
        }
    }

    @Test
    public void testPack2fInput() {
        final float[][] data = new float[rows][columns];
        String msg = String.format(DEFAULT_MESSAGE, rows, columns) + "[%d][%d]";
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c++) {
                final float expected = random.nextFloat();
                try {
                    unpacker.pack(expected, r, c, data);
                    final float actual = unpacker.unpack(r, c, data);
                    Assert.assertEquals(String.format(msg, r, c), expected,
                            actual, 0.);
                } catch (IllegalArgumentException e) {
                    // Do nothing
                }
            }
        }
    }
}
