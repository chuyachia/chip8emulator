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
    private final static String DEFAULT_MESSAGE = "<HTML>Choose a ROM file to start<br/></HTML>";
    private final static String FILE_TOO_LARGE_MESSAGE = "<HTML>The chosen file is too large for a Chip8 machine.<br/> Please choose another one</HTML>";

    private final Chip8 chip8;
    private JLabel title;
    private JLabel label;
    private JFileChooser fc;

    public Home(Chip8 chip8) {
        this.chip8 = chip8;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE));


        addTitle();
        addInputs();
        addDetail();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Screen.WIDTH * chip8.getScreenScaleFactor(), Screen.HEIGHT * chip8.getScreenScaleFactor());
    }

    private void addTitle() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        title = new JLabel("Welcome to Chip8 emulator");
        title.setFont(new Font(FONT, Font.BOLD,20));
        panel.add(title);
        this.add(panel, BorderLayout.NORTH);
    }

    private void addInputs() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel fileChooser = fileChooserPanel();
        JPanel screenSize = screenSizePanel();
        JPanel emulationSpeed = emulationSpeedPanel();
        panel.add(fileChooser);
        panel.add(screenSize);
        panel.add(emulationSpeed);

        this.add(panel, BorderLayout.CENTER);
    }

    private JPanel fileChooserPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        label = new JLabel(DEFAULT_MESSAGE);
        label.setFont(new Font(FONT, Font.PLAIN,18));
        panel.add(label);

        JButton button = new JButton("Browse File");
        button.setFont(new Font(FONT, Font.PLAIN,18));
        panel.add(button);
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

        this.fc = new JFileChooser();

        return panel;
    }

    private void addDetail() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel detail = new JLabel("Press ESC during emulation to stop");
        detail.setFont(new Font(FONT, Font.ITALIC, 12));
        panel.add(detail);
        this.add(panel,BorderLayout.SOUTH);
    }

    private JPanel screenSizePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel spinnerLabel = new JLabel("Choose screen size");
        spinnerLabel.setFont(new Font(FONT, Font.PLAIN,18));
        panel.add(spinnerLabel);
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(10, 10, 50, 10));
        spinner.addChangeListener(changeEvent -> {
            int screenScaleFactor = (int) spinner.getValue();
            this.chip8.setScreenScaleFactor(screenScaleFactor);
            JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            topFrame.pack();
        });
        panel.add(spinner);
        return panel;
    }

    private JPanel emulationSpeedPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel sliderLabel = new JLabel("Choose emulation clock speed (Hz)");
        sliderLabel.setFont(new Font(FONT, Font.PLAIN,18));
        panel.add(sliderLabel);
        JSlider slider = new JSlider(400, 900, 500);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTrack(true);
        slider.setMajorTickSpacing(100);
        slider.setMinorTickSpacing(50);
        slider.addChangeListener(changeEvent -> {
            int clockRate = slider.getValue();
            this.chip8.setClockRate(clockRate);
        });
        panel.add(slider);
        return panel;
    }
}
