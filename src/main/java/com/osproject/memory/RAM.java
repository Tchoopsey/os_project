package com.osproject.memory;


import java.util.Arrays;

/**
 * RAM
 */
public class RAM {
    private int size;
    private int[] cells;
    
    public RAM(int size) {
        if (size <= 0 ){
			throw new IllegalArgumentException("RAM size must be larger than 0");
		}
		this.size = size;
		this.cells = new int[size];
    }

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int[] getCells() {
		return cells;
	}

	public void setCells(int[] cells) {
		this.cells = cells;
	}

	public void write (int address, int value){
		checkAddress(address);
		cells[address] = value;
	}

	private void checkAddress(int address) {
		if (address < 0 || address >= size){
			throw new IndexOutOfBoundsException("Address " + address + " is out of band of RAM  [0," + (size - 1) + "]");
		}
	}

	public void dump(){
		System.out.println("-----RAM (size = "+ size + ")-----");
		for (int i = 0 ; i < size; i++){
			System.out.printf("[%4d] = %d%n", i, cells[i]);
		}
		System.out.println("-----------------------------------");
	}

	@Override
	public String toString() {
		return "RAM{" +
				"size=" + size +
				", cells=" + Arrays.toString(cells) +
				'}';
	}
}
