package com.osproject.io;

import com.osproject.process.PCB;

/**
 * ConsoleDevice
 */
public class ConsoleDevice extends IODevice {
    private String lastOutput;

    public ConsoleDevice(String name) {
        super(name);
    }

    public void completeOperation() {
        this.busy = false;
        System.out.println("[ConsoleDevice " + name + "] Operation completed");
    }

    @Override
    public void startOperation(IOOperation op, PCB p) {
        if (busy) {
            throw new IllegalStateException("Console '" + name + "' is busy!");
        }

        this.busy = true;

        if (op.getType() == IOType.WRITE) {
            System.out.println("[CONSOLE] " + op.getData());
            this.lastOutput = op.getData();
        } else if (op.getType() == IOType.READ) {
            this.lastOutput = "Simulated keyboard input";
            System.out.println("[CONSOLE] Read: " + lastOutput);
        }
    }
    public String getLastOutput(){
        return lastOutput;
    }

    @Override
    public String toString() {
        return "ConsoleDevice{" +
                "busy=" + busy +
                ", name='" + name + '\'' +
                '}';
    }
}
