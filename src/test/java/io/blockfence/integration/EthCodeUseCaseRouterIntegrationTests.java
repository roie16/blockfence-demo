package io.blockfence.integration;


import io.blockfence.data.AddressesDTO;
import io.blockfence.data.AddressesError;
import io.blockfence.data.Contract;
import io.blockfence.router.EthCodeUseCaseRouter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest
public class EthCodeUseCaseRouterIntegrationTests {

    private static final String V_1_ETH_CONTRACTRAWDATA_ADDRESS = "/v1/eth/contractrawdata?address={1}";
    private static final String V_1_ETH_CONTRACTRAWDATA = "/v1/eth/contractrawdata";
    private static final String ADDRESS = "0x7D56485e026D5D3881F778E99969D2b1F90c50aF";
    private static final String BLABLA = "blabla";
    private static final String ADDRESS_2 = "0x00000000006c3852cbEf3e08E8dF289169EdE581";

    @Autowired
    private EthCodeUseCaseRouter ethCodeUseCaseRouter;

    @Test
    @DisplayName("Test contractrawdata GET request")
    public void validGetRequestIntegrationTest() {
        WebTestClient
                .bindToRouterFunction(ethCodeUseCaseRouter.EthCodeUseCaseRoutes())
                .build()
                .get()
                .uri(V_1_ETH_CONTRACTRAWDATA_ADDRESS, ADDRESS)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Contract.class)
                .consumeWith(serverResponse -> {
                    assertNotNull(serverResponse.getResponseBody());
                    assertFalse(serverResponse.getResponseBody().getContractOpcodes().getDisassembledCode().isEmpty());
                    assertFalse(serverResponse.getResponseBody().getByteCode().isBlank());
                    assertFalse(serverResponse.getResponseBody().getAddress().isBlank());
                });
    }

    @Test
    @DisplayName("Test contractrawdata POST request")
    public void validPostRequestIntegrationTest() {
        WebTestClient
                .bindToRouterFunction(ethCodeUseCaseRouter.EthCodeUseCaseRoutes())
                .build()
                .post()
                .uri(V_1_ETH_CONTRACTRAWDATA)
                .bodyValue(validRequestBody(ADDRESS, ADDRESS_2))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Contract.class)
                .consumeWith(serverResponse -> {
                    assertNotNull(serverResponse.getResponseBody());
                    assertFalse(serverResponse.getResponseBody().get(0).getByteCode().isBlank());
                    assertFalse(serverResponse.getResponseBody().get(0).getAddress().isBlank());
                });
    }

    @Test
    @DisplayName("Test contractrawdata POST request multiple(same address)")
    public void validPostRequestIntegrationTestMultipleAddresses() {
        WebTestClient
                .bindToRouterFunction(ethCodeUseCaseRouter.EthCodeUseCaseRoutes())
                .build()
                .post()
                .uri(V_1_ETH_CONTRACTRAWDATA)
                .bodyValue(validRequestBody(ADDRESS, ADDRESS_2, ADDRESS))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Contract.class)
                .consumeWith(serverResponse -> {
                    assertNotNull(serverResponse.getResponseBody());
                    assertEquals(2, serverResponse.getResponseBody().size());
                    assertFalse(serverResponse.getResponseBody().get(0).getByteCode().isBlank());
                    assertFalse(serverResponse.getResponseBody().get(0).getAddress().isBlank());
                });
    }


    @Test
    @DisplayName("Test contractrawdata GET invalid address request")
    public void invalidAddressRequestIntegrationTest() {
        WebTestClient
                .bindToRouterFunction(ethCodeUseCaseRouter.EthCodeUseCaseRoutes())
                .build()
                .get()
                .uri(V_1_ETH_CONTRACTRAWDATA_ADDRESS, BLABLA)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(AddressesError.class)
                .consumeWith(serverResponse -> {
                    assertNotNull(serverResponse.getResponseBody());
                    assertFalse(serverResponse.getResponseBody().getMsg().isBlank());
                });
    }

    @Test
    @DisplayName("Test contractrawdata GET no address request")
    public void noAddressRequestIntegrationTest() {
        WebTestClient
                .bindToRouterFunction(ethCodeUseCaseRouter.EthCodeUseCaseRoutes())
                .build()
                .get()
                .uri(V_1_ETH_CONTRACTRAWDATA)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(AddressesError.class)
                .consumeWith(serverResponse -> {
                    assertNotNull(serverResponse.getResponseBody());
                    assertFalse(serverResponse.getResponseBody().getMsg().isBlank());
                });
    }

    @Test
    @DisplayName("Test invalid contractrawdata POST request")
    public void inValidPostRequestIntegrationTest() {
        WebTestClient
                .bindToRouterFunction(ethCodeUseCaseRouter.EthCodeUseCaseRoutes())
                .build()
                .post()
                .uri(V_1_ETH_CONTRACTRAWDATA)
                .bodyValue(invalidRequestBody())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody(AddressesError.class)
                .consumeWith(serverResponse -> {
                    assertNotNull(serverResponse.getResponseBody());
                    assertFalse(serverResponse.getResponseBody().getMsg().isBlank());
                });
    }

    private AddressesDTO validRequestBody(String... addresses) {
        AddressesDTO addressesDTO = new AddressesDTO();
        addressesDTO.setAddresses(List.of(addresses));
        return addressesDTO;
    }

    private AddressesDTO invalidRequestBody() {
        AddressesDTO addressesDTO = new AddressesDTO();
        addressesDTO.setAddresses(List.of(ADDRESS, BLABLA));
        return addressesDTO;
    }


}
