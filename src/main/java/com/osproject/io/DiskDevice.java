package com.osproject.io;

import com.osproject.filesystem.File;
import com.osproject.process.PCB;

import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;

/**
 * DiskDevice
 */
public class DiskDevice extends IODevice {
    private Map<File,Integer> allocatedFiles;
    private int totalBlocks;
    private int usedBlocks;
    public static final int BLOCK_SIZE = 64;

    public DiskDevice(String name, int totalBlocks){
        super(name);
        this.totalBlocks = totalBlocks;
        this.usedBlocks = 0;
        this.allocatedFiles = new HashMap<>();
    }

    public void allocateFile(File file){
        if (allocatedFiles.containsKey(file)){
            throw new IllegalArgumentException("File " + file.getName()+ "is already allocated");
        }
        int blocksNeeded = calculateBlocksNeeded(file);
        if ((usedBlocks + blocksNeeded) > totalBlocks){
            throw new IllegalArgumentException("There's not enough space on disk. Space needed " + blocksNeeded + ", free space " + (totalBlocks - usedBlocks));
        }

        allocatedFiles.put(file, blocksNeeded);
        usedBlocks += blocksNeeded;
    }

    private int calculateBlocksNeeded(File file) {
        int size = file.getSize();
        return Math.max(1,(int) Math.ceil((double) size / BLOCK_SIZE));
    }

    public void deallocateFile(File file){
        Integer blocks = allocatedFiles.remove(file);
        if (blocks != null){
            usedBlocks -= blocks;
        }
    }

    public boolean isAllocated (File file){
        return allocatedFiles.containsKey(file);
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
        System.out.println("Files on a disk: " );
        allocatedFiles.forEach(((file, blocks) -> System.out.println(" - " + file.getName() + " (" + blocks + " blocks")));
        System.out.println("-----------------------------");
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
                "allocatedFiles=" + allocatedFiles +
                ", totalBlocks=" + totalBlocks +
                ", usedBlocks=" + usedBlocks +
                '}';
    }
}
