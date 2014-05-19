/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is JTransforms.
 *
 * The Initial Developer of the Original Code is
 * Piotr Wendykier, Emory University.
 * Portions created by the Initial Developer are Copyright (C) 2007-2009
 * the Initial Developer. All Rights Reserved.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.emory.mathcs.jtransforms.fft;

import edu.emory.mathcs.utils.IOUtils;

/**
 * Accuracy check of double precision FFT's
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class AccuracyCheckDoubleFFT {

    private static int[] sizes1D = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32, 64, 100, 120, 128, 256, 310, 512, 1024, 1056, 2048, 8192, 10158, 16384, 32768, 65530, 65536, 131072 };

    private static int[] sizes2D = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32, 64, 100, 120, 128, 256, 310, 511, 512, 1024 };

    private static int[] sizes3D = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32, 64, 100, 128 };

    private static int[] sizes2D2 = { 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024 };

    private static int[] sizes3D2 = { 2, 4, 8, 16, 32, 64, 128 };

    private static double eps = Math.pow(2, -52);

    private AccuracyCheckDoubleFFT() {

    }

    public static void checkAccuracyComplexFFT_1D() {
        System.out.println("Checking accuracy of 1D complex FFT...");
        for (int i = 0; i < sizes1D.length; i++) {
            DoubleFFT_1D fft = new DoubleFFT_1D(sizes1D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes1D[i]];
            IOUtils.fillMatrix_1D(2 * sizes1D[i], a);
            double[] b = new double[2 * sizes1D[i]];
            IOUtils.fillMatrix_1D(2 * sizes1D[i], b);
            fft.complexForward(a);
            fft.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft = null;
            System.gc();
        }

    }

    public static void checkAccuracyComplexFFT_2D() {
        System.out.println("Checking accuracy of 2D complex FFT (double[] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes2D[i] * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], 2 * sizes2D[i], a);
            double[] b = new double[2 * sizes2D[i] * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], 2 * sizes2D[i], b);
            fft2.complexForward(a);
            fft2.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 2D complex FFT (double[][] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[][] a = new double[sizes2D[i]][2 * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], 2 * sizes2D[i], a);
            double[][] b = new double[sizes2D[i]][2 * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], 2 * sizes2D[i], b);
            fft2.complexForward(a);
            fft2.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }
    }

    public static void checkAccuracyComplexFFT_3D() {
        System.out.println("Checking accuracy of 3D complex FFT (double[] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes3D[i] * sizes3D[i] * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], 2 * sizes3D[i], a);
            double[] b = new double[2 * sizes3D[i] * sizes3D[i] * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], 2 * sizes3D[i], b);
            fft3.complexForward(a);
            fft3.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }

        System.out.println("Checking accuracy of 3D complex FFT (double[][][] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[][][] a = new double[sizes3D[i]][sizes3D[i]][2 * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], 2 * sizes3D[i], a);
            double[][][] b = new double[sizes3D[i]][sizes3D[i]][2 * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], 2 * sizes3D[i], b);
            fft3.complexForward(a);
            fft3.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }
    }

    public static void checkAccuracyRealFFT_1D() {
        System.out.println("Checking accuracy of 1D real FFT...");
        for (int i = 0; i < sizes1D.length; i++) {
            DoubleFFT_1D fft = new DoubleFFT_1D(sizes1D[i]);
            double err = 0.0;
            double[] a = new double[sizes1D[i]];
            IOUtils.fillMatrix_1D(sizes1D[i], a);
            double[] b = new double[sizes1D[i]];
            IOUtils.fillMatrix_1D(sizes1D[i], b);
            fft.realForward(a);
            fft.realInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft = null;
            System.gc();
        }
        System.out.println("Checking accuracy of on 1D real forward full FFT...");
        for (int i = 0; i < sizes1D.length; i++) {
            DoubleFFT_1D fft = new DoubleFFT_1D(sizes1D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes1D[i]];
            IOUtils.fillMatrix_1D(sizes1D[i], a);
            double[] b = new double[2 * sizes1D[i]];
            for (int j = 0; j < sizes1D[i]; j++) {
                b[2 * j] = a[j];
            }
            fft.realForwardFull(a);
            fft.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 1D real inverse full FFT...");
        for (int i = 0; i < sizes1D.length; i++) {
            DoubleFFT_1D fft = new DoubleFFT_1D(sizes1D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes1D[i]];
            IOUtils.fillMatrix_1D(sizes1D[i], a);
            double[] b = new double[2 * sizes1D[i]];
            for (int j = 0; j < sizes1D[i]; j++) {
                b[2 * j] = a[j];
            }
            fft.realInverseFull(a, true);
            fft.complexForward(a);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft = null;
            System.gc();
        }

    }

    public static void checkAccuracyRealFFT_2D() {
        System.out.println("Checking accuracy of 2D real FFT (double[] input)...");
        for (int i = 0; i < sizes2D2.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D2[i], sizes2D2[i]);
            double err = 0.0;
            double[] a = new double[sizes2D2[i] * sizes2D2[i]];
            IOUtils.fillMatrix_2D(sizes2D2[i], sizes2D2[i], a);
            double[] b = new double[sizes2D2[i] * sizes2D2[i]];
            IOUtils.fillMatrix_2D(sizes2D2[i], sizes2D2[i], b);
            fft2.realForward(a);
            fft2.realInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D2[i] + " x " + sizes2D2[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D2[i] + " x " + sizes2D2[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 2D real FFT (double[][] input)...");
        for (int i = 0; i < sizes2D2.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D2[i], sizes2D2[i]);
            double err = 0.0;
            double[][] a = new double[sizes2D2[i]][sizes2D2[i]];
            IOUtils.fillMatrix_2D(sizes2D2[i], sizes2D2[i], a);
            double[][] b = new double[sizes2D2[i]][sizes2D2[i]];
            IOUtils.fillMatrix_2D(sizes2D2[i], sizes2D2[i], b);
            fft2.realForward(a);
            fft2.realInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D2[i] + " x " + sizes2D2[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D2[i] + " x " + sizes2D2[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 2D real forward full FFT (double[] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes2D[i] * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], a);
            double[] b = new double[2 * sizes2D[i] * sizes2D[i]];
            for (int r = 0; r < sizes2D[i]; r++) {
                for (int c = 0; c < sizes2D[i]; c++) {
                    b[r * 2 * sizes2D[i] + 2 * c] = a[r * sizes2D[i] + c];
                }
            }
            fft2.realForwardFull(a);
            fft2.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 2D real forward full FFT (double[][] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[][] a = new double[sizes2D[i]][2 * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], a);
            double[][] b = new double[sizes2D[i]][2 * sizes2D[i]];
            for (int r = 0; r < sizes2D[i]; r++) {
                for (int c = 0; c < sizes2D[i]; c++) {
                    b[r][2 * c] = a[r][c];
                }
            }
            fft2.realForwardFull(a);
            fft2.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 2D real inverse full FFT (double[] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes2D[i] * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], a);
            double[] b = new double[2 * sizes2D[i] * sizes2D[i]];
            for (int r = 0; r < sizes2D[i]; r++) {
                for (int c = 0; c < sizes2D[i]; c++) {
                    b[r * 2 * sizes2D[i] + 2 * c] = a[r * sizes2D[i] + c];
                }
            }
            fft2.realInverseFull(a, true);
            fft2.complexForward(a);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 2D real inverse full FFT (double[][] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleFFT_2D fft2 = new DoubleFFT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[][] a = new double[sizes2D[i]][2 * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], a);
            double[][] b = new double[sizes2D[i]][2 * sizes2D[i]];
            for (int r = 0; r < sizes2D[i]; r++) {
                for (int c = 0; c < sizes2D[i]; c++) {
                    b[r][2 * c] = a[r][c];
                }
            }
            fft2.realInverseFull(a, true);
            fft2.complexForward(a);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fft2 = null;
            System.gc();
        }

    }

    public static void checkAccuracyRealFFT_3D() {
        System.out.println("Checking accuracy of 3D real FFT (double[] input)...");
        for (int i = 0; i < sizes3D2.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D2[i], sizes3D2[i], sizes3D2[i]);
            double err = 0.0;
            double[] a = new double[sizes3D2[i] * sizes3D2[i] * sizes3D2[i]];
            IOUtils.fillMatrix_3D(sizes3D2[i], sizes3D2[i], sizes3D2[i], a);
            double[] b = new double[sizes3D2[i] * sizes3D2[i] * sizes3D2[i]];
            IOUtils.fillMatrix_3D(sizes3D2[i], sizes3D2[i], sizes3D2[i], b);
            fft3.realForward(b);
            fft3.realInverse(b, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D2[i] + " x " + sizes3D2[i] + " x " + sizes3D2[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D2[i] + " x " + sizes3D2[i] + " x " + sizes3D2[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 3D real FFT (double[][][] input)...");
        for (int i = 0; i < sizes3D2.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D2[i], sizes3D2[i], sizes3D2[i]);
            double err = 0.0;
            double[][][] a = new double[sizes3D2[i]][sizes3D2[i]][sizes3D2[i]];
            IOUtils.fillMatrix_3D(sizes3D2[i], sizes3D2[i], sizes3D2[i], a);
            double[][][] b = new double[sizes3D2[i]][sizes3D2[i]][sizes3D2[i]];
            IOUtils.fillMatrix_3D(sizes3D2[i], sizes3D2[i], sizes3D2[i], b);
            fft3.realForward(b);
            fft3.realInverse(b, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D2[i] + " x " + sizes3D2[i] + " x " + sizes3D2[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D2[i] + " x " + sizes3D2[i] + " x " + sizes3D2[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 3D real forward full FFT (double[] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes3D[i] * sizes3D[i] * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], a);
            double[] b = new double[2 * sizes3D[i] * sizes3D[i] * sizes3D[i]];
            for (int s = 0; s < sizes3D[i]; s++) {
                for (int r = 0; r < sizes3D[i]; r++) {
                    for (int c = 0; c < sizes3D[i]; c++) {
                        b[s * 2 * sizes3D[i] * sizes3D[i] + r * 2 * sizes3D[i] + 2 * c] = a[s * sizes3D[i] * sizes3D[i] + r * sizes3D[i] + c];
                    }
                }
            }
            fft3.realForwardFull(a);
            fft3.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }

        System.out.println("Checking accuracy of 3D real forward full FFT (double[][][] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[][][] a = new double[sizes3D[i]][sizes3D[i]][2 * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], a);
            double[][][] b = new double[sizes3D[i]][sizes3D[i]][2 * sizes3D[i]];
            for (int s = 0; s < sizes3D[i]; s++) {
                for (int r = 0; r < sizes3D[i]; r++) {
                    for (int c = 0; c < sizes3D[i]; c++) {
                        b[s][r][2 * c] = a[s][r][c];
                    }
                }
            }
            fft3.realForwardFull(a);
            fft3.complexInverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }

        System.out.println("Checking accuracy of 3D real inverse full FFT (double[] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[] a = new double[2 * sizes3D[i] * sizes3D[i] * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], a);
            double[] b = new double[2 * sizes3D[i] * sizes3D[i] * sizes3D[i]];
            for (int s = 0; s < sizes3D[i]; s++) {
                for (int r = 0; r < sizes3D[i]; r++) {
                    for (int c = 0; c < sizes3D[i]; c++) {
                        b[s * 2 * sizes3D[i] * sizes3D[i] + r * 2 * sizes3D[i] + 2 * c] = a[s * sizes3D[i] * sizes3D[i] + r * sizes3D[i] + c];
                    }
                }
            }
            fft3.realInverseFull(a, true);
            fft3.complexForward(a);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 3D real inverse full FFT (double[][][] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleFFT_3D fft3 = new DoubleFFT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[][][] a = new double[sizes3D[i]][sizes3D[i]][2 * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], a);
            double[][][] b = new double[sizes3D[i]][sizes3D[i]][2 * sizes3D[i]];
            for (int s = 0; s < sizes3D[i]; s++) {
                for (int r = 0; r < sizes3D[i]; r++) {
                    for (int c = 0; c < sizes3D[i]; c++) {
                        b[s][r][2 * c] = a[s][r][c];
                    }
                }
            }
            fft3.realInverseFull(a, true);
            fft3.complexForward(a);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            fft3 = null;
            System.gc();
        }
    }

    private static double computeRMSE(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays are not the same size.");
        }
        double rms = 0;
        double tmp;
        for (int i = 0; i < a.length; i++) {
            tmp = (a[i] - b[i]);
            rms += tmp * tmp;
        }
        return Math.sqrt(rms / (double) a.length);
    }

    private static double computeRMSE(double[][] a, double[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            throw new IllegalArgumentException("Arrays are not the same size.");
        }
        double rms = 0;
        double tmp;
        for (int r = 0; r < a.length; r++) {
            for (int c = 0; c < a[0].length; c++) {
                tmp = (a[r][c] - b[r][c]);
                rms += tmp * tmp;
            }
        }
        return Math.sqrt(rms / (a.length * a[0].length));
    }

    private static double computeRMSE(double[][][] a, double[][][] b) {
        if (a.length != b.length || a[0].length != b[0].length || a[0][0].length != b[0][0].length) {
            throw new IllegalArgumentException("Arrays are not the same size.");
        }
        double rms = 0;
        double tmp;
        for (int s = 0; s < a.length; s++) {
            for (int r = 0; r < a[0].length; r++) {
                for (int c = 0; c < a[0][0].length; c++) {
                    tmp = (a[s][r][c] - b[s][r][c]);
                    rms += tmp * tmp;
                }
            }
        }
        return Math.sqrt(rms / (a.length * a[0].length * a[0][0].length));
    }

    public static void main(String[] args) {
        checkAccuracyComplexFFT_1D();
        checkAccuracyRealFFT_1D();
        checkAccuracyComplexFFT_2D();
        checkAccuracyRealFFT_2D();
        checkAccuracyComplexFFT_3D();
        checkAccuracyRealFFT_3D();
        System.exit(0);
    }
}
