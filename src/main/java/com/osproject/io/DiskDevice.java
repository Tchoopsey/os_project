package com.osproject.io;

import com.osproject.filesystem.File;

/**
 * DiskDevice
 */
public class DiskDevice extends IODevice {
    public void allocateFile(File file){
        System.out.println("Allocated file " + file.getName());
    }

    public void deallocateFile(File file){
        System.out.println("Deallocated file " + file.getName());
    }
}
