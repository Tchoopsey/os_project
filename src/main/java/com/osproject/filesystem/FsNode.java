package com.osproject.filesystem;

public abstract class FsNode {
    private String name;
    private Directory parent;

    public FsNode(String name, Directory parent){
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Directory getParent() {
        return parent;
    }

    public void setParent(Directory parent) {
        this.parent = parent;
    }

    @Override
    public java.lang.String toString() {
        return "FsNode{" +
                "name='" + name + '\'' +
                ", parent=" + parent +
                '}';
    }
}
