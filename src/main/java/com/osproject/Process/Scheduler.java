package com.osproject.Process;

public interface Scheduler {
    PCB chooseNext(ReadyQueue readyQueue);
}
