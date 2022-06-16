package io.blockfence.service;

import io.blockfence.data.AddressesDTO;
import io.blockfence.data.ContractsCodes;
import io.blockfence.service.EthOpcodeDecompiler.EthByteCodeDisassembler;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.methods.response.EthGetCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Optional;

import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Service
public class EthCodeUseCaseService {

    private final Ethereum web3j;
    private final EthByteCodeDisassembler ethByteCodeDisassembler;

    public EthCodeUseCaseService(Ethereum web3j, EthByteCodeDisassembler ethByteCodeDisassembler) {
        this.web3j = web3j;
        this.ethByteCodeDisassembler = ethByteCodeDisassembler;
    }


    public Flux<ContractsCodes> generateContractCodeForAddressList(AddressesDTO addressesDTO) {
        return fromIterable(addressesDTO.getAddresses())
                .map(this::getEthGetCodeByAddress)
                .subscribeOn(boundedElastic());
    }

    public Mono<ContractsCodes> generateContractCodeForAddress(Optional<String> address) {
        return address
                .map(this::getContractsCodesMono)
                .orElseThrow();
    }

    public Mono<String> getClientVersion() {
        return fromCallable(() -> web3j.web3ClientVersion().send())
                .map(web3ClientVersion -> "Client version" + web3ClientVersion)
                .subscribeOn(boundedElastic());
    }

    private Mono<ContractsCodes> getContractsCodesMono(String address) {
        return fromCallable(() -> getEthGetCodeByAddress(address))
                .subscribeOn(boundedElastic());
    }

    private ContractsCodes getEthGetCodeByAddress(String address) {
        try {
            EthGetCode ethGetCode = web3j.ethGetCode(address, DefaultBlockParameterName.LATEST).send();
            return new ContractsCodes(ethGetCode.getCode(), ethByteCodeDisassembler.buildContractOpcodesFromByteCode(ethGetCode.getCode()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
