package ie.universityofgalway.groupnine.delivery.rest.product.dto;

import jakarta.validation.constraints.NotBlank;

public class AttributeDto {
    @NotBlank private String name;
    @NotBlank private String value;
    public AttributeDto() {}
    public AttributeDto(String name, String value) { this.name = name; this.value = value; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
