package com.example.ems.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Organization address details")
public class OrganizationAddressDto {
    @Schema(description = "Street address", example = "123 Business Way")
    @NotBlank(message = "Street is required")
    private String street;

    @Schema(description = "City", example = "San Francisco")
    @NotBlank(message = "City is required")
    private String city;

    @Schema(description = "State or Province", example = "CA")
    @NotBlank(message = "State is required")
    private String state;

    @Schema(description = "Country", example = "United States")
    @NotBlank(message = "Country is required")
    private String country;

    @Schema(description = "Zip or Postal Code", example = "94105")
    @NotBlank(message = "Zip code is required")
    private String zipCode;

    public OrganizationAddressDto() {}

    public OrganizationAddressDto(String street, String city, String state, String country, String zipCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipCode = zipCode;
    }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
}
