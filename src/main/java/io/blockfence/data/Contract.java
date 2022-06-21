package io.blockfence.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    private String address;
    private String byteCode;
    private ContractOpcodes contractOpcodes;
}
