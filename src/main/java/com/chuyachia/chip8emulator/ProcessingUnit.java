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
                jumpToNnnn(arguments);
                return;
            case CALL_ADDR:
                goToNnnn(arguments);
                return;

            case SE_VX:
                skipIfVxKKEqual(arguments);
                return;

            case SNE_VX:
                skipIfVxKKNotEqual(arguments);
                return;

            case SE_VX_VY:
                skipIfVxVyEqual(arguments);
                return;

            case LD_VX:
                setVxToKk(arguments);
                return;

            case ADD_VX:
                addKkToVx(arguments);
                return;

            case LD_VX_VY:
                storeVyValueInVx(arguments);
                return;

            case OR_VX_VY:
                orVxVy(arguments);
                return;

            case AND_VX_VY:
                andVxVy(arguments);
                return;

            case XOR_VX_VY:
                xorVxVy(arguments);
                return;

            case ADD_VX_VY:
                addVxVy(arguments);
                return;

            case SUB_VX_VY:
                subVxVy(arguments);
                return;

            case SHR_VX_VY:
                rightShiftVx(arguments);
                return;

            case SUBN_VX_VY:
                subVyVx(arguments);
                return;

            case SHL_VX_VY:
                leftShiftVx(arguments);
                return;

            case SNE_VX_VY:
                skipIfVxVyNotEqual(arguments);
                return;

            case LD_I_ADDR:
                setIToNnnn(arguments);
                return;

            case JP_V0_ADDR:
                jumpToNnnnPlusV0(arguments);
                return;

            case RND_VX:
                randomByteAndKk(arguments);
                return;

            case DRW_VX_VY:
                displayOnScreen(arguments);
                return;

            case SKP_VX:
                skipIfKeyVxPressed(arguments);
                return;

            case SKNP_VX:
                skipIfKeyVxNotPressed(arguments);
                return;

            case LD_VX_DT:
                setVxToDT(arguments);
                return;

            case LD_VX_K:
                setVxToKeyInput(arguments);
                return;

            case LD_DT_VX:
                setDTtoVx(arguments);
                return;

            case LD_ST_VX:
                setSTtoVx(arguments);
                return;

            case ADD_I_VX:
                addIVx(arguments);
                return;

            case LD_F_VX:
                setISpriteVx(arguments);
                return;

            case LD_B_VX:
                storeVxBCDRepresentation(arguments);
                return;

            case LD_I_VX:
                storeRegisterV0ToVx(arguments);
                return;

            case LD_VX_I:
                readToRegisterV0ToVx(arguments);
                return;

            default:
                return;

        }
    }

    private void jumpToNnnn(short[] arguments) {
        short nnnn = Instruction.getNnnn(arguments);

        memory.setPC(nnnn);
    }

    private void goToNnnn(short[] arguments) throws Exception {
        short nnnn = Instruction.getNnnn(arguments);

        stack.push(memory.getPC());
        memory.setPC(nnnn);
    }

    private void skipIfVxKKEqual(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte kk = Instruction.getKk(arguments);
        byte vx = V[x];

        if (vx == kk) {
            memory.increasePC(2);
        }
    }

    private void skipIfVxKKNotEqual(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte kk = Instruction.getKk(arguments);
        byte vx = V[x];

        if (vx != kk) {
            memory.increasePC(2);
        }
    }

    private void skipIfVxVyEqual(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);
        byte vx = V[x];
        byte vy = V[y];

        if (vx == vy) {
            memory.increasePC(2);
        }
    }

    private void setVxToKk(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte kk = Instruction.getKk(arguments);
        V[x] = kk;
    }

    private void addKkToVx(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte kk = Instruction.getKk(arguments);

        V[x] = (byte) (V[x] + kk);
    }

    private void storeVyValueInVx(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);

        V[x] = V[y];
    }

    private void orVxVy(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);

        V[x] = (byte) (V[x] | V[y]);
    }

    private void andVxVy(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);

        V[x] = (byte) (V[x] & V[y]);
    }

    private void xorVxVy(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);

        V[x] = (byte) (V[x] ^ V[y]);
    }


    private void addVxVy(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);

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

    private void subVxVy(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);

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

    private void rightShiftVx(short[] arguments) {
        byte x = Instruction.getX(arguments);

        V[0xf] = (byte) (V[x]& 0x01);
        V[x] = (byte) (byteToIntValue(V[x]) >>> 1);
    }

    private void subVyVx(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);

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

    private void leftShiftVx(short[] arguments) {
        byte x = Instruction.getX(arguments);

        byte mostSignificant = (byte) (V[x] & 0x80);

        if (mostSignificant != 0) {
            V[0xf] = (byte) 1;
        } else {
            V[0xf] = (byte) 0;
        }

        V[x] = (byte) (byteToIntValue(V[x]) << 1);
    }

    private void skipIfVxVyNotEqual(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);
        byte vx = V[x];
        byte vy = V[y];

        if (vx != vy) {
            memory.increasePC(2);
        }
    }

    private void setIToNnnn(short[] arguments) {
        short nnnn = Instruction.getNnnn(arguments);
        I = nnnn;
    }

    private void jumpToNnnnPlusV0(short[] arguments) {
        short nnnn = Instruction.getNnnn(arguments);

        memory.setPC((short) (shortToIntValue(nnnn) + byteToIntValue(V[0])));
    }

    private void randomByteAndKk(short[] arguments) {
        byte x = Instruction.getKk(arguments);
        byte kk = Instruction.getKk(arguments);

        byte rand = (byte) random.nextInt(256);
        V[arguments[0]] = (byte) (rand & kk);
    }

    private void displayOnScreen(short[] arguments) {
        byte x = Instruction.getX(arguments);
        byte y = Instruction.getY(arguments);
        byte n = Instruction.getN(arguments);

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

    private void skipIfKeyVxPressed(short[] arguments) {
        byte x = Instruction.getX(arguments);

        if (keyboard.isPressed(V[x])) {
            memory.increasePC(2);
        }
    }

    private void skipIfKeyVxNotPressed(short[] arguments) {
        byte x = Instruction.getX(arguments);

        if (!keyboard.isPressed(V[x])) {
            memory.increasePC(2);
        }
    }

    private void setVxToDT(short[] arguments) {
        byte x = Instruction.getX(arguments);

        V[x] = DT;
    }

    private void setVxToKeyInput(short[] arguments) throws InterruptedException {
        byte x = Instruction.getX(arguments);

        V[x] = keyboard.waitForInput();
    }

    private void setDTtoVx(short[] arguments) {
        byte x = Instruction.getX(arguments);

        DT = V[x];
    }

    private void setSTtoVx(short[] arguments) {
        byte x = Instruction.getX(arguments);

        ST = V[x];
    }

    private void addIVx(short[] arguments) {
        byte x = Instruction.getX(arguments);

        I = (short) (shortToIntValue(I) + byteToIntValue(V[x]));
    }

    private void setISpriteVx(short[] arguments) {
        byte x = Instruction.getX(arguments);

        I = (short) (byteToIntValue(V[x]) * Memory.SPRITE_SIZE);
    }

    private void storeVxBCDRepresentation(short[] arguments) {
        // Takes the decimal value of Vx, and
        // places the hundreds digit in memory at location in I,
        // the tens digit at location I+1,
        // and the ones digit at location I+2
        byte x = Instruction.getX(arguments);

        int vXValue = byteToIntValue(V[x]);
        int power = 100;

        for (int i = 2; i >= 0 & vXValue > 0; i--) {
            int digit = vXValue / power;
            memory.setByte(shortToIntValue(I) + (2 - i), (byte) digit);
            vXValue -= digit * power;
            power /= 10;
        }
    }

    private void storeRegisterV0ToVx(short[] arguments) {
        byte x = Instruction.getX(arguments);
        int to = byteToIntValue(x);

        int memoryStart = shortToIntValue(I);
        for (int i = 0; i <= to; i++) {
            byte b = V[i];
            memory.setByte(memoryStart, b);
            memoryStart++;
        }
    }

    private void readToRegisterV0ToVx(short[] arguments) {
        byte x = Instruction.getX(arguments);
        int to = byteToIntValue(x);

        int register = 0;
        int memoryStart = shortToIntValue(I);
        while (register <= to) {
            V[register] = memory.getByte(memoryStart + register);
            register++;
        }
    }

    private int byteToIntValue(byte b) {
        return b & 0xff;
    }

    private int shortToIntValue(short s) {
        return s & 0xffff;
    }
}
