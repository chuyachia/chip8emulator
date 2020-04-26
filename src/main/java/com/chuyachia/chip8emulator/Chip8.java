package com.chuyachia.chip8emulator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Chip8 {
    public static void main(String[] args) {
        byte[] rom = null;
        try {
            rom = loadRom();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Memory memory = new Memory();
        memory.loadGame(rom);

        ProcessingUnit processingUnit = new ProcessingUnit(memory);
        while (memory.hasNextInstruction()) {
            processingUnit.instructionCycle();
        }
    }

    private static byte[] loadRom() throws IOException {
        Path path = Paths.get("rom/15PUZZLE");
        return Files.readAllBytes(path);
    }
}
