/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.hipparchus.util;

import org.hipparchus.Field;
import org.hipparchus.CalculusFieldElement;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.random.RandomDataGenerator;
import org.hipparchus.util.MathUtils.FieldSumAndResidual;
import org.hipparchus.util.MathUtils.SumAndResidual;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for the MathUtils class.
 */
public final class MathUtilsTest {
    @Test
    public void testEqualsDouble() {
        final double x = 1234.5678;
        Assert.assertTrue(MathUtils.equals(x, x));
        Assert.assertFalse(MathUtils.equals(x, -x));

        // Special cases (cf. semantics of JDK's "Double").
        // 1. NaN
        Assert.assertTrue(MathUtils.equals(Double.NaN, Double.NaN));
        // 2. Negative zero
        final double mZero = -0d;
        final double zero = 0d;
        Assert.assertTrue(MathUtils.equals(zero, zero));
        Assert.assertTrue(MathUtils.equals(mZero, mZero));
        Assert.assertFalse(MathUtils.equals(mZero, zero));
    }

    @Test
    public void testHash() {
        double[] testArray = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            1d,
            0d,
            1E-14,
            (1 + 1E-14),
            Double.MIN_VALUE,
            Double.MAX_VALUE };
        for (int i = 0; i < testArray.length; i++) {
            for (int j = 0; j < testArray.length; j++) {
                if (i == j) {
                    Assert.assertEquals(MathUtils.hash(testArray[i]), MathUtils.hash(testArray[j]));
                    Assert.assertEquals(MathUtils.hash(testArray[j]), MathUtils.hash(testArray[i]));
                } else {
                    Assert.assertTrue(MathUtils.hash(testArray[i]) != MathUtils.hash(testArray[j]));
                    Assert.assertTrue(MathUtils.hash(testArray[j]) != MathUtils.hash(testArray[i]));
                }
            }
        }
    }

    @Test
    public void testArrayHash() {
        Assert.assertEquals(0, MathUtils.hash((double[]) null));
        Assert.assertEquals(MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }),
                     MathUtils.hash(new double[] {
                                      Double.NaN, Double.POSITIVE_INFINITY,
                                      Double.NEGATIVE_INFINITY, 1d, 0d
                                    }));
        Assert.assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { FastMath.nextAfter(1d, 2d) }));
        Assert.assertFalse(MathUtils.hash(new double[] { 1d }) ==
                    MathUtils.hash(new double[] { 1d, 1d }));
    }

    /**
     * Make sure that permuted arrays do not hash to the same value.
     */
    @Test
    public void testPermutedArrayHash() {
        double[] original = new double[10];
        double[] permuted = new double[10];
        RandomDataGenerator random = new RandomDataGenerator(100);

        // Generate 10 distinct random values
        for (int i = 0; i < 10; i++) {
            original[i] = random.nextUniform(i + 0.5, i + 0.75);
        }

        // Generate a random permutation, making sure it is not the identity
        boolean isIdentity = true;
        do {
            int[] permutation = random.nextPermutation(10, 10);
            for (int i = 0; i < 10; i++) {
                if (i != permutation[i]) {
                    isIdentity = false;
                }
                permuted[i] = original[permutation[i]];
            }
        } while (isIdentity);

        // Verify that permuted array has different hash
        Assert.assertFalse(MathUtils.hash(original) == MathUtils.hash(permuted));
    }

    @Test
    public void testIndicatorByte() {
        Assert.assertEquals((byte)1, MathUtils.copySign((byte)1, (byte)2));
        Assert.assertEquals((byte)1, MathUtils.copySign((byte)1, (byte)0));
        Assert.assertEquals((byte)(-1), MathUtils.copySign((byte)1, (byte)(-2)));
    }

    @Test
    public void testIndicatorInt() {
        Assert.assertEquals(1, MathUtils.copySign(1, 2));
        Assert.assertEquals(1, MathUtils.copySign(1, 0));
        Assert.assertEquals((-1), MathUtils.copySign(1, -2));
    }

    @Test
    public void testIndicatorLong() {
        Assert.assertEquals(1L, MathUtils.copySign(1L, 2L));
        Assert.assertEquals(1L, MathUtils.copySign(1L, 0L));
        Assert.assertEquals(-1L, MathUtils.copySign(1L, -2L));
    }

    @Test
    public void testIndicatorShort() {
        Assert.assertEquals((short)1, MathUtils.copySign((short)1, (short)2));
        Assert.assertEquals((short)1, MathUtils.copySign((short)1, (short)0));
        Assert.assertEquals((short)(-1), MathUtils.copySign((short)1, (short)(-2)));
    }

    @Test
    public void testNormalizeAngle() {
        for (double a = -15.0; a <= 15.0; a += 0.1) {
            for (double b = -15.0; b <= 15.0; b += 0.2) {
                double c = MathUtils.normalizeAngle(a, b);
                Assert.assertTrue((b - FastMath.PI) <= c);
                Assert.assertTrue(c <= (b + FastMath.PI));
                double twoK = FastMath.rint((a - c) / FastMath.PI);
                Assert.assertEquals(c, a - twoK * FastMath.PI, 1.0e-14);
            }
        }
    }

    @Test
    public void testFieldNormalizeAngle() {
        doTestFieldNormalizeAngle(Binary64Field.getInstance());
    }

    private <T extends CalculusFieldElement<T>> void doTestFieldNormalizeAngle(final Field<T> field) {
        final T zero = field.getZero();
        for (double a = -15.0; a <= 15.0; a += 0.1) {
            for (double b = -15.0; b <= 15.0; b += 0.2) {
                T c = MathUtils.normalizeAngle(zero.add(a), zero.add(b));
                double cR = c.getReal();
                Assert.assertTrue((b - FastMath.PI) <= cR);
                Assert.assertTrue(cR <= (b + FastMath.PI));
                double twoK = FastMath.rint((a - cR) / FastMath.PI);
                Assert.assertEquals(cR, a - twoK * FastMath.PI, 1.0e-14);
            }
        }
    }

    @Test
    public void testReduce() {
        final double period = -12.222;
        final double offset = 13;

        final double delta = 1.5;

        double orig = offset + 122456789 * period + delta;
        double expected = delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-7);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-7);

        orig = offset - 123356789 * period - delta;
        expected = FastMath.abs(period) - delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-6);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-6);

        orig = offset - 123446789 * period + delta;
        expected = delta;
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, period, offset),
                            1e-6);
        Assert.assertEquals(expected,
                            MathUtils.reduce(orig, -period, offset),
                            1e-6);

        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, Double.NaN, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.NaN, period, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, period, Double.NaN)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig, period,
                Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                period, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig,
                Double.POSITIVE_INFINITY, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(orig,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                period, Double.POSITIVE_INFINITY)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY, offset)));
        Assert.assertTrue(Double.isNaN(MathUtils.reduce(Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY,  Double.POSITIVE_INFINITY)));
    }

    @Test
    public void testReduceComparedWithNormalizeAngle() {
        final double tol = Math.ulp(1d);
        final double period = 2 * Math.PI;
        for (double a = -15; a <= 15; a += 0.5) {
            for (double center = -15; center <= 15; center += 1) {
                final double nA = MathUtils.normalizeAngle(a, center);
                final double offset = center - Math.PI;
                final double r = MathUtils.reduce(a, period, offset);
                Assert.assertEquals(nA, r + offset, tol);
            }
        }
    }

    @Test
    public void testSignByte() {
        final byte one = (byte) 1;
        Assert.assertEquals((byte) 1, MathUtils.copySign(one, (byte) 2));
        Assert.assertEquals((byte) (-1), MathUtils.copySign(one, (byte) (-2)));
    }

    @Test
    public void testSignInt() {
        final int one = 1;
        Assert.assertEquals(1, MathUtils.copySign(one, 2));
        Assert.assertEquals((-1), MathUtils.copySign(one, -2));
    }

    @Test
    public void testSignLong() {
        final long one = 1L;
        Assert.assertEquals(1L, MathUtils.copySign(one, 2L));
        Assert.assertEquals(-1L, MathUtils.copySign(one, -2L));
    }

    @Test
    public void testSignShort() {
        final short one = (short) 1;
        Assert.assertEquals((short) 1, MathUtils.copySign(one, (short) 2));
        Assert.assertEquals((short) (-1), MathUtils.copySign(one, (short) (-2)));
    }

    @Test
    public void testCheckFinite() {
        try {
            MathUtils.checkFinite(Double.POSITIVE_INFINITY);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            Assert.assertEquals(LocalizedCoreFormats.NOT_FINITE_NUMBER, e.getSpecifier());
        }
        try {
            MathUtils.checkFinite(Double.NEGATIVE_INFINITY);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            Assert.assertEquals(LocalizedCoreFormats.NOT_FINITE_NUMBER, e.getSpecifier());
        }
        try {
            MathUtils.checkFinite(Double.NaN);
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            Assert.assertEquals(LocalizedCoreFormats.NOT_FINITE_NUMBER, e.getSpecifier());
        }

        try {
            MathUtils.checkFinite(new double[] {0, -1, Double.POSITIVE_INFINITY, -2, 3});
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            Assert.assertEquals(LocalizedCoreFormats.NOT_FINITE_NUMBER, e.getSpecifier());
        }
        try {
            MathUtils.checkFinite(new double[] {1, Double.NEGATIVE_INFINITY, -2, 3});
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            Assert.assertEquals(LocalizedCoreFormats.NOT_FINITE_NUMBER, e.getSpecifier());
        }
        try {
            MathUtils.checkFinite(new double[] {4, 3, -1, Double.NaN, -2, 1});
            Assert.fail("an exception should have been thrown");
        } catch (MathIllegalArgumentException e) {
            Assert.assertEquals(LocalizedCoreFormats.NOT_FINITE_NUMBER, e.getSpecifier());
        }
    }

    @Test
    public void testCheckNotNull1() {
        try {
            Object obj = null;
            MathUtils.checkNotNull(obj);
        } catch (NullArgumentException e) {
            // Expected.
        }
    }

    @Test
    public void testCheckNotNull2() {
        try {
            double[] array = null;
            MathUtils.checkNotNull(array, LocalizedCoreFormats.INPUT_ARRAY);
        } catch (NullArgumentException e) {
            // Expected.
        }
    }

    @Test
    public void testCopySignByte() {
        byte a = MathUtils.copySign(Byte.MIN_VALUE, (byte) -1);
        Assert.assertEquals(Byte.MIN_VALUE, a);

        final byte minValuePlusOne = Byte.MIN_VALUE + (byte) 1;
        a = MathUtils.copySign(minValuePlusOne, (byte) 1);
        Assert.assertEquals(Byte.MAX_VALUE, a);

        a = MathUtils.copySign(Byte.MAX_VALUE, (byte) -1);
        Assert.assertEquals(minValuePlusOne, a);

        final byte one = 1;
        byte val = -2;
        a = MathUtils.copySign(val, one);
        Assert.assertEquals(-val, a);

        final byte minusOne = -one;
        val = 2;
        a = MathUtils.copySign(val, minusOne);
        Assert.assertEquals(-val, a);

        val = 0;
        a = MathUtils.copySign(val, minusOne);
        Assert.assertEquals(val, a);

        val = 0;
        a = MathUtils.copySign(val, one);
        Assert.assertEquals(val, a);
    }

    @Test(expected=MathRuntimeException.class)
    public void testCopySignByte2() {
        MathUtils.copySign(Byte.MIN_VALUE, (byte) 1);
    }

    /**
     * Tests {@link TwoSum#twoSum(double, double)}.
     */
    @Test
    public void testTwoSum() {
        //      sum = 0.30000000000000004
        // residual = -2.7755575615628914E-17
        final double a1 = 0.1;
        final double b1 = 0.2;
        final SumAndResidual result1 = MathUtils.twoSum(a1, b1);
        Assert.assertEquals(a1 + b1, result1.getSum(), 0.);
        Assert.assertEquals(a1 + b1, result1.getSum() + result1.getResidual(), 0.);
        Assert.assertNotEquals(0., result1.getResidual(), 0.);

        //      sum = -1580.3205849419005
        // residual = -1.1368683772161603E-13
        final double a2 = -615.7212034581913;
        final double b2 = -964.5993814837093;
        final SumAndResidual result2 = MathUtils.twoSum(a2, b2);
        Assert.assertEquals(a2 + b2, result2.getSum(), 0.);
        Assert.assertEquals(a2 + b2, result2.getSum() + result2.getResidual(), 0.);
        Assert.assertNotEquals(0., result2.getResidual(), 0.);

        //      sum = 251.8625825973395
        // residual = 1.4210854715202004E-14
        final double a3 = 60.348375484313706;
        final double b3 = 191.5142071130258;
        final SumAndResidual result3 = MathUtils.twoSum(a3, b3);
        Assert.assertEquals(a3 + b3, result3.getSum(), 0.);
        Assert.assertEquals(a3 + b3, result3.getSum() + result3.getResidual(), 0.);
        Assert.assertNotEquals(0., result3.getResidual(), 0.);

        //      sum = 622.8319023175123
        // residual = -4.3315557304163255E-14
        final double a4 = 622.8314146170453;
        final double b4 = 0.0004877004669900762;
        final SumAndResidual result4 = MathUtils.twoSum(a4, b4);
        Assert.assertEquals(a4 + b4, result4.getSum(), 0.);
        Assert.assertEquals(a4 + b4, result4.getSum() + result4.getResidual(), 0.);
        Assert.assertNotEquals(0., result4.getResidual(), 0.);
    }

    /**
     * Tests {@link TwoSum#twoSum(CalculusFieldElement, CalculusFieldElement)}.
     */
    @Test
    public void testTwoSumField() {
        final Tuple a = new Tuple(0.1, -615.7212034581913, 60.348375484313706, 622.8314146170453);
        final Tuple b = new Tuple(0.2, -964.5993814837093, 191.5142071130258, 0.0004877004669900762);
        final FieldSumAndResidual<Tuple> result = MathUtils.twoSum(a, b);
        for (int i = 0; i < a.getDimension(); ++i) {
            Assert.assertEquals(a.getComponent(i) + b.getComponent(i), result.getSum().getComponent(i), 0.);
            Assert.assertEquals(a.getComponent(i) + b.getComponent(i),
                    result.getSum().getComponent(i) + result.getResidual().getComponent(i), 0.);
            Assert.assertNotEquals(0., result.getResidual().getComponent(i), 0.);
        }
    }

}
