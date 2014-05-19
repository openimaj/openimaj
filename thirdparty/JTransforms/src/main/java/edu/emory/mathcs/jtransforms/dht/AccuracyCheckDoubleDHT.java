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

package edu.emory.mathcs.jtransforms.dht;

import edu.emory.mathcs.utils.IOUtils;

/**
 * Accuracy check of double precision DHT's
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 * 
 */
public class AccuracyCheckDoubleDHT {

    private static int[] sizes1D = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32, 64, 100, 120, 128, 256, 310, 512, 1024, 1056, 2048, 8192, 10158, 16384, 32768, 65536, 131072 };

    private static int[] sizes2D = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32, 64, 100, 120, 128, 256, 310, 511, 512, 1024 };

    private static int[] sizes3D = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 16, 32, 64, 100, 128 };

    private static double eps = Math.pow(2, -52);

    private AccuracyCheckDoubleDHT() {

    }

    public static void checkAccuracyDHT_1D() {
        System.out.println("Checking accuracy of 1D DHT...");
        for (int i = 0; i < sizes1D.length; i++) {
            DoubleDHT_1D fht = new DoubleDHT_1D(sizes1D[i]);
            double err = 0.0;
            double[] a = new double[sizes1D[i]];
            IOUtils.fillMatrix_1D(sizes1D[i], a);
            double[] b = new double[sizes1D[i]];
            IOUtils.fillMatrix_1D(sizes1D[i], b);
            fht.forward(a);
            fht.inverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes1D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fht = null;
            System.gc();
        }
    }

    public static void checkAccuracyDHT_2D() {
        System.out.println("Checking accuracy of 2D DHT (double[] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleDHT_2D fht2 = new DoubleDHT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[] a = new double[sizes2D[i] * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], a);
            double[] b = new double[sizes2D[i] * sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], b);
            fht2.forward(a);
            fht2.inverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fht2 = null;
            System.gc();
        }
        System.out.println("Checking accuracy of 2D DHT (double[][] input)...");
        for (int i = 0; i < sizes2D.length; i++) {
            DoubleDHT_2D fht2 = new DoubleDHT_2D(sizes2D[i], sizes2D[i]);
            double err = 0.0;
            double[][] a = new double[sizes2D[i]][sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], a);
            double[][] b = new double[sizes2D[i]][sizes2D[i]];
            IOUtils.fillMatrix_2D(sizes2D[i], sizes2D[i], b);
            fht2.forward(a);
            fht2.inverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes2D[i] + " x " + sizes2D[i] + ";\terror = " + err);
            }
            a = null;
            b = null;
            fht2 = null;
            System.gc();
        }

    }

    public static void checkAccuracyDHT_3D() {
        System.out.println("Checking accuracy of 3D DHT (double[] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleDHT_3D dht3 = new DoubleDHT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[] a = new double[sizes3D[i] * sizes3D[i] * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], a);
            double[] b = new double[sizes3D[i] * sizes3D[i] * sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], b);
            dht3.forward(a);
            dht3.inverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            dht3 = null;
            System.gc();
        }

        System.out.println("Checking accuracy of 3D DHT (double[][][] input)...");
        for (int i = 0; i < sizes3D.length; i++) {
            DoubleDHT_3D dht3 = new DoubleDHT_3D(sizes3D[i], sizes3D[i], sizes3D[i]);
            double err = 0.0;
            double[][][] a = new double[sizes3D[i]][sizes3D[i]][sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], a);
            double[][][] b = new double[sizes3D[i]][sizes3D[i]][sizes3D[i]];
            IOUtils.fillMatrix_3D(sizes3D[i], sizes3D[i], sizes3D[i], b);
            dht3.forward(a);
            dht3.inverse(a, true);
            err = computeRMSE(a, b);
            if (err > eps) {
                System.err.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            } else {
                System.out.println("\tsize = " + sizes3D[i] + " x " + sizes3D[i] + " x " + sizes3D[i] + ";\t\terror = " + err);
            }
            a = null;
            b = null;
            dht3 = null;
            System.gc();
        }
    }

    private static double computeRMSE(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Arrays are not the same size");
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
            throw new IllegalArgumentException("Arrays are not the same size");
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
            throw new IllegalArgumentException("Arrays are not the same size");
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
        checkAccuracyDHT_1D();
        checkAccuracyDHT_2D();
        checkAccuracyDHT_3D();
        System.exit(0);
    }
}
