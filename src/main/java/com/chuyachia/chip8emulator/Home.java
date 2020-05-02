package com.chuyachia.chip8emulator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Home extends JPanel {

    public final static String PANEL_NAME = "Home";
    private final static String FONT = "Courier";
    private final static int PADDING_SIZE = 20;
    private final static String DEFAULT_MESSAGE = "Choose a ROM file to start";
    private final static String FILE_TOO_LARGE_MESSAGE = "<HTML>The chosen file is too large for a Chip8 machine.<br/> Please choose another one</HTML>";

    private final Chip8 chip8;
    private final JLabel title;
    private final JLabel label;
    private final JButton button;
    private final JLabel detail;
    private final JFileChooser fc;

    public Home(Chip8 chip8) {
        this.chip8 = chip8;
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE));
        this.title = new JLabel("Welcome to Chip8 emulator");
        this.label = new JLabel(DEFAULT_MESSAGE);
        this.button = new JButton("Browse File");
        this.detail = new JLabel("Press ESC during emulation to stop");
        this.fc = new JFileChooser();

        addTitle();
        addLabel();
        addButton();
        addDetail();
        attacheActionListener();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Screen.WIDTH * Screen.SCALE_FACTOR, Screen.HEIGHT * Screen.SCALE_FACTOR);
    }

    private void addTitle() {
        this.title.setFont(new Font(FONT, Font.BOLD,20));
        this.title.setBorder(BorderFactory.createEmptyBorder(0,0,PADDING_SIZE,0 ));
        this.add(title);
    }

    private void addLabel() {
        this.label.setFont(new Font(FONT, Font.PLAIN,18));
        this.label.setBorder(BorderFactory.createEmptyBorder(0,0,PADDING_SIZE,0));
        this.add(label);
    }

    private void addButton() {
        this.button.setFont(new Font(FONT, Font.PLAIN,18));
        this.add(button);
    }

    private void addDetail() {
        this.detail.setFont(new Font(FONT, Font.ITALIC, 12));
        this.detail.setBorder(BorderFactory.createEmptyBorder(PADDING_SIZE,0,0,0));
        this.add(detail);
    }

    private void attacheActionListener() {
        button.addActionListener(actionEvent -> {
            int useInteraction = fc.showOpenDialog(this);
            if (useInteraction == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f.length() > chip8.availableMemory()) {
                    label.setText(FILE_TOO_LARGE_MESSAGE);
                } else {
                    try {
                        byte[] game = Files.readAllBytes(f.toPath());
                        chip8.loadGame(game);
                    } catch (IOException e) {
                        System.out.println("Error reading file");
                        e.printStackTrace();
                    }

                    Container parentContainer = this.getParent();
                    CardLayout cardLayout = (CardLayout) parentContainer.getLayout();
                    cardLayout.show(parentContainer, Screen.PANEL_NAME);
                    label.setText(DEFAULT_MESSAGE);

                    new Thread(() -> chip8.startEmulation()).start();
                }
            }
        });
    }

}
