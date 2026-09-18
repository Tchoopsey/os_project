package com.osproject.filesystem;

public class File extends FsNode{
    private StringBuilder content;

    public File(String name, Directory parent){
        super(name,parent);
        this.content = new StringBuilder();
    }

    public String read(){
        return content.toString();
    }

    public void write(String data){
        content.setLength(0);
        content.append(data);
    }

    public void append(String data){
        content.append(data);
    }

    public int getSize(){
        return content.length();
    }


    @Override
    public String toString() {
        return "File{" +
                "content=" + content +
                '}';
    }
}
