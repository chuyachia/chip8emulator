package com.chuyachia.chip8emulator;

import java.util.Arrays;

public class Memory {

    public final static int SPRITE_SIZE = 5;
    private final static int MEMORY_SIZE = 4096;
    private final static int MEMORY_START = 0x200;
    private final static byte[][] DEFAULT_SPRITES = {
            {(byte) 0xF0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xF0},
            {(byte) 0x20, (byte) 0x60, (byte) 0x20, (byte) 0x20, (byte) 0x70},
            {(byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x80, (byte) 0xF0},
            {(byte) 0xF0, (byte) 0x10, (byte) 0xF0, (byte) 0x10, (byte) 0xF0},
            {(byte) 0x90, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0x10},
            {(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x10, (byte) 0xF0},
            {(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x90, (byte) 0xF0},
            {(byte) 0xF0, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x40},
            {(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0xF0},
            {(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x10, (byte) 0xF0},
            {(byte) 0xF0, (byte) 0x90, (byte) 0xF0, (byte) 0x90, (byte) 0x90},
            {(byte) 0xE0, (byte) 0x90, (byte) 0xE0, (byte) 0x90, (byte) 0xE0},
            {(byte) 0xF0, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0xF0},
            {(byte) 0xE0, (byte) 0x90, (byte) 0x90, (byte) 0x90, (byte) 0xE0},
            {(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0xF0},
            {(byte) 0xF0, (byte) 0x80, (byte) 0xF0, (byte) 0x80, (byte) 0x80}
    };

    private short programCounter;
    private final byte[] memory;

    public Memory() {
        this.programCounter = MEMORY_START;
        this.memory = new byte[MEMORY_SIZE];
        loadSpritesInReservedMemory();
    }

    public void loadGame(byte[] rom)  {
        int currentMemory = MEMORY_START;

        for (byte b : rom) {
            memory[currentMemory] = b;
            currentMemory++;
        }
    }

    public int availableMemory() {
        return MEMORY_SIZE - MEMORY_START;
    }

    public void increasePC(int n) {
        programCounter = (short) ((programCounter & 0xffff) + n);
    }

    public short getPC() {
        return programCounter;
    }

    public void setPC(short n) {
        programCounter = n;
    }

    public byte getByte(int i) {
        return memory[i];
    }

    public void setByte(int i, byte b) {
        memory[i] = b;
    }

    public byte[] getMemory() {
        return memory;
    }

    public void setMemory(byte[] values) {
        for (int i = 0 ; i < values.length; i++) {
            this.memory[i] = values[i];
        }
    }

    public short nextInstruction() {
        short instruction = constructInstruction(memory[programCounter], memory[++programCounter]);
        programCounter++;

        return instruction;
    }

    public void clear() {
        programCounter = MEMORY_START;
        Arrays.fill(memory, (byte) 0);
        loadSpritesInReservedMemory();
    }

    private void loadSpritesInReservedMemory() {
        int i = 0;
        for (byte[] sprite : DEFAULT_SPRITES) {
            for (byte b : sprite) {
                memory[i] = b;
                i++;
            }
        }

    }

    private short constructInstruction(byte b1, byte b2) {
        short combined = (short) ((b1 & 0xff) << 8 | (b2 & 0xff));
        return combined;
    }
}
