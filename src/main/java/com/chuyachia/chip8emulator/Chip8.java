package com.chuyachia.chip8emulator;

import javax.swing.*;
import java.awt.*;
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
        try {
            memory.loadGame(rom);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Screen screen = new Screen(memory);
        prepareJFrameScreen(screen);
        Keyboard keyboard = new Keyboard();
        ProcessingUnit processingUnit = new ProcessingUnit(memory, screen,keyboard);

        processingUnit.start();
    }

    private static byte[] loadRom() throws IOException {
        Path path = Paths.get("rom/PONG");
        return Files.readAllBytes(path);
    }

    private static void prepareJFrameScreen(Screen screen) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(screen);
        frame.setVisible(true);
        frame.setSize(new Dimension(Screen.WIDTH * Screen.SCALE_FACTOR, Screen.HEIGHT* Screen.SCALE_FACTOR));
    }
}
