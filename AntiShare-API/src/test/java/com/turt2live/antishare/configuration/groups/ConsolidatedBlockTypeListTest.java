/*******************************************************************************
 * Copyright (C) 2014 Travis Ralston (turt2live)
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.turt2live.antishare.configuration.groups;

import com.turt2live.antishare.engine.list.BlockTypeList;
import com.turt2live.antishare.object.ABlock;
import com.turt2live.antishare.object.attribute.TrackedState;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ConsolidatedBlockTypeListTest {

    private static class ReturnIsTrackedWorkaround implements Answer<Boolean> {

        @Override
        public Boolean answer(InvocationOnMock invocation) throws Throwable {
            // Workaround for ensuring isTracked() works
            return ((BlockTypeList) invocation.getMock()).getState((ABlock) invocation.getArguments()[0]) == TrackedState.INCLUDED;
        }
    }

    private static BlockTypeList list1, list2, list3, list4;
    private static ConsolidatedBlockTypeList consolidated;
    private static ABlock testBlock;

    @BeforeClass
    public static void beforeTest() {
        list1 = mock(BlockTypeList.class);
        list2 = mock(BlockTypeList.class);
        list3 = mock(BlockTypeList.class);
        list4 = mock(BlockTypeList.class);
        testBlock = mock(ABlock.class);

        consolidated = new ConsolidatedBlockTypeList(list1, list2, list3, list4);

        when(list1.isTracked(any(ABlock.class))).then(new ReturnIsTrackedWorkaround());
        when(list2.isTracked(any(ABlock.class))).then(new ReturnIsTrackedWorkaround());
        when(list3.isTracked(any(ABlock.class))).then(new ReturnIsTrackedWorkaround());
        when(list4.isTracked(any(ABlock.class))).then(new ReturnIsTrackedWorkaround());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmpty1() {
        new ConsolidatedBlockTypeList(new ArrayList<BlockTypeList>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateEmpty2() {
        new ConsolidatedBlockTypeList(new BlockTypeList[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull1() {
        new ConsolidatedBlockTypeList((List<BlockTypeList>) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNull2() {
        new ConsolidatedBlockTypeList((BlockTypeList[]) null);
    }

    @Test
    public void testBias() throws Exception {
        // Ensure equal balance of tracked & negated results in "not tracked"

        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);

        assertEquals(TrackedState.NOT_PRESENT, consolidated.getState(testBlock));
        assertEquals(false, consolidated.isTracked(testBlock));

        // When nothing is included, result should be "not tracked"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);

        assertEquals(TrackedState.NOT_PRESENT, consolidated.getState(testBlock));
        assertEquals(false, consolidated.isTracked(testBlock));

        // When at least one is included, result should be "included"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);

        assertEquals(TrackedState.INCLUDED, consolidated.getState(testBlock));
        assertEquals(true, consolidated.isTracked(testBlock));

        // Ensure equal balance of tracked & negated results in "not tracked"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);

        assertEquals(TrackedState.NOT_PRESENT, consolidated.getState(testBlock));
        assertEquals(false, consolidated.isTracked(testBlock));

        // When at least one is negated, result should be "negated"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);

        assertEquals(TrackedState.NEGATED, consolidated.getState(testBlock));
        assertEquals(false, consolidated.isTracked(testBlock));

        // When a majority are negated, the result should be "negated"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);

        assertEquals(TrackedState.NEGATED, consolidated.getState(testBlock));
        assertEquals(false, consolidated.isTracked(testBlock));

        // When a majority are negated, the result should be "negated"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);

        assertEquals(TrackedState.NEGATED, consolidated.getState(testBlock));
        assertEquals(false, consolidated.isTracked(testBlock));

        // When a majority are included, the result should be "included"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NEGATED);

        assertEquals(TrackedState.INCLUDED, consolidated.getState(testBlock));
        assertEquals(true, consolidated.isTracked(testBlock));

        // When a majority are included, the result should be "included"
        when(list1.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list2.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list3.getState(any(ABlock.class))).thenReturn(TrackedState.INCLUDED);
        when(list4.getState(any(ABlock.class))).thenReturn(TrackedState.NOT_PRESENT);

        assertEquals(TrackedState.INCLUDED, consolidated.getState(testBlock));
        assertEquals(true, consolidated.isTracked(testBlock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull1() {
        consolidated.isTracked(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull2() {
        consolidated.getState(null);
    }
}
