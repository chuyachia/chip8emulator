package com.chuyachia.chip8emulator;

public enum Instruction {

    CLS((short) 0x00E0, (short) 0xFFFF, new short[]{}),
    RET((short) 0x00EE, (short) 0xFFFF, new short[]{}),
    JP_ADDR((short) 0x1000, (short) 0xF000, new short[]{0x0FFF}),
    CALL_ADDR((short) 0x2000, (short) 0xF000, new short[]{0x0FFF}),
    SE_VX((short) 0x3000, (short) 0xF000, new short[]{0x0F00, 0x00FF}),
    SNE_VX((short) 0x4000, (short) 0xF000, new short[]{0x0F00, 0x00FF}),
    SE_VX_VY((short) 0x5000, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    LD_VX((short) 0x6000, (short) 0xF000, new short[]{0x0F00, 0x00FF}),
    ADD_VX((short) 0x7000, (short) 0xF000, new short[]{0x0F00, 0x00FF}),
    LD_VX_VY((short) 0x8000, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    OR_VX_VY((short) 0x8001, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    AND_VX_VY((short) 0x8002, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    XOR_VX_VY((short) 0x8003, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    ADD_VX_VY((short) 0x8004, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    SUB_VX_VY((short) 0x8005, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    SHR_VX_VY((short) 0x8006, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    SUBN_VX_VY((short) 0x8007, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    SHL_VX_VY((short) 0x800E, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    SNE_VX_VY((short) 0x9000, (short) 0xF00F, new short[]{0x0F00, 0x00F0}),
    LD_I_ADDR((short) 0xA000, (short) 0xF000, new short[]{0x0FFF}),
    JP_V0_ADDR((short) 0xB000, (short) 0xF000, new short[]{0x0FFF}),
    RND_VX((short) 0xC000, (short) 0xF000, new short[]{0x0F00, 0x00FF}),
    DRW_VX_VY((short) 0xD000, (short) 0xF000, new short[]{0x0F00, 0x00F0, 0x000F}),
    SKP_VX((short) 0xE09E, (short) 0xF0FF, new short[]{0x0F00}),
    SKNP_VX((short) 0xE0A1, (short) 0xF0FF, new short[]{0x0F00}),
    LD_VX_DT((short) 0xF007, (short) 0xF0FF, new short[]{0x0F00}),
    LD_VX_K((short) 0xF00A, (short) 0xF0FF, new short[]{0x0F00}),
    LD_DT_VX((short) 0xF015, (short) 0xF0FF, new short[]{0x0F00}),
    LD_ST_VX((short) 0xF018, (short) 0xF0FF, new short[]{0x0F00}),
    ADD_I_VX((short) 0xF01E, (short) 0xF0FF, new short[]{0x0F00}),
    LD_F_VX((short) 0xF029, (short) 0xF0FF, new short[]{0x0F00}),
    LD_B_VX((short) 0xF033, (short) 0xF0FF, new short[]{0x0F00}),
    LD_I_VX((short) 0xF055, (short) 0xF0FF, new short[]{0x0F00}),
    LD_VX_I((short) 0xF065, (short) 0xF0FF, new short[]{0x0F00});


    Instruction(short pattern, short patternMask, short[] valueMasks) {
        this.pattern = pattern;
        this.patternMask = patternMask;
        this.valueMasks = valueMasks;
    }

    private short pattern;
    private short patternMask;
    private short[] valueMasks;

    public static Instruction getMatched(short instruction) {
        Instruction instructionPattern = null;
        for (Instruction existingInstruction : Instruction.values()) {
            if (existingInstruction.match(instruction)) {
                instructionPattern = existingInstruction;
                break;
            }
        }

        return instructionPattern;
    }

    public static short getNnnn(short[] arguments) {
        return arguments[0];
    }

    public static byte getKk(short[] arguments) {
        return (byte) arguments[1];
    }

    public static byte getX(short[] arguments) {
        return (byte) arguments[0];
    }

    public static byte getY(short[] arguments) {
        return (byte) arguments[1];
    }

    public static byte getN(short[] arguments) {
        return (byte) arguments[2];
    }

    public short[] getArguments(short instruction) {
        short[] arguments = new short[valueMasks.length];
        for (short i = 0; i < valueMasks.length; i++) {
            short argumentValue = (short) (valueMasks[i] & instruction);
            short valueMask = valueMasks[i];
            // Get rid of trailing zeros
            while (valueMask % 16 == 0 && valueMask > 0) {
                argumentValue = (short) (argumentValue >>> 4);
                valueMask = (short) (valueMask >>> 4);
            }

            arguments[i] = argumentValue;
        }

        return arguments;
    }

    public boolean match(short instruction) {
        return (instruction & patternMask) == pattern;
    }
}
