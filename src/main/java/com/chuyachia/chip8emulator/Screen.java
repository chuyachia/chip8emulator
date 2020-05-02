package com.chuyachia.chip8emulator;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Screen extends JPanel {

    public final static String PANEL_NAME = "Chip8Screen";
    public static final int SCALE_FACTOR = 10;
    public static final int WIDTH = 64;
    public static final int HEIGHT = 32;
    public static final int BYTE_SIZE = 8;
    private final byte[][] pixels;
    private final Memory memory;
    private boolean collision;
    private boolean repaintFlag;

    public Screen(Memory memory) {
        this.pixels = new byte[HEIGHT][WIDTH / BYTE_SIZE];
        this.memory = memory;
    }

    public void display(byte x, byte y, short n, short memoryStart) {
        int xValue = x & 0xff;
        int yValue = y & 0xff;
        int nValue = n & 0xffff;
        int memoryStartValue = memoryStart & 0xffff;
        int byteDislayed = 0;
        collision = false;

        while (byteDislayed < nValue) {
            byte currentByte = memory.getByte(memoryStartValue + byteDislayed);
            int validYValue = (yValue + byteDislayed) % (HEIGHT);
            for (int i = BYTE_SIZE - 1; i >= 0 && (currentByte & 0xff) > 0; i--) {
                int validXValue = (xValue + i) % (WIDTH);
                updateBit(validXValue, validYValue, currentByte);
                currentByte = (byte) ((currentByte & 0xff) >> 1);
            }

            byteDislayed++;
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

    public boolean shouldRepaint() {
        return repaintFlag;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH * SCALE_FACTOR, HEIGHT * SCALE_FACTOR);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, WIDTH * SCALE_FACTOR, HEIGHT * SCALE_FACTOR);

        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH / BYTE_SIZE; j++) {
                byte b = pixels[i][j];
                int bValue = b & 0xff;
                for (int k = 0; k < BYTE_SIZE; k++) {
                    if ((bValue >> k) % 2 == 1) {
                        graphics.setColor(Color.GREEN);
                        int x = (j * BYTE_SIZE) + (BYTE_SIZE - k - 1);
                        int y = i;
                        graphics.fillRect(x * SCALE_FACTOR, y * SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
                    }
                }
            }
        }

        repaintFlag = false;
    }


    public boolean erasedPrevious() {
        return collision;
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
