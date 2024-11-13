package unical.enterpriceapplication.onlycards.application.data.entities.orders;

import lombok.Getter;

@Getter
public enum Status {
    PENDING("pending", "In elaborazione"),
    SHIPPED("shipped", "Spedito"),
    DELIVERED("delivered", "Consegnato"),
    CANCELLED("cancelled", "Cancellato");

    private final String code;
    private final String description;

    Status(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Status fromCode(String code) {
        for (Status status : Status.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    public static Status fromDescription(String description) {
        for (Status status : Status.values()) {
            if (status.description.equals(description)) {
                return status;
            }
        }
        return null;
    }
}
