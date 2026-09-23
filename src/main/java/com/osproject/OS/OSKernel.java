package com.osproject.OS;

import com.osproject.filesystem.FileSystem;
import com.osproject.io.ConsoleDevice;
import com.osproject.io.DiskDevice;
import com.osproject.io.IODevice;
import com.osproject.io.IOManager;
import com.osproject.memory.MemoryManager;
import com.osproject.memory.RAM;
import com.osproject.process.*;
import com.osproject.syscall.Syscall;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class OSKernel {
    private List<PCB> processTable;
    private ReadyQueue readyQueue;
    private BlockedQueue blockedQueue;
    private CPU cpu;
    private Scheduler scheduler;
    private MemoryManager memoryManager;
    private FileSystem fileSystem;
    private IOManager ioManager;
    private int nextPid;
    private static final int DEFAULT_PROCESS_MEMORY = 64;

    public OSKernel (){
        this.processTable = new ArrayList<>();
        this.readyQueue = new ReadyQueue();
        this.blockedQueue = new BlockedQueue();
        this.cpu = new CPU();
        this.scheduler = new XScheduler(5);
        this.memoryManager = new MemoryManager(1024);
        this.ioManager = new IOManager(this);
        this.nextPid = 1;

        DiskDevice disk = new DiskDevice("DISK0", 512);
        this.fileSystem = new FileSystem(disk);
        this.ioManager.addDevice(new ConsoleDevice("CONSOLE0"));

    }

    public  void boot(){
        System.out.println("---------------------------------");
        System.out.println("-----OS Kernel is starting-------");
        System.out.println("---------------------------------");
        createProcess("init",10);

        System.out.println("[Kernel] Boot completed, nextPid = " + nextPid);
        System.out.println("[Kernel] Proccess in table " + processTable.size());
        System.out.println("-------------------------------------");
    }

    public  int createProcess(String programName, int priority){
        int pid = nextPid++;
        PCB pcb = new PCB(pid,programName,priority);
        try {
            memoryManager.allocate(pcb,DEFAULT_PROCESS_MEMORY);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return -1;
        }

        pcb.setState(ProcessState.READY);
        processTable.add(pcb);
        readyQueue.add(pcb);
        System.out.println("[Kernel] Created process PID = " + pid + " (" + programName + ")" + " , priority =" + priority);

        return pid;
    }

    public  void terminateProcess(int pid){
       PCB pcb = findProcessByPid(pid);
       if (pcb == null){
           throw new IllegalArgumentException("Processs PID=" +pid + " doesn't exist" );
       }

    }

    public static void timerTick(){

    }

    public static void handleIOCompletion (IODevice device){

    }

    public static void syscall (Syscall request){

    }


    public PCB findProcessByPid(int pid) {
        for (PCB pcb : processTable) {
            if (pcb.getPid() == pid) {
                return pcb;
            }
        }
        return null;
    }

    public List<PCB> getProcessTable() {
        return processTable;
    }

    public void setProcessTable(List<PCB> processTable) {
        this.processTable = processTable;
    }

    public ReadyQueue getReadyQueue() {
        return readyQueue;
    }

    public void setReadyQueue(ReadyQueue readyQueue) {
        this.readyQueue = readyQueue;
    }

    public BlockedQueue getBlockedQueue() {
        return blockedQueue;
    }

    public void setBlockedQueue(BlockedQueue blockedQueue) {
        this.blockedQueue = blockedQueue;
    }

    public CPU getCpu() {
        return cpu;
    }

    public void setCpu(CPU cpu) {
        this.cpu = cpu;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public void setMemoryManager(MemoryManager memoryManager) {
        this.memoryManager = memoryManager;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public IOManager getIoManager() {
        return ioManager;
    }

    public void setIoManager(IOManager ioManager) {
        this.ioManager = ioManager;
    }

    public int getNextPid() {
        return nextPid;
    }

    public void setNextPid(int nextPid) {
        this.nextPid = nextPid;
    }


}
