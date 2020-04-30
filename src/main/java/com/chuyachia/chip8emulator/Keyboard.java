package com.chuyachia.chip8emulator;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

public class Keyboard {

    private static final Map<Integer, Short> KEYBOARD_MAP = new HashMap<>();

    {
        KEYBOARD_MAP.put(KeyEvent.VK_1, (short) (1 << 1));
        KEYBOARD_MAP.put(KeyEvent.VK_2, (short) (1 << 2));
        KEYBOARD_MAP.put(KeyEvent.VK_3, (short) (1 << 3));
        KEYBOARD_MAP.put(KeyEvent.VK_4, (short) (1 << 12));
        KEYBOARD_MAP.put(KeyEvent.VK_Q, (short) (1 << 4));
        KEYBOARD_MAP.put(KeyEvent.VK_W, (short) (1 << 5));
        KEYBOARD_MAP.put(KeyEvent.VK_E, (short) (1 << 6));
        KEYBOARD_MAP.put(KeyEvent.VK_R, (short) (1 << 13));
        KEYBOARD_MAP.put(KeyEvent.VK_A, (short) (1 << 7));
        KEYBOARD_MAP.put(KeyEvent.VK_S, (short) (1 << 8));
        KEYBOARD_MAP.put(KeyEvent.VK_D, (short) (1 << 9));
        KEYBOARD_MAP.put(KeyEvent.VK_F, (short) (1 << 14));
        KEYBOARD_MAP.put(KeyEvent.VK_Z, (short) (1 << 10));
        KEYBOARD_MAP.put(KeyEvent.VK_X, (short) (1 << 0));
        KEYBOARD_MAP.put(KeyEvent.VK_C, (short) (1 << 11));
        KEYBOARD_MAP.put(KeyEvent.VK_V, (short) (1 << 15));
    }

    private short pressed;
    private short lastPressed;

    public Keyboard() {
        KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        keyboardFocusManager.addKeyEventDispatcher((KeyEvent e) -> {
            synchronized (this) {
                Short key = KEYBOARD_MAP.get(e.getKeyCode());
                if (key != null) {
                    lastPressed = key;
                    switch (e.getID()) {
                        case KeyEvent.KEY_PRESSED:
                            pressed |= key;
                            notify();
                            return true;
                        case KeyEvent.KEY_RELEASED:
                            pressed ^= key;
                            return true;
                        default:
                            return false;
                    }
                }

                return false;
            }
        });
    }

    public byte waitForInput() throws InterruptedException {
        synchronized (this) {
            wait();
            return (byte) ((lastPressed & 0xffff) / Math.log(2));
        }
    }

    public boolean isPressed(byte key) {
        int keyValue = key & 0xff;
        short keyValueMask = (short) (1 << keyValue);
        return (pressed & keyValueMask) == keyValueMask;
    }
}
