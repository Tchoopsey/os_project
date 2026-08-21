package com.osproject.process;

public interface Scheduler {
    PCB chooseNext(ReadyQueue readyQueue);
}
