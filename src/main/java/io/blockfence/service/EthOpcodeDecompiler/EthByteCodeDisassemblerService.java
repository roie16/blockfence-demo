package io.blockfence.service.EthOpcodeDecompiler;

import io.blockfence.data.ContractOpcodes;
import io.blockfence.service.EthOpcodeDecompiler.iterators.StringTwoCharIterator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.blockfence.service.EthOpcodeDecompiler.Opcodes.UNKNOWN;
import static io.blockfence.service.EthOpcodeDecompiler.Opcodes.getOpcode;
import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.lang.Integer.valueOf;
import static java.util.Objects.requireNonNullElse;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Stream.iterate;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@Slf4j
public class EthByteCodeDisassemblerService {

    private static final String CONTRACT_METADATA_PREFIX = "a165627a7a72305820"; // 0xa1 0x65 'b' 'z' 'z' 'r' '0' 0x58 0x20 + <32 bytes swarm hash> <2 bytes length of the metadata>
    private static final int BEGIN_INDEX = 2;
    private static final int RADIX = 16;
    private static final String PREFIX = "0x";

    public ContractOpcodes buildContractOpcodesFromByteCode(String byteCodeString) {
        byteCodeString = requireNonNullElse(byteCodeString, EMPTY_STRING);
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
        AtomicInteger index = new AtomicInteger(0);
        iterate(iterator, Iterator::hasNext, identity())
                .map(StringTwoCharIterator::next)
                .forEach(nextBytes -> handleNextByes(index, nextBytes, disassembledCodes, iterator));
        return disassembledCodes;
    }

    private void handleNextByes(AtomicInteger index, String nextByte, List<String> disassembledCodes, StringTwoCharIterator iterator) {
        Opcode opcode = new Opcode();
        opcode.setOffset(index.get());
        Integer opcodeHex = valueOf(nextByte, RADIX);
        getOpcode(opcodeHex).ifPresentOrElse(opcodes -> doOnOpcode(index, iterator, opcode, opcodes), () -> doOnEmptyOpcode(opcode, opcodeHex));
        disassembledCodes.add(opcode.toString());
        index.incrementAndGet();
    }

    private void doOnOpcode(AtomicInteger index, StringTwoCharIterator iterator, Opcode opcode, Opcodes opcodes) {
        opcode.setOpcode(opcodes);
        int parametersNum = opcodes.getParametersNum();
        if (parametersNum > 0) {
            index.addAndGet(parametersNum);
            String opParameter = getParameter(parametersNum, iterator);
            String parameterString = opParameter.replaceAll(PREFIX, EMPTY_STRING);
            addParameterIfNotEmpty(opcode, parameterString);
        }
    }

    private void doOnEmptyOpcode(Opcode opcode, Integer opcodeHex) {
        log.warn("Unknown opcode: " + opcodeHex);
        opcode.setOpcode(UNKNOWN);
    }

    private void addParameterIfNotEmpty(Opcode opcode, String parameterString) {
        if (isEmpty(parameterString)) {
            opcode.setOpcode(UNKNOWN);
        } else {
            opcode.setParameter(new BigInteger(parameterString, RADIX));
        }
    }

    private String getParameter(int parameterSize, StringTwoCharIterator iterator) {
        StringBuilder stringBuilder = new StringBuilder(PREFIX);
        AtomicInteger parameterIndex = new AtomicInteger(0);
        iterate(iterator, stringTwoCharIterator -> isParameter(parameterSize, parameterIndex, stringTwoCharIterator), identity())
                .map(StringTwoCharIterator::next)
                .forEach(nextString -> appendToParameterString(stringBuilder, parameterIndex, nextString));
        return stringBuilder.toString();
    }

    private void appendToParameterString(StringBuilder stringBuilder, AtomicInteger index, String nextString) {
        stringBuilder.append(nextString);
        index.incrementAndGet();
    }

    private boolean isParameter(int parameterSize, AtomicInteger index, StringTwoCharIterator stringTwoCharIterator) {
        return stringTwoCharIterator.hasNext() && index.get() < parameterSize;
    }
}