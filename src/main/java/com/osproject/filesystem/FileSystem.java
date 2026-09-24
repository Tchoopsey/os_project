package com.osproject.filesystem;

import com.osproject.io.DiskDevice;

public class FileSystem {
    private Directory root;
    private DiskDevice disk;

    public FileSystem(DiskDevice disk){
        this.disk = disk;
        this.root = new Directory("/", null);
    }

    public Directory getRoot(){
        return root;
    }

    public DiskDevice getDisk(){
        return disk;
    }

    public File createFile(String path){
        String parentPath = getParentPath(path);
        String directoryName = getFileName(path);

        Directory parent = parentDirectory(parentPath);
        if (parent == null){
            throw new IllegalArgumentException("Parent directory doesn't exist" + parentPath);

        }
        if (parent.getChild(directoryName) != null){
            throw new IllegalArgumentException("Node with that name already exists " + directoryName);
        }
        File newFile = new File(directoryName,parent);
        disk.allocateFile(newFile);
        parent.addChild(newFile);
        return newFile;
    }

    public Directory createDirectory(String path){
        String parentPath = getParentPath(path);
        String dirName = getFileName(path);

        Directory parent = parentDirectory(parentPath);
        if (parent == null){
            throw new IllegalArgumentException("Parent directory doesn't exist" + parentPath);
        }
        if (parent.getChild(dirName) != null){
            throw new IllegalArgumentException("Node already exist with that name " + dirName);
        }
        Directory newDir = new Directory(dirName,parent);
        parent.addChild(newDir);
        return newDir;
    }

    public OpenFileHandle openFile(String path){
        return null;
    }

    public FsNode resolve (String path){
        if (path == null || path.isEmpty()){
            return null;
        }
        if (path.equals("/")){
            return root;
        }

        String[] parts = path.split("/");
        FsNode currentNode = root;

        for (String part: parts){
            if (part.isEmpty()){
                continue;
            }
            if (!(currentNode instanceof Directory)){
                return null;
            }

            currentNode = ((Directory)currentNode).getChild(part);
            if (currentNode == null){
                return null;
            }
        }
        return currentNode;
    }
    private Directory parentDirectory(String parentPath) {
        FsNode node = resolve (parentPath);
        if (node instanceof Directory){
            return (Directory) node;
        }
        return null;
    }

    private String getFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }

    private String getParentPath(String fullPath){
        int lastSlash = fullPath.lastIndexOf('/');
        if (lastSlash <=0){
            return "/";
        }
        return fullPath.substring(0,lastSlash);
    }

}
