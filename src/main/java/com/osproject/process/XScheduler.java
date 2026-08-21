package com.osproject.process;

public class XScheduler implements Scheduler{
    private int timeQuantum;

    public XScheduler (int timeQuantum){
        this.timeQuantum = timeQuantum;
    }

    public int getTimeQuantum(){
        return timeQuantum;
    }

    @Override
    public PCB chooseNext (ReadyQueue readyQueue){
        if (readyQueue.isEmpty()){
            return null;
        }
        return readyQueue.removeNext();
    }
}
