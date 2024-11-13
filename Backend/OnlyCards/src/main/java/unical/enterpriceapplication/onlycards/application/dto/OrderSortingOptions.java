package unical.enterpriceapplication.onlycards.application.dto;

import lombok.Getter;

@Getter
public enum OrderSortingOptions {
    ADD_DATE("created-date", "addDate"),
    STATUS("status","status"),
    MODIFY_DATE("last-modified", "modifyDate"),
    BUYER("customer-username", "user.username"),
    SELLER("seller-email", "vendorEmail");

    private final String value;
    private final String key;

     OrderSortingOptions(String value, String key) {
        this.value = value;
        this.key = key;
    }
    public static OrderSortingOptions fromValue(String value)  {
        for (OrderSortingOptions option : OrderSortingOptions.values()) {
            if (option.value.equals(value)) {
                return option;
            }
        }
        throw new IllegalArgumentException();
    }

}
