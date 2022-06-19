package io.blockfence.service.EthOpcodeDecompiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.blockfence.data.ContractOpcodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class EthByteCodeDisassemblerTest {

    public static final String SIMPLE_STRING = "0x600035601c52";

    @InjectMocks
    private EthByteCodeDisassemblerService ethByteCodeDisassemblerService;

    private final ClassLoader classLoader = getClass().getClassLoader();


    @Test
    void buildSimpleContractOpcodesFromByteCode() throws IOException {
        ContractOpcodes expected = readContractOpcodesFromResource("contractOpcodes/SimpleContractOpcodes.json");
        ContractOpcodes contractOpcodes = ethByteCodeDisassemblerService.buildContractOpcodesFromByteCode(SIMPLE_STRING);
        assertEquals(expected, contractOpcodes);
    }


    @Test
    void buildContractOpcodesFromByteCodeMetadata() throws IOException, URISyntaxException {
        ContractOpcodes expected = readContractOpcodesFromResource("contractOpcodes/metadata/metadata.json");
        String byteCode = Files.readString(Path.of(requireNonNull(classLoader.getResource("contractOpcodes/metadata/byteCode")).toURI()));
        ContractOpcodes contractOpcodes = ethByteCodeDisassemblerService.buildContractOpcodesFromByteCode(byteCode);
        assertEquals(expected, contractOpcodes);
    }

    @Test
    void buildContractOpcodesFromByteCodeNoMetadata() throws IOException, URISyntaxException {
        ContractOpcodes expected = readContractOpcodesFromResource("contractOpcodes/no_metadata/metadata.json");
        String byteCode = Files.readString(Path.of(requireNonNull(classLoader.getResource("contractOpcodes/no_metadata/byteCode")).toURI()));
        ContractOpcodes contractOpcodes = ethByteCodeDisassemblerService.buildContractOpcodesFromByteCode(byteCode);
        assertEquals(expected, contractOpcodes);
    }

    private ContractOpcodes readContractOpcodesFromResource(String path) throws IOException {
        URL url = classLoader.getResource(path);
        return new ObjectMapper().readValue(url, ContractOpcodes.class);
    }
}