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
 * The Original Code is Parallel Colt.
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
package edu.emory.mathcs.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

/**
 * I/O utilities.
 * 
 * @author Piotr Wendykier (piotr.wendykier@gmail.com)
 */
public class IOUtils {

    private static final String FF = "%.4f";

    private IOUtils() {

    }

    /**
     * Fills 1D matrix with random numbers.
     * 
     * @param N
     *            size
     * @param m
     *            1D matrix
     */
    public static void fillMatrix_1D(int N, double[] m) {
        Random r = new Random(2);
        for (int i = 0; i < N; i++) {
            m[i] = r.nextDouble();
        }
    }

    /**
     * Fills 1D matrix with random numbers.
     * 
     * @param N
     *            size
     * @param m
     *            1D matrix
     */
    public static void fillMatrix_1D(int N, float[] m) {
        Random r = new Random(2);
        for (int i = 0; i < N; i++) {
            m[i] = r.nextFloat();
        }
    }

    /**
     * Fills 2D matrix with random numbers.
     * 
     * @param n1
     *            rows
     * @param n2
     *            columns
     * @param m
     *            2D matrix
     */
    public static void fillMatrix_2D(int n1, int n2, double[] m) {
        Random r = new Random(2);
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                m[i * n2 + j] = r.nextDouble();
            }
        }
    }

    /**
     * Fills 2D matrix with random numbers.
     * 
     * @param n1
     *            rows
     * @param n2
     *            columns
     * @param m
     *            2D matrix
     */
    public static void fillMatrix_2D(int n1, int n2, float[] m) {
        Random r = new Random(2);
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                m[i * n2 + j] = r.nextFloat();
            }
        }
    }

    /**
     * Fills 2D matrix with random numbers.
     * 
     * @param n1
     *            rows
     * @param n2
     *            columns
     * @param m
     *            2D matrix
     */
    public static void fillMatrix_2D(int n1, int n2, double[][] m) {
        Random r = new Random(2);
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                m[i][j] = r.nextDouble();
            }
        }
    }

    /**
     * Fills 2D matrix with random numbers.
     * 
     * @param n1
     *            rows
     * @param n2
     *            columns
     * @param m
     *            2D matrix
     */
    public static void fillMatrix_2D(int n1, int n2, float[][] m) {
        Random r = new Random(2);
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                m[i][j] = r.nextFloat();
            }
        }
    }

    /**
     * Fills 3D matrix with random numbers.
     * 
     * @param n1
     *            slices
     * @param n2
     *            rows
     * @param n3
     *            columns
     * @param m
     *            3D matrix
     */
    public static void fillMatrix_3D(int n1, int n2, int n3, double[] m) {
        Random r = new Random(2);
        int sliceStride = n2 * n3;
        int rowStride = n3;
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n3; k++) {
                    m[i * sliceStride + j * rowStride + k] = r.nextDouble();
                }
            }
        }
    }

    /**
     * Fills 3D matrix with random numbers.
     * 
     * @param n1
     *            slices
     * @param n2
     *            rows
     * @param n3
     *            columns
     * @param m
     *            3D matrix
     */
    public static void fillMatrix_3D(int n1, int n2, int n3, float[] m) {
        Random r = new Random(2);
        int sliceStride = n2 * n3;
        int rowStride = n3;
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n3; k++) {
                    m[i * sliceStride + j * rowStride + k] = r.nextFloat();
                }
            }
        }
    }

    /**
     * Fills 3D matrix with random numbers.
     * 
     * @param n1
     *            slices
     * @param n2
     *            rows
     * @param n3
     *            columns
     * @param m
     *            3D matrix
     */
    public static void fillMatrix_3D(int n1, int n2, int n3, double[][][] m) {
        Random r = new Random(2);
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n3; k++) {
                    m[i][j][k] = r.nextDouble();
                }
            }
        }
    }

    /**
     * Fills 3D matrix with random numbers.
     * 
     * @param n1
     *            slices
     * @param n2
     *            rows
     * @param n3
     *            columns
     * @param m
     *            3D matrix
     */
    public static void fillMatrix_3D(int n1, int n2, int n3, float[][][] m) {
        Random r = new Random(2);
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                for (int k = 0; k < n3; k++) {
                    m[i][j][k] = r.nextFloat();
                }
            }
        }
    }

    /**
     * Displays elements of <code>x</code>, assuming that it is 1D complex
     * array. Complex data is represented by 2 double values in sequence: the
     * real and imaginary parts.
     * 
     * @param x
     * @param title
     */
    public static void showComplex_1D(double[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < x.length; i = i + 2) {
            if (x[i + 1] == 0) {
                System.out.println(String.format(FF, x[i]));
                continue;
            }
            if (x[i] == 0) {
                System.out.println(String.format(FF, x[i + 1]) + "i");
                continue;
            }
            if (x[i + 1] < 0) {
                System.out.println(String.format(FF, x[i]) + " - " + (String.format(FF, -x[i + 1])) + "i");
                continue;
            }
            System.out.println(String.format(FF, x[i]) + " + " + (String.format(FF, x[i + 1])) + "i");
        }
        System.out.println();
    }

    /**
     * Displays elements of <code>x</code>, assuming that it is 2D complex
     * array. Complex data is represented by 2 double values in sequence: the
     * real and imaginary parts.
     * 
     * @param rows
     * @param columns
     * @param x
     * @param title
     */
    public static void showComplex_2D(int rows, int columns, double[] x, String title) {
        StringBuffer s = new StringBuffer(String.format(title + ": complex array 2D: %d rows, %d columns\n\n", rows, columns));
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 2 * columns; c = c + 2) {
                if (x[r * 2 * columns + c + 1] == 0) {
                    s.append(String.format(FF + "\t", x[r * 2 * columns + c]));
                    continue;
                }
                if (x[r * 2 * columns + c] == 0) {
                    s.append(String.format(FF + "i\t", x[r * 2 * columns + c + 1]));
                    continue;
                }
                if (x[r * 2 * columns + c + 1] < 0) {
                    s.append(String.format(FF + " - " + FF + "i\t", x[r * 2 * columns + c], -x[r * 2 * columns + c + 1]));
                    continue;
                }
                s.append(String.format(FF + " + " + FF + "i\t", x[r * 2 * columns + c], x[r * 2 * columns + c + 1]));
            }
            s.append("\n");
        }
        System.out.println(s.toString());
    }

    /**
     * Displays elements of <code>x</code>, assuming that it is 3D complex
     * array. Complex data is represented by 2 double values in sequence: the
     * real and imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param title
     */
    public static void showComplex_3D(int n1, int n2, int n3, double[] x, String title) {
        int sliceStride = n2 * 2 * n3;
        int rowStride = 2 * n3;

        System.out.println(title);
        System.out.println("-------------------");

        for (int k = 0; k < 2 * n3; k = k + 2) {
            System.out.println("(:,:," + k / 2 + ")=\n");
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (x[i * sliceStride + j * rowStride + k + 1] == 0) {
                        System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k]) + "\t");
                        continue;
                    }
                    if (x[i * sliceStride + j * rowStride + k] == 0) {
                        System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                        continue;
                    }
                    if (x[i * sliceStride + j * rowStride + k + 1] < 0) {
                        System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k]) + " - " + String.format(FF, -x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                        continue;
                    }
                    System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k]) + " + " + String.format(FF, x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                }
                System.out.println("");
            }
        }
        System.out.println("");
    }

    /**
     * Displays elements of <code>x</code>. Complex data is represented by 2
     * double values in sequence: the real and imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param title
     */
    public static void showComplex_3D(int n1, int n2, int n3, double[][][] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");

        for (int k = 0; k < 2 * n3; k = k + 2) {
            System.out.println("(:,:," + k / 2 + ")=\n");
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (x[i][j][k + 1] == 0) {
                        System.out.print(String.format(FF, x[i][j][k]) + "\t");
                        continue;
                    }
                    if (x[i][j][k] == 0) {
                        System.out.print(String.format(FF, x[i][j][k + 1]) + "i\t");
                        continue;
                    }
                    if (x[i][j][k + 1] < 0) {
                        System.out.print(String.format(FF, x[i][j][k]) + " - " + String.format(FF, -x[i][j][k + 1]) + "i\t");
                        continue;
                    }
                    System.out.print(String.format(FF, x[i][j][k]) + " + " + String.format(FF, x[i][j][k + 1]) + "i\t");
                }
                System.out.println("");
            }
        }
        System.out.println("");
    }

    /**
     * Displays elements of <code>x</code>, assuming that it is 3D complex
     * array. Complex data is represented by 2 double values in sequence: the
     * real and imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param title
     */
    public static void showComplex_3D(int n1, int n2, int n3, float[] x, String title) {
        int sliceStride = n2 * 2 * n3;
        int rowStride = 2 * n3;

        System.out.println(title);
        System.out.println("-------------------");

        for (int k = 0; k < 2 * n3; k = k + 2) {
            System.out.println("(:,:," + k / 2 + ")=\n");
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (x[i * sliceStride + j * rowStride + k + 1] == 0) {
                        System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k]) + "\t");
                        continue;
                    }
                    if (x[i * sliceStride + j * rowStride + k] == 0) {
                        System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                        continue;
                    }
                    if (x[i * sliceStride + j * rowStride + k + 1] < 0) {
                        System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k]) + " - " + String.format(FF, -x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                        continue;
                    }
                    System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k]) + " + " + String.format(FF, x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                }
                System.out.println("");
            }
        }
        System.out.println("");
    }

    /**
     * Displays elements of <code>x</code>, assuming that it is 1D real array.
     * 
     * @param x
     * @param title
     */
    public static void showReal_1D(double[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int j = 0; j < x.length; j++) {
            System.out.println(String.format(FF, x[j]));
        }
        System.out.println();
    }

    /**
     * Displays elements of <code>x</code>, assuming that it is 2D real array.
     * 
     * @param n1
     * @param n2
     * @param x
     * @param title
     */
    public static void showReal_2D(int n1, int n2, double[] x, String title) {
        System.out.println(title);
        System.out.println("-------------------");
        for (int i = 0; i < n1; i++) {
            for (int j = 0; j < n2; j++) {
                if (Math.abs(x[i * n2 + j]) < 5e-5) {
                    System.out.print("0\t");
                } else {
                    System.out.print(String.format(FF, x[i * n2 + j]) + "\t");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * Displays elements of <code>x</code>, assuming that it is 3D real array.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param title
     */
    public static void showReal_3D(int n1, int n2, int n3, double[] x, String title) {
        int sliceStride = n2 * n3;
        int rowStride = n3;

        System.out.println(title);
        System.out.println("-------------------");

        for (int k = 0; k < n3; k++) {
            System.out.println();
            System.out.println("(:,:," + k + ")=\n");
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (Math.abs(x[i * sliceStride + j * rowStride + k]) <= 5e-5) {
                        System.out.print("0\t");
                    } else {
                        System.out.print(String.format(FF, x[i * sliceStride + j * rowStride + k]) + "\t");
                    }
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    /**
     * Displays elements of <code>x</code>.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param title
     */
    public static void showReal_3D(int n1, int n2, int n3, double[][][] x, String title) {

        System.out.println(title);
        System.out.println("-------------------");

        for (int k = 0; k < n3; k++) {
            System.out.println();
            System.out.println("(:,:," + k + ")=\n");
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (Math.abs(x[i][j][k]) <= 5e-5) {
                        System.out.print("0\t");
                    } else {
                        System.out.print(String.format(FF, x[i][j][k]) + "\t");
                    }
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 1D complex array. Complex data is represented by 2
     * double values in sequence: the real and imaginary parts.
     * 
     * @param x
     * @param filename
     */
    public static void writeToFileComplex_1D(double[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < x.length; i = i + 2) {
                if (x[i + 1] == 0) {
                    out.write(String.format(FF, x[i]));
                    out.newLine();
                    continue;
                }
                if (x[i] == 0) {
                    out.write(String.format(FF, x[i + 1]) + "i");
                    out.newLine();
                    continue;
                }
                if (x[i + 1] < 0) {
                    out.write(String.format(FF, x[i]) + " - " + String.format(FF, -x[i + 1]) + "i");
                    out.newLine();
                    continue;
                }
                out.write(String.format(FF, x[i]) + " + " + String.format(FF, x[i + 1]) + "i");
                out.newLine();
            }
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 1D complex array. Complex data is represented by 2
     * double values in sequence: the real and imaginary parts.
     * 
     * @param x
     * @param filename
     */
    public static void writeToFileComplex_1D(float[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < x.length; i = i + 2) {
                if (x[i + 1] == 0) {
                    out.write(String.format(FF, x[i]));
                    out.newLine();
                    continue;
                }
                if (x[i] == 0) {
                    out.write(String.format(FF, x[i + 1]) + "i");
                    out.newLine();
                    continue;
                }
                if (x[i + 1] < 0) {
                    out.write(String.format(FF, x[i]) + " - " + String.format(FF, -x[i + 1]) + "i");
                    out.newLine();
                    continue;
                }
                out.write(String.format(FF, x[i]) + " + " + String.format(FF, x[i + 1]) + "i");
                out.newLine();
            }
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 2D complex array. Complex data is represented by 2
     * double values in sequence: the real and imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param x
     * @param filename
     */
    public static void writeToFileComplex_2D(int n1, int n2, double[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < 2 * n2; j = j + 2) {
                    if ((Math.abs(x[i * 2 * n2 + j]) < 5e-5) && (Math.abs(x[i * 2 * n2 + j + 1]) < 5e-5)) {
                        if (x[i * 2 * n2 + j + 1] >= 0.0) {
                            out.write("0 + 0i\t");
                        } else {
                            out.write("0 - 0i\t");
                        }
                        continue;
                    }

                    if (Math.abs(x[i * 2 * n2 + j + 1]) < 5e-5) {
                        if (x[i * 2 * n2 + j + 1] >= 0.0) {
                            out.write(String.format(FF, x[i * 2 * n2 + j]) + " + 0i\t");
                        } else {
                            out.write(String.format(FF, x[i * 2 * n2 + j]) + " - 0i\t");
                        }
                        continue;
                    }
                    if (Math.abs(x[i * 2 * n2 + j]) < 5e-5) {
                        if (x[i * 2 * n2 + j + 1] >= 0.0) {
                            out.write("0 + " + String.format(FF, x[i * 2 * n2 + j + 1]) + "i\t");
                        } else {
                            out.write("0 - " + String.format(FF, -x[i * 2 * n2 + j + 1]) + "i\t");
                        }
                        continue;
                    }
                    if (x[i * 2 * n2 + j + 1] < 0) {
                        out.write(String.format(FF, x[i * 2 * n2 + j]) + " - " + String.format(FF, -x[i * 2 * n2 + j + 1]) + "i\t");
                        continue;
                    }
                    out.write(String.format(FF, x[i * 2 * n2 + j]) + " + " + String.format(FF, x[i * 2 * n2 + j + 1]) + "i\t");
                }
                out.newLine();
            }

            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 2D complex array. Complex data is represented by 2
     * double values in sequence: the real and imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param x
     * @param filename
     */
    public static void writeToFileComplex_2D(int n1, int n2, float[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < 2 * n2; j = j + 2) {
                    if ((Math.abs(x[i * 2 * n2 + j]) < 5e-5) && (Math.abs(x[i * 2 * n2 + j + 1]) < 5e-5)) {
                        if (x[i * 2 * n2 + j + 1] >= 0.0) {
                            out.write("0 + 0i\t");
                        } else {
                            out.write("0 - 0i\t");
                        }
                        continue;
                    }

                    if (Math.abs(x[i * 2 * n2 + j + 1]) < 5e-5) {
                        if (x[i * 2 * n2 + j + 1] >= 0.0) {
                            out.write(String.format(FF, x[i * 2 * n2 + j]) + " + 0i\t");
                        } else {
                            out.write(String.format(FF, x[i * 2 * n2 + j]) + " - 0i\t");
                        }
                        continue;
                    }
                    if (Math.abs(x[i * 2 * n2 + j]) < 5e-5) {
                        if (x[i * 2 * n2 + j + 1] >= 0.0) {
                            out.write("0 + " + String.format(FF, x[i * 2 * n2 + j + 1]) + "i\t");
                        } else {
                            out.write("0 - " + String.format(FF, -x[i * 2 * n2 + j + 1]) + "i\t");
                        }
                        continue;
                    }
                    if (x[i * 2 * n2 + j + 1] < 0) {
                        out.write(String.format(FF, x[i * 2 * n2 + j]) + " - " + String.format(FF, -x[i * 2 * n2 + j + 1]) + "i\t");
                        continue;
                    }
                    out.write(String.format(FF, x[i * 2 * n2 + j]) + " + " + String.format(FF, x[i * 2 * n2 + j + 1]) + "i\t");
                }
                out.newLine();
            }

            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>. Complex
     * data is represented by 2 double values in sequence: the real and
     * imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param x
     * @param filename
     */
    public static void writeToFileComplex_2D(int n1, int n2, double[][] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < 2 * n2; j = j + 2) {
                    if ((Math.abs(x[i][j]) < 5e-5) && (Math.abs(x[i][j + 1]) < 5e-5)) {
                        if (x[i][j + 1] >= 0.0) {
                            out.write("0 + 0i\t");
                        } else {
                            out.write("0 - 0i\t");
                        }
                        continue;
                    }

                    if (Math.abs(x[i][j + 1]) < 5e-5) {
                        if (x[i][j + 1] >= 0.0) {
                            out.write(String.format(FF, x[i][j]) + " + 0i\t");
                        } else {
                            out.write(String.format(FF, x[i][j]) + " - 0i\t");
                        }
                        continue;
                    }
                    if (Math.abs(x[i][j]) < 5e-5) {
                        if (x[i][j + 1] >= 0.0) {
                            out.write("0 + " + String.format(FF, x[i][j + 1]) + "i\t");
                        } else {
                            out.write("0 - " + String.format(FF, -x[i][j + 1]) + "i\t");
                        }
                        continue;
                    }
                    if (x[i][j + 1] < 0) {
                        out.write(String.format(FF, x[i][j]) + " - " + String.format(FF, -x[i][j + 1]) + "i\t");
                        continue;
                    }
                    out.write(String.format(FF, x[i][j]) + " + " + String.format(FF, x[i][j + 1]) + "i\t");
                }
                out.newLine();
            }

            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 3D complex array. Complex data is represented by 2
     * double values in sequence: the real and imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param filename
     */
    public static void writeToFileComplex_3D(int n1, int n2, int n3, double[] x, String filename) {
        int sliceStride = n2 * n3 * 2;
        int rowStride = n3 * 2;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int k = 0; k < 2 * n3; k = k + 2) {
                out.newLine();
                out.write("(:,:," + k / 2 + ")=");
                out.newLine();
                out.newLine();
                for (int i = 0; i < n1; i++) {
                    for (int j = 0; j < n2; j++) {
                        if (x[i * sliceStride + j * rowStride + k + 1] == 0) {
                            out.write(String.format(FF, x[i * sliceStride + j * rowStride + k]) + "\t");
                            continue;
                        }
                        if (x[i * sliceStride + j * rowStride + k] == 0) {
                            out.write(String.format(FF, x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                            continue;
                        }
                        if (x[i * sliceStride + j * rowStride + k + 1] < 0) {
                            out.write(String.format(FF, x[i * sliceStride + j * rowStride + k]) + " - " + String.format(FF, -x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                            continue;
                        }
                        out.write(String.format(FF, x[i * sliceStride + j * rowStride + k]) + " + " + String.format(FF, x[i * sliceStride + j * rowStride + k + 1]) + "i\t");
                    }
                    out.newLine();
                }
            }
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>. Complex
     * data is represented by 2 double values in sequence: the real and
     * imaginary parts.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param filename
     */
    public static void writeToFileComplex_3D(int n1, int n2, int n3, double[][][] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int k = 0; k < 2 * n3; k = k + 2) {
                out.newLine();
                out.write("(:,:," + k / 2 + ")=");
                out.newLine();
                out.newLine();
                for (int i = 0; i < n1; i++) {
                    for (int j = 0; j < n2; j++) {
                        if (x[i][j][k + 1] == 0) {
                            out.write(String.format(FF, x[i][j][k]) + "\t");
                            continue;
                        }
                        if (x[i][j][k] == 0) {
                            out.write(String.format(FF, x[i][j][k + 1]) + "i\t");
                            continue;
                        }
                        if (x[i][j][k + 1] < 0) {
                            out.write(String.format(FF, x[i][j][k]) + " - " + String.format(FF, -x[i][j][k + 1]) + "i\t");
                            continue;
                        }
                        out.write(String.format(FF, x[i][j][k]) + " + " + String.format(FF, x[i][j][k + 1]) + "i\t");
                    }
                    out.newLine();
                }
            }
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 2D real array.
     * 
     * @param x
     * @param filename
     */
    public static void writeToFileReal_1D(double[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int j = 0; j < x.length; j++) {
                out.write(String.format(FF, x[j]));
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 2D real array.
     * 
     * @param x
     * @param filename
     */
    public static void writeToFileReal_1D(float[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int j = 0; j < x.length; j++) {
                out.write(String.format(FF, x[j]));
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 2D real array.
     * 
     * @param n1
     * @param n2
     * @param x
     * @param filename
     */
    public static void writeToFileReal_2D(int n1, int n2, double[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (Math.abs(x[i * n2 + j]) < 5e-5) {
                        out.write("0\t");
                    } else {
                        out.write(String.format(FF, x[i * n2 + j]) + "\t");
                    }
                }
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 2D real array.
     * 
     * @param n1
     * @param n2
     * @param x
     * @param filename
     */
    public static void writeToFileReal_2D(int n1, int n2, float[] x, String filename) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int i = 0; i < n1; i++) {
                for (int j = 0; j < n2; j++) {
                    if (Math.abs(x[i * n2 + j]) < 5e-5) {
                        out.write("0\t");
                    } else {
                        out.write(String.format(FF, x[i * n2 + j]) + "\t");
                    }
                }
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves elements of <code>x</code> in a file <code>filename</code>,
     * assuming that it is 3D real array.
     * 
     * @param n1
     * @param n2
     * @param n3
     * @param x
     * @param filename
     */
    public static void writeToFileReal_3D(int n1, int n2, int n3, double[] x, String filename) {
        int sliceStride = n2 * n3;
        int rowStride = n3;

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            for (int k = 0; k < n3; k++) {
                out.newLine();
                out.write("(:,:," + k + ")=");
                out.newLine();
                out.newLine();
                for (int i = 0; i < n1; i++) {
                    for (int j = 0; j < n2; j++) {
                        out.write(String.format(FF, x[i * sliceStride + j * rowStride + k]) + "\t");
                    }
                    out.newLine();
                }
                out.newLine();
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves benchmark results in a file.
     * 
     * @param filename
     * @param nthread
     * @param niter
     * @param doWarmup
     * @param doScaling
     * @param times
     * @param sizes
     */
    public static void writeFFTBenchmarkResultsToFile(String filename, int nthread, int niter, boolean doWarmup, boolean doScaling, int[] sizes, double[] times) {
        String[] properties = { "os.name", "os.version", "os.arch", "java.vendor", "java.version" };
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename, false));
            out.write(new Date().toString());
            out.newLine();
            out.write("System properties:");
            out.newLine();
            out.write("\tos.name = " + System.getProperty(properties[0]));
            out.newLine();
            out.write("\tos.version = " + System.getProperty(properties[1]));
            out.newLine();
            out.write("\tos.arch = " + System.getProperty(properties[2]));
            out.newLine();
            out.write("\tjava.vendor = " + System.getProperty(properties[3]));
            out.newLine();
            out.write("\tjava.version = " + System.getProperty(properties[4]));
            out.newLine();
            out.write("\tavailable processors = " + Runtime.getRuntime().availableProcessors());
            out.newLine();
            out.write("Settings:");
            out.newLine();
            out.write("\tused processors = " + nthread);
            out.newLine();
            out.write("\tTHREADS_BEGIN_N_2D = " + ConcurrencyUtils.getThreadsBeginN_2D());
            out.newLine();
            out.write("\tTHREADS_BEGIN_N_3D = " + ConcurrencyUtils.getThreadsBeginN_3D());
            out.newLine();
            out.write("\tnumber of iterations = " + niter);
            out.newLine();
            out.write("\twarm-up performed = " + doWarmup);
            out.newLine();
            out.write("\tscaling performed = " + doScaling);
            out.newLine();
            out.write("--------------------------------------------------------------------------------------------------");
            out.newLine();
            out.write("sizes=[");
            for (int i = 0; i < sizes.length; i++) {
                out.write(Integer.toString(sizes[i]));
                if (i < sizes.length - 1) {
                    out.write(", ");
                } else {
                    out.write("]");
                }
            }
            out.newLine();
            out.write("times(in msec)=[");
            for (int i = 0; i < times.length; i++) {
                out.write(String.format("%.2f", times[i]));
                if (i < times.length - 1) {
                    out.write(", ");
                } else {
                    out.write("]");
                }
            }
            out.newLine();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
