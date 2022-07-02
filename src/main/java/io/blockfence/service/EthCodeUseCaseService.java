package io.blockfence.service;

import io.blockfence.data.AddressesDTO;
import io.blockfence.data.Contract;
import io.blockfence.data.ContractOpcodes;
import io.blockfence.service.EthOpcodeDecompiler.EthByteCodeDisassemblerService;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Ethereum;
import org.web3j.protocol.core.methods.response.EthGetCode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static reactor.core.publisher.Flux.fromIterable;
import static reactor.core.publisher.Mono.fromCallable;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Service
public class EthCodeUseCaseService {

    private final Ethereum web3j;
    private final EthByteCodeDisassemblerService ethByteCodeDisassemblerService;

    public EthCodeUseCaseService(Ethereum web3j, EthByteCodeDisassemblerService ethByteCodeDisassemblerService) {
        this.web3j = web3j;
        this.ethByteCodeDisassemblerService = ethByteCodeDisassemblerService;
    }


    public Flux<Contract> generateContractCodeForAddressList(AddressesDTO addressesDTO) {
        return fromIterable(addressesDTO.getAddresses())
                .map(this::getEthGetCodeByAddress)
                .subscribeOn(boundedElastic());
    }

    public Mono<Contract> getContractsCodesMono(String address) {
        return fromCallable(() -> getEthGetCodeByAddress(address))
                .subscribeOn(boundedElastic());
    }

    public Mono<String> getClientVersion() {
        return fromCallable(() -> web3j.web3ClientVersion().send())
                .map(web3ClientVersion -> "client version: " + web3ClientVersion)
                .subscribeOn(boundedElastic());
    }

    private Contract getEthGetCodeByAddress(String address) {
        try {
            EthGetCode ethGetCode = web3j.ethGetCode(address, DefaultBlockParameterName.LATEST).send();
            ContractOpcodes contractOpcodes = ethByteCodeDisassemblerService.buildContractOpcodesFromByteCode(ethGetCode.getCode());
            return new Contract(address, ethGetCode.getCode(), contractOpcodes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
