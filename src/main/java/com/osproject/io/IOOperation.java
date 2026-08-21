package com.osproject.io;

/**
 * IOOperation
 */
public class IOOperation {
    private IOType type;
    private String data;
    private int duration;

	public IOOperation(IOType type, String data, int duration) {
		this.type = type;
		this.data = data;
		this.duration = duration;
	}

	public IOType getType() {
		return type;
	}

	public void setType(IOType type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
}
