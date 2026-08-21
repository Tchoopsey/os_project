package com.osproject.memory;

import java.util.ArrayList;
import java.util.List;

import com.osproject.process.PCB;

/**
 * MemoryManager
 */
public class MemoryManager {
    private RAM ram;
    private List<MemorySegment> segments;

	public MemoryManager(RAM ram) {
		this.ram = ram;
        this.segments = new ArrayList<>();
	}

    private boolean allocate(PCB p, int size) {
        int base = 0;

        // pronalazi prvi slobodan segment
        for (MemorySegment seg : segments) {
            if (base + size - 1 < seg.getBase()) {
                break;
            }
            base = seg.getLimit() - 1;
        }

        // provjera slobodnog RAM prostora
        if (base + size > ram.getSize()) {
            return false;
        }

        int limit = base + size - 1;
        p.setBaseAddress(base);
        p.setLimit(limit);

        MemorySegment segment = new MemorySegment(p, base, limit);
        segments.add(segment);

        return true;
    }
    
    private void free(PCB p) {
        MemorySegment segToRemove = null;

        // pronalazi trazeni segment
        for (MemorySegment seg : segments) {
            if (seg.getOwner() == p) {
                segToRemove = seg;
                break;
            }
        }

        if (segToRemove != null) {
            segments.remove(segToRemove);
            p.setBaseAddress(0);
            p.setLimit(0);
        }
    }

    private int read(PCB p, int address) {
        if (address < p.getBaseAddress() ||
            address > p.getLimit()) {
            System.err.println("GRESKA: Adresa van memorije!");
        }
        return ram.getCells()[address];
    }

    private void write(PCB p, int address, int value) {
        if (address < p.getBaseAddress() ||
            address > p.getLimit()) {
            System.err.println("GRESKA: Adresa van memorije!");
        }
        ram.getCells()[address] = value;
    }

    private String dumpMemory() {
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
}
