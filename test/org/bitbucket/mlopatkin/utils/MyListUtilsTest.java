/*
 * Copyright 2011 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.mlopatkin.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class MyListUtilsTest {

    @Test
    public void testMergeOrdered_emptyBoth() {
        genericTest(MyListUtilsTest.<Integer> list(), MyListUtilsTest.<Integer> list(), 0);
    }

    @Test
    public void testMergeOrdered_emptyBase() {
        genericTest(MyListUtilsTest.<Integer> list(), list(3, 4, 5), 0);
    }

    @Test
    public void testMergeOrdered_emptyElems() {
        genericTest(list(3, 4, 5), MyListUtilsTest.<Integer> list(), 3);
    }

    @Test
    public void testMergeOrdered_allToEnd() {
        genericTest(list(1, 2), list(3, 4, 5), 2);
    }

    @Test
    public void testMergeOrdered_allToBegin() {
        genericTest(list(3, 4, 5), list(1, 2), 0);
    }

    @Test
    public void testMergeOrdered_OddEven() {
        genericTest(list(1, 3, 5), list(2, 4), 1);
    }

    @Test
    public void testMergeOrdered_interval() {
        genericTest(list(1, 5), list(2, 4), 1);
    }

    @Test
    public void testMergeOrdered_intervals() {
        genericTest(list(1, 5, 9), list(2, 4, 6, 7, 8), 1);
    }

    @Test
    public void testMergeOrdered_intervalsEnd() {
        genericTest(list(1, 5, 9), list(2, 4, 6, 7, 8, 10, 12), 1);
    }

    @Test
    public void testMergeOrdered_intervalsBegin() {
        genericTest(list(1, 5, 9), list(-2, -1, 2, 4, 6, 7, 8, 10, 12), 0);
    }

    @Test
    public void testMergeOrdered_duplicates() {
        genericTest(list(1, 5, 9), list(2, 2, 2, 6, 6, 6, 6), 1);
    }

    @Test
    public void testMergeOrdered_duplicates2() {
        genericTest(list(1, 1, 5, 5, 9), list(2, 2, 2, 6, 6, 6, 6), 2);
    }

    @Test
    public void testMergeOrdered_duplicates3() {
        genericTest(list(1, 2, 3, 4, 5, 6), list(2, 2, 2, 6, 6, 6, 6), 2);
    }

    public <T extends Comparable<? super T>> void genericTest(List<T> base, List<T> elems,
            int expectedResultPos) {
        List<T> result = new ArrayList<T>(base);
        result.addAll(elems);
        Collections.sort(result);

        int resultPos = MyListUtils.mergeOrdered(base, elems);
        assertListEquals(result, base);
        assertEquals(expectedResultPos, resultPos);
    }

    private static <T> void assertListEquals(List<T> expected, List<T> actual) {
        if (expected == null) {
            if (actual == null) {
                return;
            } else {
                fail("null expected but found non-null");
            }
        }
        if (expected != null && actual == null) {
            fail("Non-null expected but found null");
        }
        Object[] expecteds = expected.toArray(), actuals = actual.toArray();
        assertArrayEquals(expecteds, actuals);
    }

    private static <T> ArrayList<T> list(T... args) {
        return new ArrayList<T>(Arrays.asList(args));
    }

    private static <T> ArrayList<T> list() {
        return new ArrayList<T>();
    }
}