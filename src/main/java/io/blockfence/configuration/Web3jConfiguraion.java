package io.blockfence.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import static org.web3j.protocol.Web3j.build;

@Configuration
public class Web3jConfiguraion {

    @Value("${blockfence.infura.mainnet.url}")
    private String url;

    @Bean
    public Web3j web3j() {
        return build(new HttpService(url));

    }

}
