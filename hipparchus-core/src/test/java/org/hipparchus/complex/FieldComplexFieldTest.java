/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
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

/*
 * This is not the original file distributed by the Apache Software Foundation
 * It has been modified by the Hipparchus project
 */
package org.hipparchus.complex;

import java.util.HashMap;
import java.util.Map;

import org.hipparchus.Field;
import org.hipparchus.util.Decimal64;
import org.hipparchus.util.Decimal64Field;
import org.junit.Assert;
import org.junit.Test;

public class FieldComplexFieldTest {

    @Test
    public void testZero() {
        Assert.assertEquals(new FieldComplex<>(Decimal64.ZERO), FieldComplexField.getField(Decimal64Field.getInstance()).getZero());
    }

    @Test
    public void testOne() {
        Assert.assertEquals(new FieldComplex<>(Decimal64.ONE), FieldComplexField.getField(Decimal64Field.getInstance()).getOne());
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testMap() {
        Map<Field<?>, Integer> map = new HashMap<>();
        for (int i = 1; i < 100; ++i) {
            map.put(new FieldComplex<>(new Decimal64(i)).getField(), 0);
        }
        // there should be only one field for all values
        FieldComplexField<Decimal64> field = FieldComplexField.getField(Decimal64Field.getInstance());
        Assert.assertEquals(1, map.size());
        Assert.assertTrue(field.equals(map.entrySet().iterator().next().getKey()));
        Assert.assertFalse(field.equals(Decimal64Field.getInstance()));
    }

    @Test
    public void testRunTimeClass() {
        Assert.assertEquals(Complex.class, ComplexField.getInstance().getRuntimeClass());
    }

}
