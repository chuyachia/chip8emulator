package com.chuyachia.chip8emulator;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstructionTest {

    @Test
    public void testCLS() {
        short instruction = 0x00e0;
        Instruction matched = Instruction.getMatched(instruction);
        short[] arguments = matched.getArguments(instruction);
        assertEquals(Instruction.CLS, matched);
        assertEquals(0, arguments.length);
    }

    @Test
    public void testRET() {
        short instruction = 0x00ee;
        Instruction matched = Instruction.getMatched(instruction);
        short[] arguments = matched.getArguments(instruction);
        assertEquals(Instruction.RET, matched);
        assertEquals(0, arguments.length);
    }

    @Test
    public void testJP_ADDR() {
        short instruction = 0x1f2a;
        Instruction matched = Instruction.getMatched(instruction);
        short[] arguments = matched.getArguments(instruction);
        short nnnn = Instruction.getNnnn(arguments);
        assertEquals(Instruction.JP_ADDR, matched);
        assertEquals(1, arguments.length);
        assertEquals(0x0f2a, nnnn);
    }

    @Test
    public void testCALL_ADDR() {
        short instruction = 0x2ea0;
        Instruction matched = Instruction.getMatched(instruction);
        short[] arguments = matched.getArguments(instruction);
        short nnnn = Instruction.getNnnn(arguments);
        assertEquals(Instruction.CALL_ADDR, matched);
        assertEquals(1, arguments.length);
        assertEquals(0x0ea0, nnnn);
    }

    // TODO add tests for other instructions
}
