/*
 * Licensed to the Hipparchus project under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The Hipparchus project licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hipparchus.analysis.polynomials;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.NullArgumentException;

/**
 * Smoothstep function factory.
 * <p>
 * It allows for quick creation of common and generic smoothstep functions as defined <a
 * href="https://en.wikipedia.org/wiki/Smoothstep>here</a>.
 */
public class SmoothStepFactory {

    /**
     * Private constructor.
     * <p>
     * This class is a utility class, it should neither have a public nor a default constructor. This private
     * constructor prevents the compiler from generating one automatically.
     */
    private SmoothStepFactory() {
        // Empty constructor
    }

    /**
     * Get the {@link SmoothStepFunction clamping smoothstep function}.
     *
     * @return clamping smoothstep function
     */
    public static SmoothStepFunction getClamp() {
        return getGeneralOrder(0);
    }

    /**
     * Get the {@link SmoothStepFunction cubic smoothstep function}.
     *
     * @return cubic smoothstep function
     */
    public static SmoothStepFunction getCubic() {
        return getGeneralOrder(1);
    }

    /**
     * Get the {@link SmoothStepFunction quintic smoothstep function}.
     *
     * @return quintic smoothstep function
     */
    public static SmoothStepFunction getQuintic() {
        return getGeneralOrder(2);
    }

    /**
     * Create a {@link SmoothStepFunction smoothstep function} of order <b>2N + 1</b>.
     * <p>
     * It uses the general smoothstep equation presented <a href="https://en.wikipedia.org/wiki/Smoothstep">here</a> :
     * $S_{N}(x) = \sum_{n=0}^{N} \begin{pmatrix} -N-1 \\ n \end{pmatrix} \begin{pmatrix} 2N+1 \\ N-n \end{pmatrix} x^{N+n+1}$
     *
     * @param N determines the order of the output smoothstep function (=2N + 1)
     * @return smoothstep function of order <b>2N + 1</b>
     */
    public static SmoothStepFunction getGeneralOrder(final int N) {

        final int twoNPlusOne = 2 * N + 1;

        final double[] coefficients = new double[twoNPlusOne + 1];

        int n = N;
        for (int i = twoNPlusOne; i > N; i--) {
            coefficients[i] = pascalTriangle(-N - 1, n) * pascalTriangle(2 * N + 1, N - n);
            n--;
        }

        return new SmoothStepFunction(coefficients);
    }

    /**
     * Returns binomial coefficient without explicit use of factorials, which can't be used with negative integers
     *
     * @param k subset in set
     * @param n set
     * @return number of subset {@code k} in global set {@code n}
     */
    private static int pascalTriangle(final int k, final int n) {

        int result = 1;
        for (int i = 0; i < n; i++) {
            result *= (k - i) / (i + 1);
        }

        return result;
    }

    /**
     * Check that input is between [0:1].
     *
     * @param input input to be checked
     * @throws MathIllegalArgumentException if input is not between [0:1]
     */
    public static void checkBetweenZeroAndOneIncluded(final double input) throws MathIllegalArgumentException {
        if (input < 0 || input > 1) {
            throw new MathIllegalArgumentException(
                    LocalizedCoreFormats.INPUT_EXPECTED_BETWEEN_ZERO_AND_ONE_INCLUDED);
        }
    }

    /**
     * Smoothstep function as defined <a href="https://en.wikipedia.org/wiki/Smoothstep>here</a>.
     * <p>
     * It is used to do a smooth transition between the "left edge" and the "right edge" with left edge assumed to be
     * smaller than right edge.
     * <p>
     * By definition, for order n > 1 and input x, a smoothstep function respects at least the following properties :
     * <ul>
     *     <li>f(x <= leftEdge) = 0 and f(x >= rightEdge) = 1</li>
     *     <li>f'(leftEdge) = f'(rightEdge) = 0</li>
     * </ul>
     * If x is normalized between edges, we have at least :
     * <ul>
     *     <li>f(x <= 0) = 0 and f(x >= 1) = 1</li>
     *     <li>f'(0) = f'(1) = 0</li>
     * </ul>
     * Smoothstep functions of higher order n will have their higher time derivatives also equal to zero at edges...
     */
    public static class SmoothStepFunction extends PolynomialFunction {

        /** Serializable UID. */
        private static final long serialVersionUID = 20230113L;

        /**
         * Construct a smoothstep with the given coefficients. The first element of the coefficients array is the
         * constant term.  Higher degree coefficients follow in sequence.  The degree of the resulting polynomial is the
         * index of the last non-null element of the array, or 0 if all elements are null.
         * <p>
         * The constructor makes a copy of the input array and assigns the copy to the coefficients property.</p>
         *
         * @param c Smoothstep polynomial coefficients.
         * @throws NullArgumentException if {@code c} is {@code null}.
         * @throws MathIllegalArgumentException if {@code c} is empty.
         */
        private SmoothStepFunction(final double[] c) throws MathIllegalArgumentException, NullArgumentException {
            super(c);
        }

        /**
         * Compute the value of the smoothstep for the given argument normalized between edges.
         *
         * @param xNormalized Normalized argument for which the function value should be computed. It is
         *         expected to be between [0:1] and will throw an exception otherwise.
         * @return the value of the polynomial at the given point.
         * @see org.hipparchus.analysis.UnivariateFunction#value(double)
         */
        @Override
        public double value(final double xNormalized) {
            checkBetweenZeroAndOneIncluded(xNormalized);
            return super.value(xNormalized);
        }

        /**
         * Compute the value of the smoothstep function for the given edges and argument.
         * <p>
         * Note that right edge is expected to be greater than left edge. It will throw an exception otherwise.
         *
         * @param leftEdge left edge
         * @param rightEdge right edge
         * @param x Argument for which the function value should be computed
         * @return the value of the polynomial at the given point
         * @throws MathIllegalArgumentException if right edge is greater than left edge
         * @see org.hipparchus.analysis.UnivariateFunction#value(double)
         */
        public double value(final double leftEdge, final double rightEdge, final double x)
                throws MathIllegalArgumentException {

            checkInputEdges(leftEdge, rightEdge);

            final double xClamped = clampInput(leftEdge, rightEdge, x);

            final double xNormalized = normalizeInput(leftEdge, rightEdge, xClamped);

            return super.value(xNormalized);
        }

        /**
         * Check that left edge is lower than right edge. Otherwise, throw an exception.
         *
         * @param leftEdge left edge
         * @param rightEdge right edge
         */
        private void checkInputEdges(final double leftEdge, final double rightEdge) {
            if (leftEdge > rightEdge) {
                throw new MathIllegalArgumentException(LocalizedCoreFormats.RIGHT_EDGE_GREATER_THAN_LEFT_EDGE,
                                                       leftEdge, rightEdge);
            }
        }

        /**
         * Clamp input between edges.
         *
         * @param leftEdge left edge
         * @param rightEdge right edge
         * @param x input to clamp
         * @return clamped input
         */
        private double clampInput(final double leftEdge, final double rightEdge, final double x) {
            if (x <= leftEdge) {
                return leftEdge;
            }
            if (x >= rightEdge) {
                return rightEdge;
            }
            return x;
        }

        /**
         * Normalize input between left and right edges.
         *
         * @param leftEdge left edge
         * @param rightEdge right edge
         * @param x input to normalize
         * @return normalized input
         */
        private double normalizeInput(final double leftEdge, final double rightEdge, final double x) {
            return (x - leftEdge) / (rightEdge - leftEdge);
        }
    }

}
