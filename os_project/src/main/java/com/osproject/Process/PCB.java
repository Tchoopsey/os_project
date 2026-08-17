package com.osproject.Process;

import com.osproject.FileSystem.OpenFileHandle;

import java.awt.desktop.OpenFilesHandler;
import java.awt.font.ImageGraphicAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PCB {
    private int pid;
    private ProcessState state;
    private int priority;
    private int programCounter;
    private Map<String,Integer> registers;
    private int baseAddress;
    private int limit;
    private List<OpenFileHandle> openFiles;
    private int arrivalTime;
    private int burstTime;
    private int remainingTime;
    private int completionTime;
    private String programName;
    private boolean isSystemProcess;

    public PCB(int pid, String programName, int priority, int arrivalTime, int burstTime,boolean isSystemProcess){
        this.pid = pid;
        this.state = ProcessState.NEW;
        this.priority = priority;
        this.registers = new HashMap<>();
        this.baseAddress = 0;
        this.limit = 0;
        this.openFiles = new ArrayList<>();
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.completionTime = 0;
        this.programName = programName;
        this.isSystemProcess = isSystemProcess;

    }

    public int getPid() {
        return pid;
    }

    public ProcessState getState() {
        return state;
    }

    public int getPriority() {
        return priority;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public Map<String, Integer> getRegisters() {
        return registers;
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public int getLimit() {
        return limit;
    }

    public List<OpenFileHandle> getOpenFiles() {
        return openFiles;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public String getProgramName() {
        return programName;
    }

    public boolean isSystemProcess() {
        return isSystemProcess;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    //da li je proces zavrsen ili prekinut naglo :')
    public boolean isFinished(){
        if (remainingTime <= 0 || programCounter == -1){ //kad CPU izvrsi HLT instrukciju, treba PC da se postavi na -1
            return true;                                    // ako je instrukcija == HLT --> nesto.setProgramCounter(-1); u CPU dijelu
        }
        return false;
    }

    public void decrementRemainingTime(){
        if (remainingTime > 0){
            remainingTime--;
        }
    }


  public int getRegister(String name){
        for (Map.Entry<String, Integer> entry : registers.entrySet()){
            String key = entry.getKey();
            int value = entry.getValue();

            if (name.equals(key)){
                return value;
            }

        }
        return 0;
  }

  public void setRegister(String name, int value){
        if (registers.containsKey(name)){
            registers.put(name,value);//update-uje vrijednost postojeceg registra
        }
  }

    @Override
    public String toString() {
        return "PCB{" +
                "pid=" + pid +
                ", state=" + state +
                ", priority=" + priority +
                ", programCounter=" + programCounter +
                ", registers=" + registers +
                ", baseAddress=" + baseAddress +
                ", limit=" + limit +
                ", openFiles=" + openFiles +
                ", arrivalTime=" + arrivalTime +
                ", burstTime=" + burstTime +
                ", remainingTime=" + remainingTime +
                ", completionTime=" + completionTime +
                ", programName='" + programName + '\'' +
                ", isSystemProcess=" + isSystemProcess +
                '}';
    }
}
