package com.chuyachia.chip8emulator;

public class Memory {
    private final static int MEMORY_START = 0x200;
    private int programCounter;
    private final byte[] memory;
    private int currentGameEnd;

    public Memory() {
        this.programCounter = MEMORY_START;
        this.memory = new byte[4096];
    }

    public void loadGame(byte[] rom) {
        int currentMemory = MEMORY_START;
        for (byte b : rom) {
            memory[currentMemory] = b;
            currentMemory++;
        }
        currentGameEnd = currentMemory;
    }

    public void increasePC(int n) {
        programCounter += n;
    }

    public int getPC() {
        return programCounter;
    }

    public void setPC(int n) {
        programCounter = n;
    }

    public boolean hasNextInstruction() {
        return programCounter < currentGameEnd;
    }

    public short nextInstruction() {
        short instruction = constructInstruction(memory[programCounter], memory[++programCounter]);
        programCounter++;

        return instruction;
    }

    private short constructInstruction(byte b1, byte b2) {
//        System.out.println(Integer.toBinaryString((b1 & 0xff)));
//        System.out.println(Integer.toBinaryString(b2 & 0xff));
        short combined = (short) ((b1 & 0xff) << 8 | (b2 & 0xff));
        return combined;
    }
}
