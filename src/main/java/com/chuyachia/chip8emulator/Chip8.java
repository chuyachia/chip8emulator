package com.chuyachia.chip8emulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

public class Chip8 {

    private final Memory memory;
    private final Screen screen;
    private final Keyboard keyboard;
    private final ProcessingUnit processingUnit;

    public Chip8() {
        memory = new Memory();
        screen = new Screen();
        keyboard = new Keyboard();
        processingUnit = new ProcessingUnit(memory, screen, keyboard);
    }


    public int availableMemory() {
        return memory.availableMemory();
    }

    public void loadGame(byte[] bytes) {
        memory.loadGame(bytes);
    }

    public void startEmulation() {
        keyboard.escapePressed.set(false);
        processingUnit.start();
    }

    public Screen getScreen() {
        return screen;
    }

    public int getScreenScaleFactor() {
        return screen.getScaleFactor();
    }

    public void setScreenScaleFactor(int factor) {
        screen.setScaleFactor(factor);
    }

    public void setClockRate(int rate) {
        this.processingUnit.setClockRate(rate);
    }

    public void restoreSavedGame(File file) throws IOException, ClassNotFoundException {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             ObjectInput objectInput = new ObjectInputStream(fileInputStream)) {
            byte[] V = (byte[]) objectInput.readObject();
            short I = (short) objectInput.readObject();
            byte DT = (byte) objectInput.readObject();
            byte ST = (byte) objectInput.readObject();
            Stack stack = (Stack) objectInput.readObject();
            short PC = (short) objectInput.readObject();
            byte[] memory = (byte[]) objectInput.readObject();
            byte[][] pixels = (byte[][]) objectInput.readObject();
            boolean collision = (boolean) objectInput.readObject();

            this.processingUnit.setV(V);
            this.processingUnit.setI(I);
            this.processingUnit.setST(ST);
            this.processingUnit.setDT(DT);
            this.processingUnit.setStack(stack);

            this.memory.setPC(PC);
            this.memory.setMemory(memory);

            this.screen.setPixels(pixels);
            this.screen.setRepaintFlag(true);
            this.screen.setCollision(collision);
        }
    }
}
