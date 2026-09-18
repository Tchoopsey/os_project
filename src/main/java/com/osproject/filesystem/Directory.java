package com.osproject.filesystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Directory extends FsNode {
    private Map<String, FsNode> children;

    public Directory(String name, Directory parent){
        super(name,parent);
        this.children = new HashMap<>();
    }

    public void addChild(FsNode node){
        if (node == null){
            throw new IllegalArgumentException("Node cannot be null! ");
        }
        if (children.containsKey(node.getName())){
            throw new IllegalArgumentException("Node " + node.getName() + " already exists in this directory " + this.getName());
        }

        node.setParent(this);
        children.put(node.getName(), node);
    }

    public FsNode getChild(String name){
        return children.get(name);
    }

    public List<FsNode> list (){
        return new ArrayList<>(children.values());
    }


    public void removeChild(String name){
        FsNode removed = children.remove(name);
        if (removed != null){
            removed.setParent(null);
        }
    }


    public boolean isEmpty(){
        return children.isEmpty();
    }

    @Override
    public String toString() {
        return "Directory{" +
                "children=" + children +
                '}';
    }
}
