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
    private final static String DEFAULT_ROM_CHOOSER_MESSAGE = "Choose a ROM file to start new game";
    private final static String FILE_TOO_LARGE_MESSAGE = "<HTML>The chosen file is too large for a Chip8 machine.<br/> Please choose another one</HTML>";
    private final static String ERROR_LOADING_ROM = "<HTML>The chosen file cannot be loaded.<br/> Please choose another one</HTML>";
    private final static String DEFAULT_SAVE_FILE_CHOOSER_MESSAGE = "Choose a save file to restore game";
    private final static String SAVE_FILE_NOT_VALID = "<HTML>The chosen file is not a valid save file.<br/> Please try another one</HTML>";

    private final Chip8 chip8;
    private JLabel title;
    private JLabel chooseRomlabel;
    private JLabel chooseSaveFilelabel;
    private JFileChooser fc;

    public Home(Chip8 chip8) {
        this.chip8 = chip8;
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE));
        fc = new JFileChooser();
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
        title.setFont(new Font(FONT, Font.BOLD, 20));
        panel.add(title);
        this.add(panel, BorderLayout.NORTH);
    }

    private void addInputs() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel romChooser = romChooserPanel();
        JPanel saveFileChooser = saveFileChooserPanel();
        JPanel screenSize = screenSizePanel();
        JPanel emulationSpeed = emulationSpeedPanel();
        panel.add(romChooser);
        panel.add(saveFileChooser);
        panel.add(screenSize);
        panel.add(emulationSpeed);
        this.add(panel, BorderLayout.CENTER);
    }

    private JPanel romChooserPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        chooseRomlabel = new JLabel(DEFAULT_ROM_CHOOSER_MESSAGE);
        chooseRomlabel.setFont(new Font(FONT, Font.PLAIN, 18));
        panel.add(chooseRomlabel);

        JButton button = new JButton("Browse File");
        button.setFont(new Font(FONT, Font.PLAIN, 18));
        panel.add(button);

        button.addActionListener(actionEvent -> {
            int useInteraction = fc.showOpenDialog(this);
            if (useInteraction == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (f.length() > chip8.availableMemory()) {
                    chooseRomlabel.setText(FILE_TOO_LARGE_MESSAGE);
                } else {
                    boolean gameLoaded = true;
                    try {
                        byte[] game = Files.readAllBytes(f.toPath());
                        chip8.loadGame(game);
                    } catch (IOException e) {
                        chooseRomlabel.setText(ERROR_LOADING_ROM);
                        gameLoaded = false;
                    }

                    if (gameLoaded) {
                        Container parentContainer = this.getParent();
                        CardLayout cardLayout = (CardLayout) parentContainer.getLayout();
                        cardLayout.show(parentContainer, Screen.PANEL_NAME);
                        chooseRomlabel.setText(DEFAULT_ROM_CHOOSER_MESSAGE);

                        new Thread(() -> chip8.startEmulation()).start();
                    }
                }
            }
        });


        return panel;
    }

    private JPanel saveFileChooserPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        chooseSaveFilelabel = new JLabel(DEFAULT_SAVE_FILE_CHOOSER_MESSAGE);
        chooseSaveFilelabel.setFont(new Font(FONT, Font.PLAIN, 18));
        panel.add(chooseSaveFilelabel);

        JButton button = new JButton("Browse File");
        button.setFont(new Font(FONT, Font.PLAIN, 18));
        panel.add(button);

        button.addActionListener(actionEvent -> {
            int useInteraction = fc.showOpenDialog(this);
            if (useInteraction == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                boolean saveFileLoaded = true;
                try {
                    chip8.restoreSavedGame(f);
                } catch (IOException e) {
                    chooseSaveFilelabel.setText(SAVE_FILE_NOT_VALID);
                    saveFileLoaded = false;
                } catch (ClassNotFoundException e) {
                    chooseSaveFilelabel.setText(SAVE_FILE_NOT_VALID);
                    saveFileLoaded = false;
                }

                if (saveFileLoaded) {
                    Container parentContainer = this.getParent();
                    CardLayout cardLayout = (CardLayout) parentContainer.getLayout();
                    cardLayout.show(parentContainer, Screen.PANEL_NAME);
                    chooseSaveFilelabel.setText(DEFAULT_SAVE_FILE_CHOOSER_MESSAGE);

                    new Thread(() -> chip8.startEmulation()).start();
                }
            }
        });

        return panel;
    }

    private void addDetail() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel detail = new JLabel("<HTML>Press ESC during emulation to stop<br/>Press SPACE during emulation to save</HTML>");
        detail.setFont(new Font(FONT, Font.ITALIC, 12));
        panel.add(detail);
        this.add(panel, BorderLayout.SOUTH);
    }

    private JPanel screenSizePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel spinnerLabel = new JLabel("Choose screen size");
        spinnerLabel.setFont(new Font(FONT, Font.PLAIN, 18));
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
        sliderLabel.setFont(new Font(FONT, Font.PLAIN, 18));
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
