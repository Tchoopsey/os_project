package com.osproject.io;

import com.osproject.OS.OSKernel;
import com.osproject.process.PCB;

import java.util.ArrayList;
import java.util.List;

/**
 * IOManager
 */
public class IOManager {
    private List<IODevice> devices;
    private OSKernel kernel;

    public IOManager(List<IODevice> devices) {
        this.devices = new ArrayList<>();
        this.kernel = null;
    }

    public IOManager (OSKernel kernel){
        this.kernel = kernel;
        this.devices = new ArrayList<>();
    }
    
    // TODO: requestIO & completeIO funkcije

    public  void requestIO(PCB p, String deviceName, IOOperation op){
        if (p == null){
            throw new IllegalArgumentException("PCB cannot be null");
        }
        if (op == null){
            throw new IllegalArgumentException("IOOperation cannot be null");
        }

        IODevice device = getDeviceByName(deviceName);
        if (device == null){
            throw new IllegalArgumentException("Device " + deviceName + " doesn't exist");
        }
        if (device.isBusy()){
            System.out.println("[IOManager] Device " + deviceName + " is currently busy. Process PID = " + p.getPid() + " is passed to blockedQueue");
            if (kernel != null){
                kernel.getBlockedQueue().block(p);
            }
            return;
        }
        device.startOperation(op,p);

        if (kernel != null){
            kernel.getBlockedQueue().block(p);
        }

    }

    public  void completeIO(IODevice device){
        if (device == null){
            throw new IllegalArgumentException("Device cannot be null");
        }
        System.out.println("[IOManager] Completed I/O on a device " + device.getName());
        if (device instanceof DiskDevice){
            ((DiskDevice) device).completeOperation();
        } else if (device instanceof ConsoleDevice) {
            ((ConsoleDevice)device).completeOperation();
        }

        if (kernel != null){
            kernel.handleIOCompletion(device);

        }
    }

    public void tick(){
        List<IODevice> busyDevices = new ArrayList<>();
        for (IODevice d: devices){
            if (d.isBusy()){
                busyDevices.add(d);
            }
        }
        for (IODevice d: devices){
            completeIO(d);
        }
    }

    public void addDevice(IODevice device){
        if (device == null){
            throw new IllegalArgumentException("Device cannot be null");
        }
        for (IODevice d : devices){
            if (d.getName().equals(device.getName())){
                throw new IllegalArgumentException("Device with name " + device.getName() + " already registered");
            }
        }
        devices.add(device);
    }

    public List<IODevice> getDevices(){
        return devices;
    }

    public IODevice getDeviceByName(String name){
        for (IODevice d : devices){
            if (d.getName().equals(name)){
                return d;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "IOManager{" +
                "devices=" + devices +
                ", kernel=" + kernel +
                '}';
    }
}
