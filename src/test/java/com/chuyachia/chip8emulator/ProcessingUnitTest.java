package com.chuyachia.chip8emulator;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ProcessingUnitTest {

    private static ProcessingUnit processingUnit;
    private static Memory memory;

    @BeforeClass
    public static void setup() {
        Screen screen = new Screen();
        Keyboard keyboard = new Keyboard();
        memory = new Memory();
        processingUnit = new ProcessingUnit(memory, screen, keyboard);
    }

    @Test
    public void testJumpToNnnn() {
        short nnnn = 0x0fae;
        processingUnit.jumpToNnnn(nnnn);
        assertEquals(nnnn, memory.getPC());
    }
    // TODO add tests for other instruction methods
}
