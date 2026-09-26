package com.osproject.assembler;

public class BinaryEncoder {


    private static int getOpcodeBits(String op){
        switch (op.toUpperCase()){
            case "LOAD": return 1;
            case "STORE": return 2;
            case "ADD": return 3;
            case "SUB": return 4;
            case "MUL": return 5;
            case "DIV": return 6;
            case "JMP": return 7;
            case "JZ": return 8;
            case "HALT": return 9;
            default: throw new IllegalArgumentException("Unknown opcode " + op);
        }
    }

    private static String getOpcodeName(int bits){
        switch (bits){
            case 1: return "LOAD";
            case 2: return "STORE";
            case 3: return "ADD";
            case 4: return "SUB";
            case 5: return "MUL";
            case 6: return "DIV";
            case 7: return "JMP";
            case 8: return "JZ";
            case 9: return "HALT";
            default: return "UNKNOWN";

        }
    }

    public static int encode (String opcode, int operand){
        return (getOpcodeBits(opcode) << 16 | (operand & 0xFFFF));
    }

    public static String decode(int binary){
        int op = (binary >> 16) & 0xFFFF;
        int arg = binary & 0xFFFF;
        return getOpcodeName(op) + " " + arg;
    }

    public static String toBinaryString(int value){
        return String.format("%32", Integer.toBinaryString(value)).replace(' ', '0');
    }


}
