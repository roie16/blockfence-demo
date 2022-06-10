package io.blockfence.controller;

import io.blockfence.controller.validation.EthAddressValidatorFilter;
import io.blockfence.data.ContractsCodes;
import io.blockfence.service.EthCodeUseCaseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class EthCodeUseCaseRouter {

    private static final String V1 = "/v1";
    private static final String ADDRESS = "address";
    private static final String VERSION = "/version";
    private static final String CONTRACT_RAW_DATA = "/contractrawdata";
    private static final String ETH = "/eth";

    private final EthCodeUseCaseHandler ethCodeUseCasehandler;
    private final EthAddressValidatorFilter ethAddressValidatorFilter;

    public EthCodeUseCaseRouter(EthCodeUseCaseHandler ethCodeUseCasehandler, EthAddressValidatorFilter ethAddressValidatorFilter) {
        this.ethCodeUseCasehandler = ethCodeUseCasehandler;
        this.ethAddressValidatorFilter = ethAddressValidatorFilter;
    }

    @Bean
    public RouterFunction<ServerResponse> EthCodeUseCaseRoutes() {
        return nest(path(V1 + ETH),
                route(GET(CONTRACT_RAW_DATA), getEthDataByAddress())
                        .filter(ethAddressValidatorFilter)
                        .andRoute(GET(VERSION), getClientVersion()));
    }

    private HandlerFunction<ServerResponse> getEthDataByAddress() {
        return request -> ok().body(ethCodeUseCasehandler.generateContractCodeForAddress(request.queryParam(ADDRESS)), ContractsCodes.class);
    }

    private HandlerFunction<ServerResponse> getClientVersion() {
        return request -> ok().body(ethCodeUseCasehandler.getClientVersion(), String.class);
    }


}
