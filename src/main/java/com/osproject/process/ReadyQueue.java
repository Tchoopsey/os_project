package com.osproject.process;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ReadyQueue {
    private Queue<PCB> queue;

    public ReadyQueue(){
        this.queue = new LinkedList<>();
    }

    public void add(PCB process){
        if (process != null){
            queue.add(process);
            process.setState(ProcessState.READY);
        }
    }

    public boolean removePCB (PCB process){
        return queue.remove(process);
    }

    public PCB removeNext(){
        return queue.poll();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }

    public int queueSize(){
        return queue.size();
    }

    public List<PCB> getCopyOfAQueue(){
        return new ArrayList<>(queue);
    }
}
