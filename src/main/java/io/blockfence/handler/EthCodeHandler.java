package io.blockfence.handler;

import am.ik.yavi.core.ViolationDetail;
import io.blockfence.data.AddressesDTO;
import io.blockfence.data.AddressesError;
import io.blockfence.data.ContractsCodes;
import io.blockfence.handler.validator.AddressesDTOValidator;
import io.blockfence.service.EthCodeUseCaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;

import static am.ik.yavi.core.ConstraintViolations.of;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class EthCodeHandler {
    private static final String ADDRESS = "address";

    private final EthCodeUseCaseService ethCodeUseCaseService;
    private final AddressesDTOValidator addressesDTOValidator;


    public EthCodeHandler(EthCodeUseCaseService ethCodeUseCaseService, AddressesDTOValidator addressesDTOValidator) {
        this.ethCodeUseCaseService = ethCodeUseCaseService;
        this.addressesDTOValidator = addressesDTOValidator;
    }

    @NotNull
    public Mono<ServerResponse> getMultipleDataByAddressListBody(ServerRequest request) {
        return request.bodyToMono(AddressesDTO.class)
                .flatMap(addressesDTO -> addressesDTOValidator.getAddressesDTOValidator()
                        .applicative()
                        .validate(addressesDTO)
                        .map(ethCodeUseCaseService::generateContractCodeForAddressList)
                        .mapErrors(violations -> of(violations).details())
                        .fold(this::buildErrorResponse, contractsCodes -> ok().body(contractsCodes, ContractsCodes.class)));
    }

    @NotNull
    public Mono<ServerResponse> getEthDataByAddress(ServerRequest request) {
        return request.queryParam(ADDRESS)
                .map(address -> addressesDTOValidator.getAddressValidator()
                        .applicative()
                        .validate(address)
                        .map(ethCodeUseCaseService::getContractsCodesMono)
                        .mapErrors(violations -> of(violations).details())
                        .fold(this::buildErrorResponse, contractsCodes -> ok().body(contractsCodes, ContractsCodes.class)))
                .orElseGet(() -> badRequest().bodyValue(new AddressesError("address query parameter not found", List.of())));
    }

    @NotNull
    public Mono<ServerResponse> getClientVersion(ServerRequest ignored) {
        return ok().body(ethCodeUseCaseService.getClientVersion(), String.class);
    }

    private Mono<ServerResponse> buildErrorResponse(List<ViolationDetail> violationDetails) {
        List<String> addresses = getInvalidAddresses(violationDetails);
        return badRequest().bodyValue(new AddressesError("Invalid Eth addresses", addresses));
    }

    private List<String> getInvalidAddresses(List<ViolationDetail> violationDetails) {
        return violationDetails.stream()
                .map(violationDetail -> violationDetail.getArgs()[1])
                .map(Object::toString)
                .distinct()
                .toList();
    }

}
