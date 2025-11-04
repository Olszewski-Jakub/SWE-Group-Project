package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AttributeDtoTest {

    @Test
    void gettersSetters_work_and_validation_passes_when_nonBlank() {
        AttributeDto dto = new AttributeDto();
        dto.setName("color");
        dto.setValue("red");

        assertEquals("color", dto.getName());
        assertEquals("red", dto.getValue());

        try (ValidatorFactory vf = Validation.buildDefaultValidatorFactory()) {
            Validator validator = vf.getValidator();
            Set<ConstraintViolation<AttributeDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty());
        }
    }

    @Test
    void validation_fails_when_blank_or_null() {
        AttributeDto blank = new AttributeDto(" ", " ");
        AttributeDto missing = new AttributeDto();

        try (ValidatorFactory vf = Validation.buildDefaultValidatorFactory()) {
            Validator validator = vf.getValidator();
            assertFalse(validator.validate(blank).isEmpty());
            assertFalse(validator.validate(missing).isEmpty());
        }
    }
}

