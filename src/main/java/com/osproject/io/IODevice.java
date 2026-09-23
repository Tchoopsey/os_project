package com.osproject.io;

import com.osproject.process.PCB;

public abstract class IODevice {
    protected String name;
    protected boolean busy;

    public IODevice (String name){
        this.name = name;
        this.busy = false;
    }
    public abstract void  startOperation(IOOperation op, PCB p);

    public boolean isBusy(){
        return busy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
