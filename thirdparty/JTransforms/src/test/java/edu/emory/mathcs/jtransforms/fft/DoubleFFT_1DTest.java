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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
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
 * This is a series of JUnit tests for the {@link DoubleFFT_1D}. First,
 * {@link DoubleFFT_1D#complexForward(double[])} is tested by comparison with
 * reference data (FFTW). Then the other methods of this class are tested using
 * {@link DoubleFFT_1D#complexForward(double[])} as a reference.
 *
 * @author S&eacute;bastien Brisard
 *
 */
@RunWith(value = Parameterized.class)
public class DoubleFFT_1DTest {
    /** Base message of all exceptions. */
    public static final String DEFAULT_MESSAGE = "%d-threaded FFT of size %d: ";

    /** Name of binary files (input, untransformed data). */
    private final static String FFTW_INPUT_PATTERN = "fftw%d.in";

    /** Name of binary files (output, transformed data). */
    private final static String FFTW_OUTPUT_PATTERN = "fftw%d.out";

    /** The constant value of the seed of the random generator. */
    public static final int SEED = 20110602;

    @Parameters
    public static Collection<Object[]> getParameters() {
        final int[] size = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32,
                64, 100, 120, 128, 256, 310, 512, 1024, 1056, 2048, 8192,
                10158, 16384, 32768, 65530, 65536, 131072 };

        final ArrayList<Object[]> parameters = new ArrayList<Object[]>();
        for (int i = 0; i < size.length; i++) {
            parameters.add(new Object[] { size[i], 1, SEED });
            parameters.add(new Object[] { size[i], 2, SEED });
            parameters.add(new Object[] { size[i], 4, SEED });
        }
        return parameters;
    }

    /** The FFT to be tested. */
    private final DoubleFFT_1D fft;

    /** The size of the FFT to be tested. */
    private final int n;

    /** For the generation of the data arrays. */
    private final Random random;

    /**
     * Creates a new instance of this class.
     *
     * @param n
     *            the size of the FFT to be tested
     * @param numThreads
     *            the number of threads
     * @param seed
     *            the seed of the random generator
     */
    public DoubleFFT_1DTest(final int n, final int numThreads, final long seed) {
        this.n = n;
        this.fft = new DoubleFFT_1D(n);
        this.random = new Random(seed);
        ConcurrencyUtils.setThreadsBeginN_1D_FFT_2Threads(512);
        ConcurrencyUtils.setThreadsBeginN_1D_FFT_4Threads(512);
        ConcurrencyUtils.setNumberOfThreads(numThreads);
    }

    public FloatingPointEqualityChecker createEqualityChecker(final double rel,
            final double abs) {
        final String msg = String.format(DEFAULT_MESSAGE,
                ConcurrencyUtils.getNumberOfThreads(), n);
        return new FloatingPointEqualityChecker(msg, rel, abs, 0f, 0f);
    }

    /**
     * Read the binary reference data files generated with FFTW. The structure
     * of these files is very simple: double values are written linearly (little
     * endian).
     *
     * @param name
     *            the file name
     * @param data
     *            the array to be updated with the data read (the size of this
     *            array gives the number of <code>double</code> to be retrieved
     */
    public void readData(final String name, final double[] data) {
        try {
            // final String path = getClass().getPackage().getName()
            // .replace(".", "/");
            // final File f = new File(getClass().getClassLoader()
            // .getResource(path + "/resources/" + name).getFile());
            final File f = new File(getClass().getClassLoader()
                    .getResource(name).getFile());
            final FileInputStream fin = new FileInputStream(f);
            final FileChannel fc = fin.getChannel();
            final ByteBuffer buffer = ByteBuffer.allocate(8 * data.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            fc.read(buffer);
            for (int i = 0; i < data.length; i++) {
                data[i] = buffer.getDouble(8 * i);
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#complexForward(double[])}. This
     * method is tested by computation of the FFT of some pre-generated data,
     * and comparison with results obtained with FFTW.
     */
    @Test
    public void testComplexForward() {
        double rel = 1E-9;
        double x0 = 1E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        readData(String.format(FFTW_INPUT_PATTERN, Integer.valueOf(n)), actual);
        readData(String.format(FFTW_OUTPUT_PATTERN, Integer.valueOf(n)),
                expected);
        fft.complexForward(actual);
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#complexInverse(double[], boolean)},
     * with the second parameter set to <code>true</code>.
     */
    @Test
    public void testComplexInverseScaled() {
        double rel = 1E-9;
        double x0 = 1E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < 2 * n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, true);
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#complexInverse(double[], boolean)},
     * with the second parameter set to <code>false</code>.
     */
    @Test
    public void testComplexInverseUnscaled() {
        double rel = 1E-9;
        double x0 = 1E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < 2 * n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.complexForward(actual);
        fft.complexInverse(actual, false);
        final double s = 1. / (double) n;
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], s * actual[i]);
        }
    }

    /** This is a test of {@link DoubleFFT_1D#realForward(double[])}. */
    @Test
    public void testRealForward() {
        double rel = 1E-9;
        double x0 = 1E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.complexForward(expected);
        fft.realForward(actual);
        checker.assertEquals("[0]", expected[0], actual[0]);
        if (n > 1) {
            checker.assertEquals("[1]", expected[n], actual[1]);
        }
        for (int i = 2; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }

    /** This is a test of {@link DoubleFFT_1D#realForward(double[])}. */
    @Test
    public void testRealForwardFull() {
        double rel = 1E-8;
        double x0 = 5E-6;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.complexForward(expected);
        fft.realForwardFull(actual);
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverseFull(double[], boolean)}
     * , with the second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseFullScaled() {
        double rel = 1E-9;
        double x0 = 1E-7;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.realInverseFull(actual, true);
        fft.complexInverse(expected, true);
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverseFull(double[], boolean)}
     * , with the second parameter set to <code>false</code>.
     */
    @Test
    public void testRealInverseFullUnscaled() {
        double rel = 1E-7;
        double x0 = 5E-7;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[2 * n];
        final double[] expected = new double[2 * n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[2 * i] = actual[i];
            expected[2 * i + 1] = 0.;
        }
        fft.realInverseFull(actual, false);
        fft.complexInverse(expected, false);
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverse(double[], boolean)},
     * with the second parameter set to <code>true</code>.
     */
    @Test
    public void testRealInverseScaled() {
        double rel = 1E-9;
        double x0 = 1E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[n];
        final double[] expected = new double[n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.realForward(actual);
        fft.realInverse(actual, true);
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }

    /**
     * This is a test of {@link DoubleFFT_1D#realInverse(double[], boolean)},
     * with the second parameter set to <code>false</code>.
     */
    @Test
    public void testRealInverseUnscaled() {
        double rel = 1E-9;
        double x0 = 1E-8;
        final double abs = rel * x0;
        final FloatingPointEqualityChecker checker = createEqualityChecker(rel,
                abs);
        final double[] actual = new double[n];
        final double[] expected = new double[n];
        for (int i = 0; i < n; i++) {
            actual[i] = 2. * random.nextDouble() - 1.;
            expected[i] = actual[i];
        }
        fft.realForward(actual);
        fft.realInverse(actual, true);
        for (int i = 0; i < actual.length; i++) {
            checker.assertEquals("[" + i + "]", expected[i], actual[i]);
        }
    }
}