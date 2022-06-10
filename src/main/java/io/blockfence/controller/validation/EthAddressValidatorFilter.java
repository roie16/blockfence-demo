package io.blockfence.controller.validation;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static java.util.Map.of;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;
import static org.web3j.crypto.WalletUtils.isValidAddress;

@Component
public class EthAddressValidatorFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @NotNull
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, @NotNull HandlerFunction<ServerResponse> handlerFunction) {
        return request.queryParam("address")
                .map(address -> isValidAddress(address) ?
                        handlerFunction.handle(request) :
                        badRequest().bodyValue(of("error", "Invalid Eth address")))
                .orElseThrow();
    }
}
