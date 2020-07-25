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
    private final static int GENERAL_REGISTERS_NUMBER = 16;

    private final Memory memory;
    private final Random random;
    private final Screen screen;
    private final Keyboard keyboard;
    // 16 8 bits general purpose registers
    private final byte[] V;
    // 16 bits register I
    private short I;
    // Delay timer register
    private byte DT;
    // Sound timer register
    private byte ST;
    private Stack stack;

    private final JFileChooser fileChooser = new JFileChooser();

    private int clockRate;
    private int refreshCycle;
    private long cpuWaitTime;


    public ProcessingUnit(Memory memory, Screen screen, Keyboard keyboard) {
        this.clockRate = DEFAULT_CLOCK_RATE;
        this.refreshCycle = clockRate / REFRESH_RATE;
        this.cpuWaitTime = (1 * 1000 / clockRate);
        this.memory = memory;
        this.screen = screen;
        this.keyboard = keyboard;
        V = new byte[GENERAL_REGISTERS_NUMBER];
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
                runInstructionCycle();
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

    void setV(byte[] value) {
        for (int i = 0; i < GENERAL_REGISTERS_NUMBER;i ++) {
            V[i] = value[i];
        }
    }

    void setI(short value) {
        I = value;
    }

    void setDT(byte value) {
        DT = value;
    }

    void setST(byte value) {
        ST = value;
    }

    void setStack(Stack value) {
        stack = value;
    }

    void setClockRate(int rate) {
        clockRate = rate;
        refreshCycle = clockRate / REFRESH_RATE;
        cpuWaitTime = (1 * 1000) / clockRate;
    }

    private void handleSaveState() {
        File defaultSavedFile = new File(String.format("chip8_%d.ser", System.currentTimeMillis()));
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

    private void runInstructionCycle() throws Exception {
        short instruction = fetch();
        Instruction instructionPattern = decode(instruction);
        execute(instruction, instructionPattern);

    }

    private short fetch() {
        return memory.nextInstruction();
    }

    private Instruction decode(short instruction) throws Exception {
        Instruction instructionPattern = Instruction.getMatched(instruction);

        if (instructionPattern == null) {
            throw new Exception(String.format("Unknown instruction encountered %s", Integer.toHexString(instruction & 0xffff)));
        }

        return instructionPattern;
    }

    private void execute(short instruction, Instruction instructionPattern) throws Exception {
        short[] arguments = instructionPattern.getArguments(instruction);
        byte x, y, n , kk;
        short nnnn;

        switch (instructionPattern) {
            case CLS:
                screen.clearDisplay();
                return;
            case RET:
                short addr = stack.pop();
                memory.setPC(addr);
                return;
            case JP_ADDR:
                nnnn = Instruction.getNnnn(arguments);
                jumpToNnnn(nnnn);
                return;
            case CALL_ADDR:
                nnnn = Instruction.getNnnn(arguments);
                goToNnnn(nnnn);
                return;

            case SE_VX:
                x = Instruction.getX(arguments);
                kk = Instruction.getKk(arguments);
                skipIfVxKKEqual(x, kk);
                return;

            case SNE_VX:
                x = Instruction.getX(arguments);
                kk = Instruction.getKk(arguments);
                skipIfVxKKNotEqual(x, kk);
                return;

            case SE_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                skipIfVxVyEqual(x, y);
                return;

            case LD_VX:
                x = Instruction.getX(arguments);
                kk = Instruction.getKk(arguments);
                setVxToKk(x, kk);
                return;

            case ADD_VX:
                x = Instruction.getX(arguments);
                kk = Instruction.getKk(arguments);
                addKkToVx(x, kk);
                return;

            case LD_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                storeVyValueInVx(x, y);
                return;

            case OR_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                orVxVy(x, y);
                return;

            case AND_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                andVxVy(x, y);
                return;

            case XOR_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                xorVxVy(x, y);
                return;

            case ADD_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                addVxVy(x, y);
                return;

            case SUB_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                subVxVy(x, y);
                return;

            case SHR_VX_VY:
                x = Instruction.getX(arguments);
                rightShiftVx(x);
                return;

            case SUBN_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                subVyVx(x, y);
                return;

            case SHL_VX_VY:
                x = Instruction.getX(arguments);
                leftShiftVx(x);
                return;

            case SNE_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                skipIfVxVyNotEqual(x, y);
                return;

            case LD_I_ADDR:
                nnnn = Instruction.getNnnn(arguments);
                setIToNnnn(nnnn);
                return;

            case JP_V0_ADDR:
                nnnn = Instruction.getNnnn(arguments);
                jumpToNnnnPlusV0(nnnn);
                return;

            case RND_VX:
                x = Instruction.getX(arguments);
                kk = Instruction.getKk(arguments);
                randomByteAndKk(x, kk);
                return;

            case DRW_VX_VY:
                x = Instruction.getX(arguments);
                y = Instruction.getY(arguments);
                n = Instruction.getN(arguments);
                displayOnScreen(x, y , n);
                return;

            case SKP_VX:
                x = Instruction.getX(arguments);
                skipIfKeyVxPressed(x);
                return;

            case SKNP_VX:
                x = Instruction.getX(arguments);
                skipIfKeyVxNotPressed(x);
                return;

            case LD_VX_DT:
                x = Instruction.getX(arguments);
                setVxToDT(x);
                return;

            case LD_VX_K:
                x = Instruction.getX(arguments);
                setVxToKeyInput(x);
                return;

            case LD_DT_VX:
                x = Instruction.getX(arguments);
                setDTtoVx(x);
                return;

            case LD_ST_VX:
                x = Instruction.getX(arguments);
                setSTtoVx(x);
                return;

            case ADD_I_VX:
                x = Instruction.getX(arguments);
                addIVx(x);
                return;

            case LD_F_VX:
                x = Instruction.getX(arguments);
                setISpriteVx(x);
                return;

            case LD_B_VX:
                x = Instruction.getX(arguments);
                storeVxBCDRepresentation(x);
                return;

            case LD_I_VX:
                x = Instruction.getX(arguments);
                storeRegisterV0ToVx(x);
                return;

            case LD_VX_I:
                x = Instruction.getX(arguments);
                readToRegisterV0ToVx(x);
                return;

            default:
                return;

        }
    }

    void jumpToNnnn(short nnnn) {
        memory.setPC(nnnn);
    }

    void goToNnnn(short nnnn) throws Exception {
        stack.push(memory.getPC());
        memory.setPC(nnnn);
    }

    void skipIfVxKKEqual(byte x, byte kk) {
        byte vx = V[x];

        if (vx == kk) {
            memory.increasePC(2);
        }
    }

    void skipIfVxKKNotEqual(byte x, byte kk) {
        byte vx = V[x];

        if (vx != kk) {
            memory.increasePC(2);
        }
    }

    void skipIfVxVyEqual(byte x, byte y) {
        byte vx = V[x];
        byte vy = V[y];

        if (vx == vy) {
            memory.increasePC(2);
        }
    }

    void setVxToKk(byte x, byte kk) {
        V[x] = kk;
    }

    void addKkToVx(byte x, byte kk) {
        V[x] = (byte) (V[x] + kk);
    }

    void storeVyValueInVx(byte x, byte y) {
        V[x] = V[y];
    }

    void orVxVy(byte x, byte y) {
        V[x] = (byte) (V[x] | V[y]);
    }

    void andVxVy(byte x, byte y) {
        V[x] = (byte) (V[x] & V[y]);
    }

    void xorVxVy(byte x, byte y) {
        V[x] = (byte) (V[x] ^ V[y]);
    }


    void addVxVy(byte x, byte y) {
        byte sum = (byte) (V[x] + V[y]);
        int sumValue = byteToIntValue(sum);
        int vxValue = byteToIntValue(V[x]);
        int vyValue = byteToIntValue(V[y]);

        if (sumValue < vxValue || sumValue < vyValue) {
            // If sum is smaller than vx or vy value, there's overflow
            V[0xf] = (byte) 1;
        } else {
            V[0xf] = (byte) 0;
        }

        V[x] = sum;
    }

    void subVxVy(byte x, byte y) {
        byte diff = (byte) (V[x] - V[y]);

        int vxValue = byteToIntValue(V[x]);
        int vyValue = byteToIntValue(V[y]);

        if (vxValue > vyValue) {
            V[0xf] = (byte) 1;
        } else {
            V[0xf] = (byte) 0;
        }

        V[x] = diff;
    }

    void rightShiftVx(byte x) {
        V[0xf] = (byte) (V[x]& 0x01);
        V[x] = (byte) (byteToIntValue(V[x]) >>> 1);
    }

    void subVyVx(byte x, byte y) {
        byte diff = (byte) (V[y] - V[x]);

        int vxValue = byteToIntValue(V[x]);
        int vyValue = byteToIntValue(V[y]);

        if (vyValue > vxValue) {
            V[0xf] = (byte) 1;
        } else {
            V[0xf] = (byte) 0;
        }

        V[x] = diff;
    }

    void leftShiftVx(byte x) {
        byte mostSignificant = (byte) (V[x] & 0x80);

        if (mostSignificant != 0) {
            V[0xf] = (byte) 1;
        } else {
            V[0xf] = (byte) 0;
        }

        V[x] = (byte) (byteToIntValue(V[x]) << 1);
    }

    void skipIfVxVyNotEqual(byte x, byte y) {
        byte vx = V[x];
        byte vy = V[y];

        if (vx != vy) {
            memory.increasePC(2);
        }
    }

    void setIToNnnn(short nnnn) {
        I = nnnn;
    }

    void jumpToNnnnPlusV0(short nnnn) {
        memory.setPC((short) (shortToIntValue(nnnn) + byteToIntValue(V[0])));
    }

    void randomByteAndKk(byte x, byte kk) {
        byte rand = (byte) random.nextInt(256);
        V[x] = (byte) (rand & kk);
    }

    void displayOnScreen(byte x, byte y, byte n) {
        int byteDislayed = 0;
        int nValue = shortToIntValue(n);
        int memoryStartValue = shortToIntValue(I);

        while (byteDislayed < nValue) {
            byte currentByte = memory.getByte(memoryStartValue + byteDislayed);
            screen.display(V[x], V[y], currentByte, byteDislayed);
            byteDislayed++;
        }

        V[0xf] = screen.erasedPrevious() ? (byte) 1 : (byte) 0;
    }

    void skipIfKeyVxPressed(byte x) {
        if (keyboard.isPressed(V[x])) {
            memory.increasePC(2);
        }
    }

    void skipIfKeyVxNotPressed(byte x) {
        if (!keyboard.isPressed(V[x])) {
            memory.increasePC(2);
        }
    }

    void setVxToDT(byte x) {
        V[x] = DT;
    }

    void setVxToKeyInput(byte x) throws InterruptedException {
        V[x] = keyboard.waitForInput();
    }

    void setDTtoVx(byte x) {
        DT = V[x];
    }

    void setSTtoVx(byte x) {
        ST = V[x];
    }

    void addIVx(byte x) {
        I = (short) (shortToIntValue(I) + byteToIntValue(V[x]));
    }

    void setISpriteVx(byte x) {
        I = (short) (byteToIntValue(V[x]) * Memory.SPRITE_SIZE);
    }

    void storeVxBCDRepresentation(byte x) {
        // Takes the decimal value of Vx, and
        // places the hundreds digit in memory at location in I,
        // the tens digit at location I+1,
        // and the ones digit at location I+2
        int vXValue = byteToIntValue(V[x]);
        int power = 100;

        for (int i = 2; i >= 0 & vXValue > 0; i--) {
            int digit = vXValue / power;
            memory.setByte(shortToIntValue(I) + (2 - i), (byte) digit);
            vXValue -= digit * power;
            power /= 10;
        }
    }

    void storeRegisterV0ToVx(byte x) {
        int to = byteToIntValue(x);

        int memoryStart = shortToIntValue(I);
        for (int i = 0; i <= to; i++) {
            byte b = V[i];
            memory.setByte(memoryStart, b);
            memoryStart++;
        }
    }

    void readToRegisterV0ToVx(byte x) {
        int to = byteToIntValue(x);

        int register = 0;
        int memoryStart = shortToIntValue(I);
        while (register <= to) {
            V[register] = memory.getByte(memoryStart + register);
            register++;
        }
    }

    int byteToIntValue(byte b) {
        return b & 0xff;
    }

    int shortToIntValue(short s) {
        return s & 0xffff;
    }
}
