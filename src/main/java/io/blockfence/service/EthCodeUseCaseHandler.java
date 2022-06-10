package io.blockfence.service;

import io.blockfence.data.ContractsCodes;
import io.blockfence.service.EthOpcodeDecompiler.EthByteCodeDisassembler;
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

    private final Ethereum web3j;
    private final EthByteCodeDisassembler ethByteCodeDisassembler;

    public EthCodeUseCaseHandler(Ethereum web3j, EthByteCodeDisassembler ethByteCodeDisassembler) {
        this.web3j = web3j;
        this.ethByteCodeDisassembler = ethByteCodeDisassembler;
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
                .map(ethGetCode -> new ContractsCodes(ethGetCode.getCode(),
                        ethByteCodeDisassembler.buildContractOpcodesFromByteCode(ethGetCode.getCode())))
                .subscribeOn(boundedElastic());
    }

    @SneakyThrows
    public Mono<String> getClientVersion() {
        return fromCallable(() -> web3j.web3ClientVersion().send())
                .map(web3ClientVersion -> "Client version" + web3ClientVersion)
                .subscribeOn(boundedElastic());
    }
}
