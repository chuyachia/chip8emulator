package com.chuyachia.chip8emulator;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StackTest {
    private Stack stack;

    @Before
    public  void setup() {
        stack = new Stack();
    }

    @Test
    public void testValidPush() throws Exception {
        for (int i = 0; i < stack.STACK_LEVEL; i++) {
            stack.push((short) i);
        }

        assertEquals(15, stack.peek());
    }

    @Test(expected = Exception.class)
    public void testInvalidPush() throws Exception {
        for (int i = 0; i < stack.STACK_LEVEL + 1; i++) {
            stack.push((short) i);
        }
    }

    @Test
    public void testPeek() throws Exception {
        stack.push((short) 2);
        stack.push((short) 3);
        assertEquals(3, stack.peek());
        assertEquals(3, stack.peek());
    }

    @Test
    public void testPop() throws Exception {
        stack.push((short) 2);
        stack.push((short) 3);
        short val = stack.pop();
        assertEquals(3, val);
        assertEquals(2, stack.peek());
    }

}
