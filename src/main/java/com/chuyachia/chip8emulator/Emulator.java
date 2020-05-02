package com.chuyachia.chip8emulator;

import javax.swing.*;
import java.awt.*;

public class Emulator {
    public static void main(String[] args) {
        Chip8 chip8 = new Chip8();
        Home home = new Home(chip8);

        JFrame frame= prepareFrame();
        frame.getContentPane().add(Home.PANEL_NAME, home);
        frame.getContentPane().add(Screen.PANEL_NAME, chip8.getScreen());
        frame.pack();
    }

    private static JFrame prepareFrame() {
        JFrame frame = new JFrame("Chip8 Emulator");
        Container container = frame.getContentPane();
        CardLayout cardLayout = new CardLayout();
        container.setLayout(cardLayout);


        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return frame;
    }
}
