package com.osproject.OS;

import com.osproject.process.PCB;
import com.osproject.process.ProcessState;

public class CPU {
    private PCB current;
    private long cycleCount;

    public CPU (){
        this.current = null;
        this.cycleCount = 0;
    }
    public  void executeNextStep(){
        if (current == null) {
            return;
        }
        cycleCount++;

        current.setProgramCounter(current.getProgramCounter() + 1);
        current.decrementRemainingTime();
        System.out.println("[CPU] Tick #" + cycleCount + " - executing PID=" + current.getPid() + "(PC = " + current.getProgramCounter() + " , remaining = " + current.getRemainingTime() + ")" );

        if (current.isFinished()){
            current.setState(ProcessState.TERMINATED);
            System.out.println("[CPU] Process PID = " + current.getPid() + " finished with executing");
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
