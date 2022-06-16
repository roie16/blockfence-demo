package io.blockfence.controller;

import io.blockfence.controller.validation.EthAddressHeaderValidatorFilter;
import io.blockfence.data.AddressesDTO;
import io.blockfence.data.AddressesError;
import io.blockfence.data.ContractsCodes;
import io.blockfence.handler.EthCodeHandler;
import io.blockfence.service.EthCodeUseCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.QUERY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class EthCodeUseCaseRouter {

    private static final String V1 = "/v1";
    private static final String VERSION = "/version";
    private static final String CONTRACT_RAW_DATA = "/contractrawdata";
    private static final String ETH = "/eth";

    private final EthAddressHeaderValidatorFilter ethAddressHeaderValidatorFilter;
    private final EthCodeHandler ethCodeHandler;

    public EthCodeUseCaseRouter(EthAddressHeaderValidatorFilter ethAddressHeaderValidatorFilter, EthCodeHandler ethCodeHandler) {

        this.ethAddressHeaderValidatorFilter = ethAddressHeaderValidatorFilter;
        this.ethCodeHandler = ethCodeHandler;
    }

    @RouterOperations({
            @RouterOperation(path = V1 + ETH + CONTRACT_RAW_DATA,
                    produces = APPLICATION_JSON_VALUE, method = POST,
                    operation = @Operation(operationId = "generateContractCodeForAddressList",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation",
                                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractsCodes.class)))),
                                    @ApiResponse(responseCode = "400", description = "Invalid Eth address",
                                            content = @Content(schema = @Schema(implementation = AddressesError.class)))},
                            requestBody = @RequestBody(required = true, description = "Get multiple addresses",
                                    content = @Content(schema = @Schema(implementation = AddressesDTO.class))))),
            @RouterOperation(path = V1 + ETH + CONTRACT_RAW_DATA,
                    produces = APPLICATION_JSON_VALUE, method = GET,
                    operation = @Operation(operationId = "generateContractCodeForAddress",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = ContractsCodes.class))),
                                    @ApiResponse(responseCode = "400", description = "Invalid Eth address",
                                            content = @Content(schema = @Schema(implementation = AddressesError.class)))},
                            parameters = {@Parameter(in = QUERY, name = "address")})),
            @RouterOperation(path = V1 + ETH + VERSION,
                    produces = APPLICATION_JSON_VALUE, method = GET, beanClass = EthCodeUseCaseService.class, beanMethod = "getClientVersion",
                    operation = @Operation(operationId = "getClientVersion",
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "successful operation",
                                            content = @Content(schema = @Schema(implementation = String.class)))})
            )})
    @Bean
    public RouterFunction<ServerResponse> EthCodeUseCaseRoutes() {
        return nest(path(V1 + ETH),
                route(GET(CONTRACT_RAW_DATA), ethCodeHandler::getEthDataByAddress).filter(ethAddressHeaderValidatorFilter)
                        .andRoute(GET(VERSION), ethCodeHandler::getClientVersion)
                        .andRoute(POST(CONTRACT_RAW_DATA), ethCodeHandler::getMultipleDataByAddressListBody));
    }
}
