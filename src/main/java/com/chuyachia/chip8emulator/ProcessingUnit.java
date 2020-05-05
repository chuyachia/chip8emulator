package com.chuyachia.chip8emulator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;

public class ProcessingUnit  {
    private final static int REFRESH_RATE = 60;
    private final static int DEFAULT_CLOCK_RATE = 500;

    private final Memory memory;
    private final Random random;
    private final Screen screen;
    private final Keyboard keyboard;
    private final byte[] V;
    private final JFileChooser fileChooser = new JFileChooser();

    private int clockRate;
    private int refreshCycle;
    private long cpuWaitTime;

    private short I;
    private byte DT;
    private byte ST;
    private Stack stack;

    public ProcessingUnit(Memory memory, Screen screen, Keyboard keyboard) {
        this.clockRate = DEFAULT_CLOCK_RATE;
        this.refreshCycle = clockRate / REFRESH_RATE;
        this.cpuWaitTime = (1 * 1000 / clockRate);
        this.memory = memory;
        this.screen = screen;
        this.keyboard = keyboard;
        V = new byte[16];
        stack = new Stack();
        random = new Random();
        fileChooser.setDialogTitle("Save");
        fileChooser.setApproveButtonText("Save");
    }

    public void start() {
        int refresh = 0;

        while (!keyboard.escapePressed.get()) {
            if (keyboard.savePressed.get()) {
                handleSaveState();
                keyboard.savePressed.set(false);
            }

            long start = System.currentTimeMillis();
            try {
                instructionCycle();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                break;
            }

            if (refresh == refreshCycle) {
                if (screen.shouldRepaint()) {
                    screen.repaint();
                }
                if (DT > 0) {
                    DT--;
                }

                if (ST > 0) {
                    Toolkit.getDefaultToolkit().beep();
                    ST--;
                }

                refresh = 0;
            }

            try {
                Thread.sleep(Math.max(0, cpuWaitTime - (System.currentTimeMillis() - start)));
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                end();
                break;
            }

            refresh++;
        }

        end();
    }

    public void setV(byte[] value) {
        for (int i = 0; i < value.length;i ++) {
            V[i] = value[i];
        }
    }

    public void setI(short value) {
        I = value;
    }

    public void setDT(byte value) {
        DT = value;
    }

    public void setST(byte value) {
        ST = value;
    }

    public void setStack(Stack value) {
        stack = value;
    }

    public void setClockRate(int rate) {
        clockRate = rate;
        refreshCycle = clockRate / REFRESH_RATE;
        cpuWaitTime = (1 * 1000 / clockRate);
    }

    private void handleSaveState() {
        File defaultSavedFile = new File(String.format("./saveFile/chip8_%d.ser", System.currentTimeMillis()));
        fileChooser.setSelectedFile(defaultSavedFile);
        int userInteraction = fileChooser.showOpenDialog(screen);
        if (userInteraction == JFileChooser.APPROVE_OPTION) {
            File savedFile = fileChooser.getSelectedFile();
            try {
                saveState(savedFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(screen, "Something went wrong when saving current emulation state");
            }
        }
    }

    private String saveState(File savedFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(savedFile);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(V);
            objectOutputStream.writeObject(I);
            objectOutputStream.writeObject(DT);
            objectOutputStream.writeObject(ST);
            objectOutputStream.writeObject(stack);
            objectOutputStream.writeObject(memory.getPC());
            objectOutputStream.writeObject(memory.getMemory());
            objectOutputStream.writeObject(screen.getPixels());
            objectOutputStream.writeObject(screen.getCollision());
        }

        return savedFile.getName();
    }

    private void end() {
        screen.clear();
        memory.clear();
        keyboard.clear();
        screen.backToEmulatorHome();
    }

    private void instructionCycle() throws Exception {
        short instruction = fetch();
        Instruction instructionPattern = decode(instruction);
        execute(instruction, instructionPattern);

    }

    private short fetch() {
        return memory.nextInstruction();
    }

    private Instruction decode(short instruction) throws Exception {
        Instruction instructionPattern = null;
        for (Instruction existingInstruction : Instruction.values()) {
            if (existingInstruction.match(instruction)) {
                instructionPattern = existingInstruction;
                break;
            }
        }

        if (instructionPattern == null) {
            throw new Exception(String.format("Unknown instruction encountered %s", Integer.toHexString(instruction & 0xffff)));
        }

        return instructionPattern;
    }

    private void execute(short instruction, Instruction instructionPattern) throws Exception {
        short[] arguments = instructionPattern.getArguments(instruction);

        switch (instructionPattern) {
            case CLS:
                screen.clearDisplay();
                return;
            case RET:
                short addr = stack.pop();
                memory.setPC(addr);
                return;
            case JP_ADDR:
                memory.setPC(arguments[0]);
                return;
            case CALL_ADDR:
                stack.push(memory.getPC());
                memory.setPC(arguments[0]);
                return;

            case SE_VX:
                if (compareVxkk(arguments[0], arguments[1]) == 0) {
                    memory.increasePC(2);
                }
                return;

            case SNE_VX:
                if (compareVxkk(arguments[0], arguments[1]) != 0) {
                    memory.increasePC(2);
                }
                return;

            case SE_VX_VY:
                if (compareVxVy(arguments[0], arguments[1]) == 0) {
                    memory.increasePC(2);
                }
                return;

            case LD_VX:
                V[arguments[0]] = (byte) (shortToIntValue(arguments[1]));
                return;

            case ADD_VX:
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[0]]) + shortToIntValue(arguments[1]));
                return;

            case LD_VX_VY:
                V[arguments[0]] = V[arguments[1]];
                return;

            case OR_VX_VY:
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[0]]) | byteToIntValue(V[arguments[1]]));
                return;

            case AND_VX_VY:
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[0]]) & byteToIntValue(V[arguments[1]]));
                return;

            case XOR_VX_VY:
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[0]]) ^ byteToIntValue(V[arguments[1]]));
                return;

            case ADD_VX_VY:
                int sum = byteToIntValue(V[arguments[0]]) + byteToIntValue(V[arguments[1]]);
                V[0xf] = sum > 255 ? (byte) 1 : (byte) 0;
                V[arguments[0]] = (byte) (sum & 0xff);
                return;

            case SUB_VX_VY:
                V[0xf] = compareVxVy(arguments[0], arguments[1]) > 0 ? (byte) 1 : (byte) 0;
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[0]]) - byteToIntValue(V[arguments[1]]));
                return;

            case SHR_VX_VY:
                V[0xf] = leastSignificatBit(V[arguments[0]]);
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[0]]) >>> 1);
                return;

            case SUBN_VX_VY:
                V[0xf] = compareVxVy(arguments[0], arguments[1]) < 0 ? (byte) 1 : (byte) 0;
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[1]]) - byteToIntValue(V[arguments[0]]));
                return;

            case SHL_VX_VY:
                V[0xf] = mostSignificatBit(V[arguments[0]]) != 0 ? (byte) 1 : (byte) 0;
                V[arguments[0]] = (byte) (byteToIntValue(V[arguments[0]]) << 1);
                return;

            case SNE_VX_VY:
                if (compareVxVy(arguments[0], arguments[1]) != 0) {
                    memory.increasePC(2);
                }
                return;

            case LD_I_ADDR:
                I = arguments[0];
                return;

            case JP_V0_ADDR:
                memory.setPC((short) (shortToIntValue(arguments[0]) + byteToIntValue(V[0])));
                return;

            case RND_VX:
                int rand = random.nextInt(256);
                V[arguments[0]] = (byte) (rand & shortToIntValue(arguments[1]));
                return;

            case DRW_VX_VY:
                screen.display(V[arguments[0]], V[arguments[1]], arguments[2], I);
                V[0xf] = screen.erasedPrevious() ? (byte) 1 : (byte) 0;
                return;

            case SKP_VX:
                if (keyboard.isPressed(V[arguments[0]])) {
                    memory.increasePC(2);
                }
                return;

            case SKNP_VX:
                if (!keyboard.isPressed(V[arguments[0]])) {
                    memory.increasePC(2);
                }
                return;

            case LD_VX_DT:
                V[arguments[0]] = DT;
                return;

            case LD_VX_K:
                V[arguments[0]] = keyboard.waitForInput();
                return;

            case LD_DT_VX:
                DT = V[arguments[0]];
                return;

            case LD_ST_VX:
                ST = V[arguments[0]];
                return;

            case ADD_I_VX:
                I = (short) (shortToIntValue(I) + byteToIntValue(V[arguments[0]]));
                return;

            case LD_F_VX:
                I = (short) (byteToIntValue(V[arguments[0]]) * Memory.SPRITE_SIZE);
                return;

            case LD_B_VX:
                storeBCDRepresentation(V[arguments[0]]);
                return;

            case LD_I_VX:
                storeRegisterIntoMemory(shortToIntValue(arguments[0]));
                return;

            case LD_VX_I:
                readMemoryIntoRegister(shortToIntValue(arguments[0]));
                return;

            default:
                return;

        }
    }

    private int compareVxkk(short x, short kk) {
        byte vx = V[x];

        return Byte.compare(vx, (byte) kk);
    }

    private int compareVxVy(short x, short y) {
        byte vx = V[x];
        byte vy = V[y];
        return Byte.compare(vx, vy);
    }

    private int byteToIntValue(byte b) {
        return b & 0xff;
    }

    private int shortToIntValue(short s) {
        return s & 0xffff;
    }

    private byte leastSignificatBit(byte b) {
        return (byte) (b & 0x01);
    }

    private byte mostSignificatBit(byte b) {
        return (byte) (b & 0x80);
    }

    private void storeBCDRepresentation(byte b) {
        int intValue = byteToIntValue(b);
        for (int i = 2; i >= 0 & intValue > 0; i--) {
            byte digit = (byte) Math.floor(intValue / Math.pow(10, i));
            memory.setByte(shortToIntValue(I) + (2 - i), digit);
            intValue = (int) (intValue % (Math.pow(10, i)));
        }
    }

    private void storeRegisterIntoMemory(int to) {
        int memoryStart = shortToIntValue(I);
        for (int i = 0; i <= to; i++) {
            byte b = V[i];
            memory.setByte(memoryStart, b);
            memoryStart++;
        }
    }

    private void readMemoryIntoRegister(int to) {
        int register = 0;
        int memoryStart = shortToIntValue(I);
        while (register <= to) {
            V[register] = memory.getByte(memoryStart + register);
            register++;
        }
    }

}
