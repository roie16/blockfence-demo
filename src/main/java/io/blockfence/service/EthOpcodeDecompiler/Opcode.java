package io.blockfence.service.EthOpcodeDecompiler;

import lombok.Data;

import java.math.BigInteger;

import static java.lang.String.format;

@Data
public class Opcode {

    private int offset;
    private Opcodes opcode;
    private BigInteger parameter;

    @Override
    public String toString() {
        String toString = "0x" + format("%03X", this.offset) + " " + this.opcode.name();
        if (parameter != null) {
            toString += " 0x" + parameter.toString(16);
        }
        return toString;
    }
}
