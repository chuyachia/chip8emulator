package com.chuyachia.chip8emulator;

public class Stack {
    private final byte STACK_LEVEL = 16;
    private byte pointer;
    private final int[] stack;

    public Stack() {
        this.pointer = 0;
        this.stack = new int[STACK_LEVEL];
    }

    public int pop() throws Exception {
        if (pointer == 0) {
            throw new Exception("Stack is empty");
        }

        return stack[pointer--];
    }

    public void push(int value) throws Exception{
        if (pointer >= STACK_LEVEL-1) {
            throw new Exception("Stack overflow");
        }

        stack[++pointer] = value;
    }

}
