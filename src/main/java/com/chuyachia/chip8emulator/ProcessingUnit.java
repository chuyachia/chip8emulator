package com.chuyachia.chip8emulator;

public class ProcessingUnit {
    // Register
    private byte[] V;
    // Memory register
    private short I;
    // Delay timer register
    private byte DT;
    // Sound timer register
    private byte ST;
    private Memory memory;
    private Stack stack;

    public ProcessingUnit(Memory memory) {
        this.memory = memory;
        V = new byte[16];
        stack = new Stack();
    }

    public void instructionCycle() {
        short instruction = fetch();
        try {
            Instruction instructionPattern = decode(instruction);
//            execute(instruction, instructionPattern);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            throw new Exception(String.format("Unknown instruction encountered %s", Integer.toHexString(instruction)));
        }

        return instructionPattern;
    }

    private void execute(short instruction, Instruction instructionPattern) {
        short[] arguments = instructionPattern.getArguments(instruction);

        switch (instructionPattern) {
            case CLS:
                // TODO
                return;
            case RET:
                try {
                    int addr = stack.pop();
                    memory.setPC(addr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            case JP_ADDR:
                memory.setPC(arguments[0]);
                System.out.println(memory.getPC());
                return;
            case CALL_ADDR:
                try {
                    stack.push(memory.getPC());
                    memory.setPC(arguments[0]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            case SE_VX:
                if (equalVxkk(arguments[0], arguments[1])) {
                    memory.increasePC(2);
                }
                return;
            case SNE_VX:
                if (!equalVxkk(arguments[0], arguments[1])) {
                    memory.increasePC(2);
                }
                return;
            case SE_VX_VY:
                if (equalVxVy(arguments[0], arguments[1])) {
                    memory.increasePC(2);
                }
                return;
            case LD_VX:
            case LD_VX_VY:
                V[arguments[0]] = (byte) arguments[1];
                return;
            case ADD_VX:
                V[arguments[0]] += arguments[1];
                return;
            case OR_VX_VY:
                V[arguments[0]] = (byte) (arguments[0] | arguments[1]);
                return;
            case AND_VX_VY:
                V[arguments[0]] = (byte) (arguments[0] & arguments[1]);
                return;
            case XOR_VX_VY:
                V[arguments[0]] = (byte) (arguments[0] ^ arguments[1]);
                return;
            case ADD_VX_VY:
                int sum = (V[arguments[0]] & 0xff) + (V[arguments[1]] & 0xff);
                V[0xf] = sum > 255 ? (byte) 1 : (byte) 0;
                V[arguments[0]] = (byte) (sum & 0xff);
                return;
            case SUB_VX_VY:
                V[0xf] = arguments[0] > arguments[1] ? (byte) 1 : (byte) 0;
                V[arguments[0]] = (byte) (V[arguments[0]] - V[arguments[1]]);
                return;

        }
    }

    private boolean equalVxkk(short x, short kk) {
        byte vx = V[x];

        return (vx & 0xff) == kk;
    }

    private boolean equalVxVy(short x, short y) {
        return V[x] == V[y];
    }
}
