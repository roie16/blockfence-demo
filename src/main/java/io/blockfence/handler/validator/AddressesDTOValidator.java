package io.blockfence.handler.validator;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.base.ContainerConstraintBase;
import am.ik.yavi.core.Validator;
import io.blockfence.data.AddressesDTO;
import org.springframework.stereotype.Component;
import org.web3j.crypto.WalletUtils;

import static am.ik.yavi.core.ViolationMessage.of;

@Component
public class AddressesDTOValidator {
    private final Validator<String> addressValidator = ValidatorBuilder.<String>of()
            .constraint(String::toString, "addressesLegal", c -> c.notBlank().predicate(WalletUtils::isValidAddress,
                    of("illegal.address", "Address \"{0}\" is illegal")))
            .build();


    private final Validator<AddressesDTO> addressesDTOValidator = ValidatorBuilder.<AddressesDTO>of()
            .constraint(AddressesDTO::getAddresses, "addressesNotEmpty", ContainerConstraintBase::notEmpty)
            .forEach(AddressesDTO::getAddresses, "addressesLegal", addressValidator)
            .build();

    public Validator<AddressesDTO> getAddressesDTOValidator() {
        return addressesDTOValidator;
    }
}
