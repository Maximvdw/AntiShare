package com.turt2live.antishare.inventory.defaults;

import com.turt2live.antishare.inventory.ASItem;
import com.turt2live.antishare.testobjects.UnitASItem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.util.Random;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DefaultASItemTest {

    @Test
    public void aTestUnlimitedRange() {
        DefaultASItem item1 = new DefaultASItem(-1);
        assertEquals(-1, item1.getId());

        DefaultASItem item2 = new DefaultASItem(0);
        assertEquals(0, item2.getId());

        DefaultASItem item3 = new DefaultASItem(1);
        assertEquals(1, item3.getId());
    }

    @Test
    public void bTestClone() {
        DefaultASItem[] items = new DefaultASItem[10];
        DefaultASItem[] cloned = new DefaultASItem[items.length];

        Random random = new Random();
        for (int i = 0; i < items.length; i++) {
            items[i] = new DefaultASItem(random.nextInt(1000) * (random.nextBoolean() ? -1 : 1));
        }

        for (int i = 0; i < items.length; i++) {
            ASItem item = items[i].clone();
            assertTrue(item != null && item instanceof DefaultASItem);
            cloned[i] = (DefaultASItem) item;
        }

        for (int i = 0; i < items.length; i++) {
            DefaultASItem item1 = items[i];
            DefaultASItem item2 = cloned[i];

            assertEquals(item1.getId(), item2.getId());
        }
    }

    @Test
    public void cTestEquals() {
        DefaultASItem item1 = new DefaultASItem(12);
        DefaultASItem item2 = new DefaultASItem(12);
        DefaultASItem item3 = new DefaultASItem(13);

        assertTrue(item1.equals(item2));
        assertFalse(item1.equals(null));
        assertFalse(item1.equals(item3));
        assertTrue(item2.equals(item1));
        assertFalse(item1.equals(new UnitASItem(12)));
        assertFalse(item1.equals(new UnitASItem(13)));
    }

    @BeforeClass
    public static void preTest() {
    }

    @AfterClass
    public static void postTest() {
    }
}
