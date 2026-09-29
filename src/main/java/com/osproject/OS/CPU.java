package com.osproject.OS;

import com.osproject.assembler.Instruction;
import com.osproject.memory.MemoryManager;
import com.osproject.memory.MemorySegment;
import com.osproject.process.PCB;
import com.osproject.process.ProcessState;

public class CPU {
    private PCB current;
    private long cycleCount;
    private MemoryManager memoryManager;

    public CPU (MemoryManager memoryManager){
        this.current = null;
        this.cycleCount = 0;
        this.memoryManager = memoryManager;
    }
    public  void executeNextStep(){
        if (current == null) {
            return;
        }
        int pc = current.getProgramCounter();
        if (current.getProgram() == null || current.getProgram().isEmpty()){
            cycleCount++;
            current.setProgramCounter(pc+1);
            current.decrementRemainingTime();
            if (current.isFinished()){
                current.setState(ProcessState.TERMINATED);
            }
            return;
        }
        if (pc < 0 ||pc >= current.getProgram().size()){
            current.setProgramCounter(-1);
            current.setState(ProcessState.TERMINATED);
            return;
        }

        Instruction instruction = current.getProgram().get(pc);
        int operand = instruction.getOperand();
        int acc = current.getACC();

        cycleCount++;
        cycleCount++;
        System.out.println("[CPU] Tick #" + cycleCount +
                " PID=" + current.getPid() +
                " PC=" + pc + " " + instruction +
                " ACC=" + acc);
        try {
            switch (instruction.getOpcode()) {

                case "LOAD":
                    current.setACC(memoryManager.read(current, operand));
                    current.setProgramCounter(pc + 1);
                    break;

                case "STORE":
                    memoryManager.write(current, operand, acc);
                    current.setProgramCounter(pc + 1);
                    break;

                case "ADD":
                    current.setACC(acc + memoryManager.read(current, operand));
                    current.setProgramCounter(pc + 1);
                    break;

                case "SUB":
                    current.setACC(acc - memoryManager.read(current, operand));
                    current.setProgramCounter(pc + 1);
                    break;

                case "MUL":
                    current.setACC(acc * memoryManager.read(current, operand));
                    current.setProgramCounter(pc + 1);
                    break;

                case "DIV":
                    int divisor = memoryManager.read(current, operand);
                    if (divisor == 0) {
                        System.err.println("[CPU] Division by zero! PID=" + current.getPid());
                        current.setProgramCounter(-1);
                        current.setState(ProcessState.TERMINATED);
                        return;
                    }
                    current.setACC(acc / divisor);
                    current.setProgramCounter(pc + 1);
                    break;

                case "JMP":
                    current.setProgramCounter(operand);
                    break;

                case "JZ":
                    current.setProgramCounter(acc == 0 ? operand : pc + 1);
                    break;

                case "HALT":
                    current.setProgramCounter(-1);
                    current.setState(ProcessState.TERMINATED);
                    System.out.println("[CPU] HALT - PID=" + current.getPid());
                    break;

                default:
                    System.err.println("[CPU] Unknown opcode: " + instruction.getOpcode());
                    current.setProgramCounter(pc + 1);
            }
        }catch (SecurityException e){
            System.err.println("[CPU]" + e.getMessage());
            current.setProgramCounter(-1);
            current.setState(ProcessState.TERMINATED);
            return;
        }
        current.decrementRemainingTime();
        current.incrementInstructions();
        if (current.isFinished()){
            current.setState(ProcessState.TERMINATED);
        }
    }

    public  void contextSwitch(PCB next){
        if (current != null){
            System.out.println("[CPU] Context switch: saving PID = " + current.getPid());
        }
        this.current = next;
        if (next != null){
            next.setState(ProcessState.RUNNING);
            System.out.println("[CPU] Context switch: loading PID=" + next.getPid());
        }else {
            System.out.println("[CPU] Context switch: CPU is now idle");
        }
    }

    private int checkAddress(PCB pcb, int address){
        if (address < 0 || address >= pcb.getLimit() -pcb.getBaseAddress() + 1){
            throw new SecurityException("Address space violation! PID = " + pcb.getPid() + " tried accessing address  " + address + " (process size= "+ (pcb.getLimit() - pcb.getBaseAddress() + 1) + ")");
        }
        return address;
    }

    public  PCB getCurrent(){
        return current;
    }

    public long getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(long cycleCount) {
        this.cycleCount = cycleCount;
    }

    public void setCurrent(PCB current) {
        this.current = current;
    }

    public boolean isIdle(){
        return current == null;
    }

    @Override
    public String toString() {
        return "CPU{" +
                "current=" + current +
                ", cycleCount=" + cycleCount +
                '}';
    }
}
