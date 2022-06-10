package io.blockfence.service;

import io.blockfence.data.ContractsCodes;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Ethereum;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Service
public class EthCodeUseCaseHandler {

//    @Value("classpath:evm-bytecode-parser.js")
//    private Resource parser;

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
        return fromCallable(() -> web3j.ethGetCode(address, DefaultBlockParameterName.LATEST).send())
                .map(ethGetCode -> new ContractsCodes(ethGetCode.getCode()))
                .subscribeOn(boundedElastic());
//        String opcode = "";
//        try (V8Runtime v8Runtime = V8Host.getNodeInstance().createV8Runtime();
//             InputStream inputStream = parser.getInputStream()) {
//            String script = new String(inputStream.readAllBytes(), UTF_8);
//            script = "const bytecode = " + ethGetCode.getCode() + script;
//            opcode = v8Runtime.getExecutor(script).executeString();
//        }
    }

    @SneakyThrows
    public Mono<String> getClientVersion() {
        return fromCallable(() -> web3j.web3ClientVersion().send())
                .map(web3ClientVersion -> "Client version" + web3ClientVersion)
                .subscribeOn(boundedElastic());
    }
}
