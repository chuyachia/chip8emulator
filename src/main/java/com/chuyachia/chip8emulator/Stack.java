package com.chuyachia.chip8emulator;

import java.io.Serializable;

public class Stack implements Serializable {
    private final byte STACK_LEVEL = 16;
    private byte pointer;
    private final short[] stack;

    public Stack() {
        this.pointer = 0;
        this.stack = new short[STACK_LEVEL];
    }

    public short pop() throws Exception {
        if (pointer == 0) {
            throw new Exception("Stack is empty");
        }

        return stack[pointer--];
    }

    public void push(short value) throws Exception{
        if (pointer >= STACK_LEVEL-1) {
            throw new Exception("Stack overflow");
        }

        stack[++pointer] = value;
    }

}
