package com.osproject.assembler;

import java.util.Locale;

public class Instruction {
    private String opcode;
    private int operand;

    public Instruction(String opcode, int operand){
        this.opcode = opcode.toUpperCase();
        this.operand = operand;
    }

    public String getOpcode(){
        return opcode;
    }

    public int getOperand(){
        return operand;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "opcode='" + opcode + '\'' +
                ", operand=" + operand +
                '}';
    }
}
