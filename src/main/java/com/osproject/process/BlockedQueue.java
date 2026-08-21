package com.osproject.process;

import java.util.ArrayList;
import java.util.List;

public class BlockedQueue {
    private List<PCB> list;

    public BlockedQueue(){
        this.list = new ArrayList<>();
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

    /*
    public List<PCB> findByDevice (IODevice device){
    }
    implementirati metodu nakon kreiranja klase IODevice i svih njenih atributa i metoda
    */


}
