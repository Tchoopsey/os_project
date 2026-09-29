package com.osproject.io;

import com.osproject.filesystem.File;

public class DMAChannel {
    private String name;
    private boolean busy;
    private int transferredBytes;

    public DMAChannel(String name){
        this.name = name;
        this.busy = false;
        this.transferredBytes = 0;
    }

    public void transferToRAM(File file){
        if (file == null){
            throw new IllegalArgumentException("File is null");
        }
        busy = true;
        int size = file.getSize();
        transferredBytes += size;
        System.out.println("[DMA " + name + "] Disk ---> RAM (" + file.getName() + ", " + size + " bytes");
        busy = false;
    }

    public void transferToDisk(File file){
        if (file == null){
            throw new IllegalArgumentException("File is null");
        }
        busy = true;
        int size = file.getSize();
        transferredBytes += size;

        System.out.println("[DMA " + name + "] RAM ---> Disk transfer : " + file.getName() +"(" + size + " bytes)" );
        busy = false;
    }

    public boolean isBusy(){
        return busy;
    }

    public int getTransferredBytes(){
        return transferredBytes;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "DMAChannel{" +
                "name='" + name + '\'' +
                ", busy=" + busy +
                ", transferredBytes=" + transferredBytes +
                '}';
    }
}
