package com.osproject.process;

import com.osproject.io.IODevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockedQueue {
    private List<PCB> list;
    private Map<PCB,IODevice> waitingOn;

    public BlockedQueue(){
        this.list = new ArrayList<>();
        this.waitingOn = new HashMap<>();
    }

    public void block(PCB process){
       if (process != null){
           list.add(process);
           process.setState(ProcessState.WAITING);
       }
    }

    public void unblock(PCB process){
        if (process != null && list.remove(process)){
            process.setState(ProcessState.READY);
        }
    }

    public boolean isEmpty(){
        return list.isEmpty();
    }

    public int size(){
        return list.size();
    }

    public List<PCB> getCopyOfAList(){
        return new ArrayList<>(list);
    }


    public List<PCB> findByDevice (IODevice device){
        List<PCB> result = new ArrayList<>();
        if (device == null){
            return result;
        }
        for (PCB p : list){
            IODevice waiting = waitingOn.get(p);
            if (waiting != null && waiting.equals(device)){
                result.add(p);
            }
        }
        return result;
    }

    public void removePCB (PCB process){
        if (process == null){
            return;
        }
        list.remove(process);
        waitingOn.remove(process);
    }

    public void setWaitingDevice(PCB process, IODevice device) {
        if (process == null || device == null) return;
        waitingOn.put(process, device);
    }

}
