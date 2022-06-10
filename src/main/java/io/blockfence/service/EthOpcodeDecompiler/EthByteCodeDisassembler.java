package io.blockfence.service.EthOpcodeDecompiler;

import io.blockfence.data.ContractOpcodes;
import io.blockfence.service.EthOpcodeDecompiler.iterators.StringTwoCharIterator;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.blockfence.service.EthOpcodeDecompiler.Opcodes.UNKNOWN;
import static io.blockfence.service.EthOpcodeDecompiler.Opcodes.getOpcode;
import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.lang.Integer.valueOf;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@Log4j2
public class EthByteCodeDisassembler {

    private static final String CONTRACT_METADATA_PREFIX = "a165627a7a72305820"; // 0xa1 0x65 'b' 'z' 'z' 'r' '0' 0x58 0x20 + <32 bytes swarm hash> <2 bytes length of the metadata>
    private static final int BEGIN_INDEX = 2;
    private static final int RADIX = 16;
    private static final String PREFIX = "0x";

    public ContractOpcodes buildContractOpcodesFromByteCode(String byteCodeString) {
        ContractOpcodes.ContractOpcodesBuilder builder = ContractOpcodes.builder();
        String[] codeStripped = cleanData(byteCodeString);
        if (codeStripped.length > 1) {
            builder.contractMetadata(CONTRACT_METADATA_PREFIX + codeStripped[1]);
        }
        return builder.disassembledCode(loadOpcodes(codeStripped[0])).build();
    }

    private String[] cleanData(String code) {
        if (code.startsWith(PREFIX)) {
            code = code.substring(BEGIN_INDEX);
        }
        return code.split(CONTRACT_METADATA_PREFIX);
    }

    private List<String> loadOpcodes(String contractByteCode) {
        StringTwoCharIterator iterator = new StringTwoCharIterator(contractByteCode);
        List<String> disassembledCodes = new ArrayList<>();
        int offset = 0;
        while (iterator.hasNext()) {
            String nextByte = iterator.next();
            Opcode opcode = new Opcode();
            opcode.setOffset(offset);
            Integer opcodeHex = valueOf(nextByte, RADIX);
            Opcodes opcodeDefinition = getOpcode(opcodeHex);
            if (opcodeDefinition == null) {
                //log.warn("Unknown opcode: " + opcodeHex);
                opcode.setOpcode(UNKNOWN);
            } else {
                opcode.setOpcode(opcodeDefinition);
                int parametersNum = opcodeDefinition.getParametersNum();
                if (parametersNum > 0) {
                    offset = offset + parametersNum;
                    String opParameter = getParameter(parametersNum, iterator);
                    String parameterString = opParameter.replaceAll(PREFIX, EMPTY_STRING);
                    if (isEmpty(parameterString)) {
                        opcode.setOpcode(UNKNOWN);
                    } else {
                        opcode.setParameter(new BigInteger(parameterString, RADIX));
                    }
                }
            }
            offset = offset + 1;
            disassembledCodes.add(opcode.toString());
        }
        return disassembledCodes;
    }

    private static String getParameter(int parametersNum, StringTwoCharIterator iterator) {
        StringBuilder sb = new StringBuilder(PREFIX);
        int i = 0;
        while (i < parametersNum && iterator.hasNext()) {
            String next = iterator.next();
            sb.append(next);
            i = i + 1;
        }
        return sb.toString();
    }
}