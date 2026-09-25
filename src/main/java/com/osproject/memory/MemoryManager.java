package com.osproject.memory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.osproject.process.PCB;

/**
 * MemoryManager
 */
public class MemoryManager {
    private RAM ram;
    private List<MemorySegment> segments;
    private LinkedList<MemorySegment> freeList;
    private static final int[] PARTITION_SIZES = {64,128,256,512};

	public MemoryManager(int ramSize) {
		this.ram = new RAM(ramSize);
        this.segments = new ArrayList<>();
        this.freeList = new LinkedList<>();

	}

    public boolean allocate(PCB p, int size) {
        if (p == null){
            throw new IllegalArgumentException("PCB is null");
        }
        for (int i = 0; i < freeList.size(); i++){
            MemorySegment segment = freeList.get(i);
            if (segment.getSize() >= size){
                freeList.remove(i);
                segment.setOwner(p);
                p.setBaseAddress(segment.getBase());
                p.setLimit(segment.getLimit());

                System.out.println("[MemoryManager] Allocated partition [ " + segment.getBase() +" ," + segment.getLimit() + "] size = " + segment.getSize() + " for PID = " + p.getPid() );
                return true;
            }
        }
        throw new IllegalArgumentException("No partiton large enough for size =" + size );
    }
    
    public void free(PCB p) {
        MemorySegment segToRemove = null;

        // pronalazi trazeni segment
        for (MemorySegment seg : segments) {
            if (seg.getOwner() == p) {
                segToRemove = seg;
                break;
            }
        }

        if (segToRemove == null){
            return;
        }

        if (segToRemove != null) {
            segments.remove(segToRemove);
            p.setBaseAddress(0);
            p.setLimit(0);
            freeList.add(segToRemove);
        }

        System.out.println("[MemoryManager] freed partition [ " + segToRemove.getBase() + ", " + segToRemove.getLimit() + "]");
    }

    public int read(PCB p, int address) {
        checkAddress(p,address);
        return ram.getCells()[address];
    }

    public void write(PCB p, int address, int value) {
        if (address < p.getBaseAddress() ||
            address > p.getLimit()) {
            System.err.println("GRESKA: Adresa van memorije!");
        }
        ram.getCells()[address] = value;
    }

    public RAM getRam() {
        return ram;
    }

    public List<MemorySegment> getSegments() {
        List<MemorySegment> allocated = new ArrayList<>();
        for (MemorySegment memorySegment : segments){
            if (!memorySegment.isFree()){
                allocated.add(memorySegment);
            }
        }
        return allocated;
    }

    public LinkedList<MemorySegment> getFreeList(){
        return freeList;
    }

    public int getFreeMemorySize(){
        int total = 0;
        for (MemorySegment memorySegment : freeList){
            total+=memorySegment.getSize();
        }
        return total;
    }

    public int getUsedMemorySize(){
        int total = 0;
        for (MemorySegment segment: segments){
            if (!segment.isFree()){
                total += segment.getSize();
            }
        }
        return total;
    }

    public String dumpMemory() {
        StringBuilder sb = new StringBuilder();

        sb.append("Memory:\n\n");

        for (MemorySegment seg : segments) {
            sb.append("PID ")
                .append(seg.getOwner().getPid())
                .append(": ")
                .append(seg.getBase())
                .append(" - ")
                .append(seg.getLimit())
                .append("\n");
        }

        return sb.toString();
    }

    private void initializePartitions(int totalSize){
        int address = 0;
        int index = 0;
        while (address < totalSize){
            int size = PARTITION_SIZES[index % PARTITION_SIZES.length];
            if (address + size > totalSize){
                size = totalSize - address;
            }
            if (size <= 0){
                break;
            }
            MemorySegment segment = new MemorySegment(null,address,address + size-1);
            freeList.add(segment);
            segments.add(segment);
            index ++;

        }
        System.out.println("[MemoryManager] Initialized " + freeList.size() + " fixed partitions (continuous allocation(");
    }

    private void checkAddress(PCB p, int address){
        if (address < p.getBaseAddress() || address > p.getLimit()){
            throw new SecurityException("Address space violation PID = " + p.getPid() + " tried accessing address " + address + " outside [" + p.getBaseAddress() + ", " + p.getLimit() + "]");
        }
    }
}
