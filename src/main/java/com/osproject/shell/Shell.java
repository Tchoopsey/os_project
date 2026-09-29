package com.osproject.shell;

import com.osproject.OS.OSKernel;
import com.osproject.filesystem.Directory;
import com.osproject.filesystem.File;
import com.osproject.filesystem.FsNode;
import com.osproject.memory.MemorySegment;
import com.osproject.process.PCB;
import com.osproject.process.Scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Shell {

    private OSKernel kernel;
    private Directory currentDirectory;
    private boolean running;
    private Scanner scanner;

    public Shell(OSKernel kernel) {
        this.kernel = kernel;
        this.currentDirectory = kernel.getFileSystem().getRoot();
        this.running = true;
    }

    public void start() {
        this.scanner = new Scanner(System.in);
        System.out.println();
        System.out.println("=====================================");
        System.out.println("   OS Shell - Minimal Command Language");
        System.out.println("   Type 'help' for a list of commands");
        System.out.println("=====================================");

        while (running) {
            System.out.print(getPrompt() + "> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            try {
                executeCommand(command, args);
            } catch (Exception e) {
                System.err.println("[Shell] Error: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private String getPrompt() {
        String name = currentDirectory.getName();
        if (name.equals("/")) return "/";
        return name;
    }
    private void executeCommand(String command, String[] args) {
        switch (command) {
            case "cd":     cmdCd(args); break;
            case "dir":
            case "ls":     cmdDir(); break;
            case "mkdir":  cmdMkdir(args); break;
            case "ps":     cmdPs(); break;
            case "run":    cmdRun(args); break;
            case "mem":    cmdMem(); break;
            case "rm":     cmdRm(args); break;
            case "kill":   cmdKill(args); break;
            case "cat":    cmdCat(args); break;
            case "touch":  cmdTouch(args); break;
            case "asm":    cmdAsm(args); break;
            case "exec":   cmdExec(args); break;
            case "tick":   cmdTick(args); break;
            case "help":   cmdHelp(); break;
            case "exit":   cmdExit(); break;
            default:
                System.err.println("[Shell] Unknown command: " + command);
                System.err.println("        Type 'help' for a list of commands.");
        }
    }



    private void cmdCd(String[] args) {
        if (args.length == 0) {
            currentDirectory = kernel.getFileSystem().getRoot();
            return;
        }

        String path = args[0];

        if (path.equals("..")) {
            Directory parent = currentDirectory.getParent();
            if (parent != null) {
                currentDirectory = parent;
            }
            return;
        }
        if (path.equals(".")) return;
        if (path.equals("/")) {
            currentDirectory = kernel.getFileSystem().getRoot();
            return;
        }

        FsNode target;
        if (path.startsWith("/")) {
            target = kernel.getFileSystem().resolve(path);
        } else {
            target = currentDirectory.getChild(path);
        }

        if (target == null) {
            System.err.println("[Shell] Directory does not exist: " + path);
            return;
        }
        if (!(target instanceof Directory)) {
            System.err.println("[Shell] Not a directory: " + path);
            return;
        }

        currentDirectory = (Directory) target;
    }


    private void cmdDir() {
        System.out.println("Contents of directory: " + getFullPath(currentDirectory));
        System.out.printf("%-15s %-25s%n", "TYPE", "NAME");
        System.out.println("---------------------------------------------");

        List<FsNode> children = currentDirectory.list();

        if (children.isEmpty()) {
            System.out.println("(empty directory)");
            return;
        }

        for (FsNode child : children) {
            String type = (child instanceof Directory) ? "DIR" : "FILE";
            System.out.printf("%-15s %-25s%n", type, child.getName());
        }
    }


    private void cmdMkdir(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: mkdir <name>");
            return;
        }

        String name = args[0];

        try {
            Directory newDir = new Directory(name, currentDirectory);
            currentDirectory.addChild(newDir);
            System.out.println("[Shell] Directory created: " + name);
        } catch (IllegalArgumentException e) {
            System.err.println("[Shell] Error: " + e.getMessage());
        }
    }


    private void cmdPs() {
        List<PCB> processes = kernel.getProcessTable();

        if (processes.isEmpty()) {
            System.out.println("No active processes.");
            return;
        }

        System.out.printf("%-5s %-15s %-10s %-8s %-8s %-8s%n",
                "PID", "NAME", "STATE", "PC", "INSTR", "RAM");
        System.out.println("----------------------------------------------------------");

        for (PCB p : processes) {
            int ramSize = p.getLimit() - p.getBaseAddress() + 1;
            if (ramSize < 0) {
                ramSize = 0;
            }

            System.out.printf("%-5d %-12s %-10s %-10d %-8d %-8d%n",
                    p.getPid(),
                    p.getProgramName(),
                    p.getState(),
                    p.getProgramCounter(),
                    p.getInstructionsExecuted(),
                    ramSize

            );
        }
    }


    private void cmdRun(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: run <name> [priority]");
            return;
        }

        String name = args[0];
        int priority = 5;

        if (args.length >= 2) {
            try {
                priority = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("[Shell] Priority must be a number.");
                return;
            }
        }

        int pid = kernel.createProcess(name, priority);

        if (pid > 0) {
            System.out.println("[Shell] Process started: PID=" + pid +
                    " (" + name + "), priority=" + priority);
        } else {
            System.err.println("[Shell] Failed to start process.");
        }
    }


    private void cmdMem() {
        int total = kernel.getMemoryManager().getRam().getSize();
        int used = 0;

        List<MemorySegment> segments = kernel.getMemoryManager().getSegments();
        for (MemorySegment s : segments) {
            used += (s.getLimit() - s.getBase() + 1);
        }

        int free = total - used;
        double percent = (total > 0) ? (100.0 * used / total) : 0;

        System.out.println("===== RAM MEMORY =====");
        System.out.println("Total:     " + total + " cells");
        System.out.println("Used:      " + used + " cells");
        System.out.println("Free:      " + free + " cells");
        System.out.printf("Usage:     %.2f%%%n", percent);
        System.out.println();

        System.out.println("Allocated segments:");
        if (segments.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (MemorySegment s : segments) {
                System.out.println("  " + s);
            }
        }

        System.out.println("======================");
    }

    private void cmdRm(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: rm <name>");
            return;
        }

        String name = args[0];
        FsNode target = currentDirectory.getChild(name);

        if (target == null) {
            System.err.println("[Shell] Does not exist: " + name);
            return;
        }

        try {
            removeNode(target);

            currentDirectory.removeChild(name);

            System.out.println("[Shell] Removed: " + name);
        } catch (Exception e) {
            System.err.println("[Shell] Error: " + e.getMessage());
        }
    }


    private void removeNode(FsNode node) {
        if (node instanceof Directory) {
            Directory dir = (Directory) node;

            for (FsNode child : dir.list()) {
                removeNode(child);
            }
        } else if (node instanceof com.osproject.filesystem.File) {
            com.osproject.filesystem.File file =
                    (com.osproject.filesystem.File) node;

            try {
                kernel.getFileSystem().getDisk().deallocateFile(file);
            } catch (Exception e) {
            }
        }
    }


    private void cmdKill(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: kill <pid>");
            return;
        }

        int pid;
        try {
            pid = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("[Shell] PID must be a number.");
            return;
        }

        try {
            kernel.terminateProcess(pid);
            System.out.println("[Shell] Process PID=" + pid + " terminated.");
        } catch (IllegalArgumentException e) {
            System.err.println("[Shell] Error: " + e.getMessage());
        }
    }


    private void cmdHelp() {
        System.out.println("===== AVAILABLE COMMANDS =====");
        System.out.println("  cd <path>          - change directory");
        System.out.println("  dir | ls           - list current directory");
        System.out.println("  mkdir <name>       - create directory");
        System.out.println("  ps                 - list processes");
        System.out.println("  run <name> [prio]  - start a process");
        System.out.println("  mem                - show RAM usage");
        System.out.println("  rm <name>          - remove file/directory");
        System.out.println("  kill <pid>         - terminate process");
        System.out.println("  touch <file>       - create empty file");
        System.out.println("  cat <file>         - display file contents");
        System.out.println("  asm <file>         - enter assembler code");
        System.out.println("  exec <file> [prio] - run process from binary");
        System.out.println("  tick [n]           - advance simulation by n ticks (default 1)");
        System.out.println("  exit               - shut down OS");
        System.out.println("  help               - show this list");
        System.out.println("==============================");
    }


    private void cmdExit() {
        System.out.println("[Shell] Shutting down OS...");
        running = false;
    }

    private void cmdTick(String[] args) {
        int n = 1;
        if (args.length >= 1) {
            try {
                n = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[Shell] Tick count must be a number");
                return;
            }
        }

        for (int i = 0; i < n; i++) {
            kernel.timerTick();
        }
        System.out.println("[Shell] Advanced " + n + " tick(s).");
    }

    private void cmdCat(String[] args){
        if (args.length == 0){
            System.err.println("Usage: cat<file>");
            return;
        }
        FsNode node = currentDirectory.getChild(args[0]);
        if (node == null){
            System.err.println("[Shell] Not found " + args[0]);
            return;
        }
        if (!(node instanceof File)){
            System.err.println("[Shell] Not a file: " + args[0]);
            return;
        }

        System.out.println(((File)node).read());
    }

    private void cmdTouch(String[] args){
        if (args.length == 0){
            System.err.println("Usage: touch<file>");
            return;
        }
        try {
            File f = kernel.getFileSystem().createFileIn(currentDirectory,args[0]);
            System.out.println("[Shell] Created file: " + args[0]);
        }catch (IllegalArgumentException e){
            System.err.println("[Shell] " + e.getMessage());
        }
    }

    private void cmdAsm(String[] args){
        if (args.length == 0){
            System.err.println("Usage: asm <file>");
            return;
        }
        FsNode node = currentDirectory.getChild(args[0]);
        if (!(node instanceof File)){
            System.err.println("[Shell] File not found "  + args[0] );
            return;
        }
        File file = (File)node;
        kernel.getDma().transferToRAM(file);
        System.out.println("Enter assembler code (empty line to finish):");
        StringBuilder stringBuilder = new StringBuilder();
        while (true){
            System.out.print("asm> ");
            String line = scanner.nextLine();
            if (line.isEmpty()){
                break;
            }
            stringBuilder.append(line).append("\n");
        }

        try {
            String binary = com.osproject.assembler.Assembler.compileToBinary(stringBuilder.toString());
            file.write(binary);
            kernel.getDma().transferToDisk(file);
            System.out.println("[Shell] Compiled and saved " +args[0]);
        }catch (Exception e){
            System.err.println("[Shell] Assembly error: " + e.getMessage());
        }
    }

    private void cmdExec(String[] args){
        if (args.length == 0){
            System.err.println("Usage: exec<file> [priority]");
            return;
        }
        int priority = 5;
        if (args.length >= 2){
            try {
                priority = Integer.parseInt(args[1]);
            }catch (NumberFormatException e){
                System.err.println("[Shell] Priority must be a number");
                return;
            }
        }

        FsNode node = currentDirectory.getChild(args[0]);
        if (!(node instanceof File)){
            System.err.println("[Shell] File not found " + args[0]);
            return;
        }
        File file = (File) node;
        kernel.getDma().transferToRAM(file);
        String binary = file.read();
        kernel.getDma().transferToDisk(file);

        int pid = kernel.createProcessFromBinary(args[0],binary,priority);
        if (pid > 0){
            System.out.println("[Shell] Process started: PID=" + pid);
        }
    }
    private String getFullPath(Directory dir) {
        if (dir.getParent() == null) return "/";

        StringBuilder sb = new StringBuilder();
        Directory current = dir;

        while (current != null && current.getParent() != null) {
            sb.insert(0, "/" + current.getName());
            current = current.getParent();
        }

        return sb.length() == 0 ? "/" : sb.toString();
    }
}