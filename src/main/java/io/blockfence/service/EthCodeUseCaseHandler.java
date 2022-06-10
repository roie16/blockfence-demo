package io.blockfence.service;

import io.blockfence.data.ContractsCodes;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static reactor.core.publisher.Mono.just;

@Service
public class EthCodeUseCaseHandler {

    @Value("classpath:evm-bytecode-parser.js")
    private Resource parser;

    private final Ethereum web3j;

    public EthCodeUseCaseHandler(Ethereum web3j) {
        this.web3j = web3j;
    }

    @SneakyThrows
    public Mono<ContractsCodes> generateContractCodeForAddress(Optional<String> address) {
        return address
                .map(this::getContractsCodesMono)
                .orElseThrow();
    }

    @SneakyThrows
    private Mono<ContractsCodes> getContractsCodesMono(String address) {
        EthGetCode ethGetCode = web3j.ethGetCode(address, DefaultBlockParameterName.LATEST).send();
//        String opcode = "";
//        try (V8Runtime v8Runtime = V8Host.getNodeInstance().createV8Runtime();
//             InputStream inputStream = parser.getInputStream()) {
//            String script = new String(inputStream.readAllBytes(), UTF_8);
//            script = "const bytecode = " + ethGetCode.getCode() + script;
//            opcode = v8Runtime.getExecutor(script).executeString();
//        }
        return just(new ContractsCodes(ethGetCode.getCode()));
    }

    @SneakyThrows
    public Mono<String> getClientVersion() {
        Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        return just("Client version" + clientVersion);
    }
}
