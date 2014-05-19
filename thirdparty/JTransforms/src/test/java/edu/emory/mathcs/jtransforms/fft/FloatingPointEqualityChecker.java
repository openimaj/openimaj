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

import org.junit.Assert;

/**
 * A utility class for consistently asserting equality of two floating-point
 * numbers.
 *
 * @author S&eacute;bastien Brisard
 *
 */
public class FloatingPointEqualityChecker {
    /**
     * If the expected (<code>double</code>) value is below this threshold, the
     * relative error is not tested.
     */
    private final double dabs;

    /** Maximum (<code>double</code> relative error. */
    private final double drel;

    /**
     * If the expected (<code>double</code>) value is below this threshold, the
     * relative error is not tested.
     */
    private final float fabs;

    /** Maximum (<code>float</code> relative error. */
    private final double frel;

    /** Default message used in all thrown exceptions. */
    private final String msg;

    /**
     * Creates a new instance of this class.
     *
     * @param msg
     *            the default message returned by all assertion exceptions
     * @param drel
     *            the maximum relative error, for <code>double</code> values
     * @param dabs
     *            the maximum absolute error, for <code>double</code> values
     * @param frel
     *            the maximum relative error, for <code>float</code> values
     * @param fabs
     *            the maximum absolute error, for <code>float</code> values
     */
    public FloatingPointEqualityChecker(final String msg, final double drel,
            final double dabs, final float frel, final float fabs) {
        this.msg = msg;
        this.drel = drel;
        this.dabs = dabs;
        this.frel = frel;
        this.fabs = fabs;
    }

    /**
     * Asserts that two <code>double</code>s are equal.
     *
     * @param msg
     *            a message to be concatenated with the default message
     * @param expected
     *            expected value
     * @param actual
     *            the value to check against <code>expected</code>
     */
    public final void assertEquals(final String msg, final double expected,
            final double actual) {
        final double delta = Math.abs(actual - expected);
        if (!(delta <= drel * Math.abs(expected))) {
            Assert.assertEquals(this.msg + msg + ", abs = " + delta
                    + ", rel = " + (delta / Math.abs(expected)), expected,
                    actual, dabs);
        }
    }

    /**
     * Asserts that two <code>float</code>s are equal.
     *
     * @param msg
     *            a message to be concatenated with the default message
     * @param expected
     *            expected value
     * @param actual
     *            the value to check against <code>expected</code>
     */
    public final void assertEquals(final String msg, final float expected,
            final float actual) {
        final float delta = Math.abs(actual - expected);
        if (!(delta <= frel * Math.abs(expected))) {
            Assert.assertEquals(this.msg + msg + ", abs = " + delta
                    + ", rel = " + (delta / Math.abs(expected)), expected,
                    actual, fabs);
        }
    }
}
