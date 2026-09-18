package com.osproject.filesystem;

public class OpenFileHandle {
    private File file;
    private int position;
    private FileMode mode;

    public OpenFileHandle(File file, FileMode mode){
        this.file = file;
        this.position = 0;
        this.mode = mode;
    }

    public FileMode getMode() {
        return mode;
    }

    public void setMode(FileMode mode) {
        this.mode = mode;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
