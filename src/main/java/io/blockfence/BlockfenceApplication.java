package io.blockfence;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "Blockfence application api", version = "0.1", description = "Blockfence demo api"))
public class BlockfenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlockfenceApplication.class, args);
    }

}
