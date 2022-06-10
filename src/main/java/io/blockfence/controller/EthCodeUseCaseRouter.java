package io.blockfence.controller;

import io.blockfence.controller.validation.EthAddressValidatorFilter;
import io.blockfence.data.ContractsCodes;
import io.blockfence.service.EthCodeUseCaseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
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
    @RouterOperations(
            {
                    @RouterOperation(path = V1 + ETH + CONTRACT_RAW_DATA
                            , produces = APPLICATION_JSON_VALUE, method = GET, beanClass = EthCodeUseCaseHandler.class, beanMethod = "generateContractCodeForAddress",
                            operation = @Operation(operationId = "generateContractCodeForAddress", responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = ContractsCodes.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid address")}, parameters = {
                                    @Parameter(in = QUERY, name = "address")})
                    ),
                    @RouterOperation(path = V1 + ETH + VERSION
                            , produces = APPLICATION_JSON_VALUE, method = GET, beanClass = EthCodeUseCaseHandler.class, beanMethod = "getClientVersion",
                            operation = @Operation(operationId = "generateContractCodeForAddress", responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = String.class)))})
                    )

            })
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
