package com.osproject.assembler;

import java.util.ArrayList;
import java.util.List;

public class Assembler {
    public static List<Instruction> assemble (String source){
        List<Instruction> program = new ArrayList<>();
        String[] lines = source.split("\n");

        for (String line : lines){
            line = line.trim();
            if (line.isEmpty() || line.startsWith(";")){
                continue;
            }
            String[] parts = line.split("\\s+");
            String opcode = parts[0].toUpperCase();

            if (opcode.equals("HALT")){
                program.add(new Instruction("HALT",0));
                continue;
            }
            if (parts.length < 2){
                throw new IllegalArgumentException(opcode + " requires operand");
            }

            int operand = Integer.parseInt(parts[1]);
            program.add(new Instruction(opcode,operand));
        }
        return program;
    }

    public static String compileToBinary (String source){
        List<Instruction> program = assemble(source);
        StringBuilder stringBuilder = new StringBuilder();

        for (Instruction instruction : program){
            int encoded = BinaryEncoder.encode(instruction.getOpcode(), instruction.getOperand());
            stringBuilder.append(BinaryEncoder.toBinaryString(encoded)).append("\n");
        }
        return stringBuilder.toString();
    }

    public static List<Instruction> fromBinary(String binary){
        List<Instruction> program = new ArrayList<>();
        for (String line: binary.split("\n")){
            line = line.trim();
            if (line.isEmpty()){
                continue;
            }
            int encoded = Integer.parseInt(line,2);
            String decoded = BinaryEncoder.decode(encoded);
            String[] parts = decoded.split(" ");
            program.add(new Instruction(parts[0],Integer.parseInt(parts[1])));
        }
        return program;
    }
}
