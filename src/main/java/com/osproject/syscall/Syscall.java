package com.osproject.syscall;

import java.util.List;

/**
 * Syscall
 */
public class Syscall {
    private SyscallType type;
    private List<String> args;

	public Syscall(SyscallType type, List<String> args) {
		this.type = type;
		this.args = args;
	}

	public SyscallType getType() {
		return type;
	}

	public void setType(SyscallType type) {
		this.type = type;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}
}
