package com.osproject.memory;

import com.osproject.process.PCB;

/**
 * MemorySegment
 */
public class MemorySegment {
    private PCB owner;
    private int base;
    private int limit;

	public MemorySegment(PCB owner, int base, int limit) {
		this.owner = owner;
		this.base = base;
		this.limit = limit;
	}
}
