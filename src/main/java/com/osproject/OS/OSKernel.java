package com.osproject.OS;

import com.osproject.filesystem.File;
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
    private int timeQuantum;
    private static final int DEFAULT_TIME_QUANTUM = 5;

    public OSKernel (){
        this.processTable = new ArrayList<>();
        this.readyQueue = new ReadyQueue();
        this.blockedQueue = new BlockedQueue();
        this.cpu = new CPU();
        this.timeQuantum = DEFAULT_TIME_QUANTUM;
        this.scheduler = new XScheduler(DEFAULT_TIME_QUANTUM);
        this.memoryManager = new MemoryManager(1024);
        this.ioManager = new IOManager(this);
        this.nextPid = 1;

        DiskDevice disk = new DiskDevice("DISK0", 512);
        this.ioManager.addDevice(disk);
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
        System.out.println("[Kernel] Terminating PID=" + pid + " (" + pcb.getProgramName() + ")");

       if (cpu.getCurrent() != null && cpu.getCurrent().getPid() == pid){
           cpu.contextSwitch(null);
       }

       readyQueue.removePCB(pcb);
       blockedQueue.removePCB(pcb);

       try {
           memoryManager.free(pcb);
       }catch (Exception e){
           System.err.println("[Kernel] Error freeing memory for PID = " + pid + " : " + e.getMessage());
       }

       if (pcb.getOpenFiles() != null){
           pcb.closeAllFiles();
       }

       pcb.setState(ProcessState.TERMINATED);
       processTable.remove(pcb);
        System.out.println("[Kernel] Process PID =" + pid + " terminated");

    }

    public  void timerTick(){
        ioManager.tick();
        if (cpu.getCurrent() == null){
            PCB next = scheduler.chooseNext(readyQueue);
            if (next == null){
                return;
            }
            cpu.contextSwitch(next);
            next.setState(ProcessState.RUNNING);
        }
        cpu.executeNextStep();

        PCB current = cpu.getCurrent();
        if (current == null){
            return;
        }

        if (current.isFinished() || current.getState() == ProcessState.TERMINATED){
            System.out.println("[Kernel] Process PID = " + current.getPid() + " finished");
            int pid = current.getPid();
            cpu.contextSwitch(null);
            terminateProcess(pid);
            return;
        }

        if (cpu.getCycleCount() % timeQuantum == 0){
            System.out.println("[Kernel] PID = " + current.getPid() + " - quantum expired, context switch");
            current.setState(ProcessState.READY);
            readyQueue.add(current);
            cpu.contextSwitch(null);
        }
    }

    public  void handleIOCompletion (IODevice device){
        System.out.println("[Kernel] I/O completed on device " + device.getName() + " . Unblocking processes...");
        List<PCB> unblocked = blockedQueue.findByDevice(device);
        for (PCB pcb : unblocked){
            blockedQueue.unblock(pcb);
            readyQueue.add(pcb);
            System.out.println("[Kernel] Process PID = " + pcb.getPid() + " returned to readyQueue");
        }
    }

    public  void syscall (Syscall request){
        if (request == null || request.getType() == null){
            throw new IllegalArgumentException("Invalid syscall");
        }

        PCB current = cpu.getCurrent();
        System.out.println("[Syscall] " + request.getType() + " (process PID = " +  (current != null ? current.getPid() : "?") + ")");

        switch (request.getType()){
            case CREATE_PROCESS :
                handleCreateProcessSyscall(request);
                break;
            case EXIT:
                handleExitSyscall();
                break;
            case OPEN:
                handleOpenSyscall(request);
                break;
            case READ:
                handleReadSyscall(request);
                break;
            case WRITE:
                handleWriteSyscall(request);
                break;
            case SLEEP:
                handleSleepSyscall();
                break;
            case YIELD:
                handleYieldSyscall();
                break;
            default:
                System.err.println("[Syscall] Unknown type: " + request.getType());
        }
    }

    private void handleExitSyscall() {
        PCB current = cpu.getCurrent();
        if (current == null) {
            System.err.println("[Syscall] EXIT: no running process!");
            return;
        }
        terminateProcess(current.getPid());
    }

    private void handleYieldSyscall() {
        PCB current = cpu.getCurrent();
        if (current == null) return;

        current.setState(ProcessState.READY);
        readyQueue.add(current);
        cpu.contextSwitch(null);
        System.out.println("[Syscall] PID=" + current.getPid() + " yielded CPU.");
    }

    private void handleSleepSyscall() {
        PCB current = cpu.getCurrent();
        if (current == null) return;

        current.setState(ProcessState.WAITING);
        blockedQueue.block(current);
        cpu.contextSwitch(null);
        System.out.println("[Syscall] PID=" + current.getPid() + " is sleeping.");
    }

    private void handleCreateProcessSyscall(Syscall request) {
        List<String> args = request.getArgs();
        if (args == null || args.size() < 2) {
            System.err.println("[Syscall] CREATE_PROCESS requires 2 args: [name, priority]");
            return;
        }

        String name = args.get(0);
        int priority;
        try {
            priority = Integer.parseInt(args.get(1));
        } catch (NumberFormatException e) {
            System.err.println("[Syscall] Priority must be a number!");
            return;
        }

        createProcess(name, priority);
    }

    private void handleOpenSyscall(Syscall request) {
        PCB current = cpu.getCurrent();
        if (current == null) {
            System.err.println("[Syscall] OPEN: no running process!");
            return;
        }

        List<String> args = request.getArgs();
        if (args == null || args.isEmpty()) {
            System.err.println("[Syscall] OPEN requires 1 arg: [path]");
            return;
        }

        String path = args.get(0);
        try {
            current.addOpenFileHandle(fileSystem.openFile(path));
            System.out.println("[Syscall] Opened: " + path);
        } catch (IllegalArgumentException e) {
            System.err.println("[Syscall] Cannot open '" + path + "': " + e.getMessage());
        }
    }

    private void handleReadSyscall(Syscall request) {
        List<String> args = request.getArgs();
        if (args == null || args.isEmpty()) {
            System.err.println("[Syscall] READ requires 1 arg: [path]");
            return;
        }

        String path = args.get(0);
        Object node = fileSystem.resolve(path);
        if (node instanceof File) {
            String content = ((File) node).read();
            System.out.println("[Syscall] Content of '" + path + "': " + content);
        } else {
            System.err.println("[Syscall] File not found: " + path);
        }
    }

    private void handleWriteSyscall(Syscall request) {
        List<String> args = request.getArgs();
        if (args == null || args.size() < 2) {
            System.err.println("[Syscall] WRITE requires 2 args: [path, data]");
            return;
        }

        String path = args.get(0);
        String data = args.get(1);

        Object node = fileSystem.resolve(path);
        if (node instanceof File) {
            ((File) node).write(data);
            System.out.println("[Syscall] Written to '" + path + "': " + data);
        } else {
            System.err.println("[Syscall] File not found: " + path);
        }
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