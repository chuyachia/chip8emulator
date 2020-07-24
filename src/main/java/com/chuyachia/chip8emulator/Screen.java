package com.chuyachia.chip8emulator;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Screen extends JPanel {

    public static final String PANEL_NAME = "Chip8Screen";
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;
    public static final int BYTE_SIZE = 8;
    private final byte[][] pixels;
    private int scaleFactor = 10;
    private boolean collision;
    private boolean repaintFlag;

    public Screen() {
        this.pixels = new byte[HEIGHT][WIDTH / BYTE_SIZE];
    }

    public void display(byte x, byte y, byte data, int offset) {
        int xValue = x & 0xff;
        int yValue = y & 0xff;
        collision = false;

        int validYValue = (yValue + offset) % (HEIGHT);
        for (int i = BYTE_SIZE - 1; i >= 0 && (data & 0xff) > 0; i--) {
            int validXValue = (xValue + i) % (WIDTH);
            updateBit(validXValue, validYValue, data);
            data = (byte) ((data & 0xff) >>> 1);
        }

        repaintFlag = true;
    }

    public void clearDisplay() {
        for (byte[] b : pixels) {
            Arrays.fill(b, (byte) 0);
        }
    }

    public void clear() {
        clearDisplay();
        collision = false;
        repaintFlag = false;
    }

    public void backToEmulatorHome() {
        Container parentContainer = this.getParent();
        CardLayout cardLayout = (CardLayout) parentContainer.getLayout();
        cardLayout.show(parentContainer, Home.PANEL_NAME);
    }

    public byte[][] getPixels() {
        return pixels;
    }

    public void setPixels(byte[][] values) {
        for (int i = 0; i < values.length; i ++) {
            byte[] row = values[i];
            for (int j = 0; j < row.length; j++) {
                this.pixels[i][j] = row[j];
            }
        }
    }

    public boolean getCollision() {
        return collision;
    }

    public void setCollision(boolean collision) {
        this.collision = collision;
    }

    public void setRepaintFlag(boolean repaintFlag) {
        this.repaintFlag = repaintFlag;
    }

    public boolean shouldRepaint() {
        return repaintFlag;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH * scaleFactor, HEIGHT * scaleFactor);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, WIDTH * scaleFactor, HEIGHT * scaleFactor);

        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH / BYTE_SIZE; j++) {
                byte b = pixels[i][j];
                int bValue = b & 0xff;
                for (int k = 0; k < BYTE_SIZE; k++) {
                    if ((bValue >>> k) % 2 == 1) {
                        graphics.setColor(Color.GREEN);
                    } else {
                        graphics.setColor(Color.BLACK);
                    }
                    int x = (j * BYTE_SIZE) + (BYTE_SIZE - k - 1);
                    int y = i;
                    graphics.fillRect(x * scaleFactor, y * scaleFactor, scaleFactor, scaleFactor);
                }
            }
        }

        repaintFlag = false;
    }


    public boolean erasedPrevious() {
        return collision;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int value) {
        this.scaleFactor = value;
    }

    private void updateBit(int x, int y, byte b) {
        int xModulo = x % BYTE_SIZE;
        int xCoord = x / BYTE_SIZE;
        byte leastSignificantBit = (byte) ((b & 0xff) % 2);
        byte update = (byte) ((leastSignificantBit & 0xff) << (BYTE_SIZE - 1 - xModulo));
        byte updatedValue = (byte) ((pixels[y][xCoord] & 0xff) ^ (update & 0xff));
        if (!collision && ((pixels[y][xCoord] & 0xff) & (update & 0xff)) != 0) {
            collision = true;
        }
        pixels[y][xCoord] = updatedValue;
    }


}
