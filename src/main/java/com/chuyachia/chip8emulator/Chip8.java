package com.chuyachia.chip8emulator;

public class Chip8 {

    private final Memory memory;
    private final Screen screen;
    private final Keyboard keyboard;
    private final ProcessingUnit processingUnit;

    public Chip8() {
        memory = new Memory();
        screen = new Screen(memory);
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
}
