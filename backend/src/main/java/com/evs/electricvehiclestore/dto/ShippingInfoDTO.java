package com.evs.electricvehiclestore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * @author Uzma Alam
 */
public class ShippingInfoDTO {

    @NotBlank(message = "Street address is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Province is required")
    private String province;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "Postal/zip code is required")
    @Pattern(regexp = "^[A-Za-z0-9 -]{3,10}$", message = "Zip/postal code format is invalid")
    private String zip;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+()\\- ]{7,20}$", message = "Phone number format is invalid")
    private String phone;

    public ShippingInfoDTO() {
    }

    public String getStreet() { return street; }
    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getCountry() { return country; }
    public String getZip() { return zip; }
    public String getPhone() { return phone; }

    public void setStreet(String street) { this.street = street; }
    public void setCity(String city) { this.city = city; }
    public void setProvince(String province) { this.province = province; }
    public void setCountry(String country) { this.country = country; }
    public void setZip(String zip) { this.zip = zip; }
    public void setPhone(String phone) { this.phone = phone; }
}