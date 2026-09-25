package com.osproject.io;

import com.osproject.filesystem.File;
import com.osproject.process.PCB;

import java.util.*;

/**
 * DiskDevice
 */
public class DiskDevice extends IODevice {
    private Queue<File> fileQueue;
    private int totalBlocks;
    private int usedBlocks;
    public static final int BLOCK_SIZE = 64;

    public DiskDevice(String name, int totalBlocks){
        super(name);
        this.totalBlocks = totalBlocks;
        this.usedBlocks = 0;
        this.fileQueue = new LinkedList<>();
    }

    public void allocateFile(File file){
        if (fileQueue.contains(file)){
            throw new IllegalArgumentException("File " + file.getName()+ "is already allocated");
        }
        int blocksNeeded = calculateBlocksNeeded(file);
        if ((usedBlocks + blocksNeeded) > totalBlocks){
            throw new IllegalArgumentException("There's not enough space on disk. Space needed " + blocksNeeded + ", free space " + (totalBlocks - usedBlocks));
        }

        fileQueue.add(file);
        usedBlocks += blocksNeeded;
    }

    private int calculateBlocksNeeded(File file) {
        int size = file.getSize();
        return Math.max(1,(int) Math.ceil((double) size / BLOCK_SIZE));
    }

    public void deallocateFile(File file){
        if (fileQueue.remove(file)){
            int blocks = calculateBlocksNeeded(file);
            usedBlocks -= blocks;

            System.out.println("[DISK " + name + "] FIFO dequeue " + file.getName());
        }
    }

    public File peekOldestFile(){
        return fileQueue.peek();
    }

    public boolean isAllocated (File file){
        return fileQueue.contains(file);
    }

    public int getFreeBlocks(){
        return totalBlocks - usedBlocks;
    }

    public int getUsedBlocks(){
        return usedBlocks;
    }

    public int getTotalBlocks(){
        return totalBlocks;
    }

    public void printDiskStatus(){
        System.out.println("-----DISK-----" + this.getName() + "-----");
        System.out.println("Capacity: " + totalBlocks +" blocks" );
        System.out.println("Used up: " + usedBlocks + " blocks");
        System.out.println("Free: " + getFreeBlocks() + " blocks");
        System.out.println("File queue FIFO order: " );
        if (fileQueue.isEmpty()){
            System.out.println("(EMPTY)");
        }else {
            int i = 1;
            for (File f : fileQueue){
                System.out.println(" " + i++ + f.getName() + " (" + calculateBlocksNeeded(f) + "blocks)");
            }
        }
        System.out.println("-------------------------------------");

    }


    public void startOperation(IOOperation operation, PCB pcb){
        if (busy){
            throw new IllegalArgumentException("DISK " + this.getName() + " is busy");
        }

        this.busy = true;
        System.out.println("[DiskDevice " + name + "] Started operation " + operation.getType() + " for process PID = "+ pcb.getPid() + " (duration " + operation.getDuration() + " )");
    }

    public void completeOperation(){
        this.busy = false;
        System.out.println("[DiskDevice " + name + "] Operation completed!"  );
    }

    @Override
    public String toString() {
        return "DiskDevice{" +
                "files =" + fileQueue +
                ", totalBlocks=" + totalBlocks +
                ", usedBlocks=" + usedBlocks +
                '}';
    }
}
