package com.persist.coretix.modal.constants;

public enum GeneralConstants {
    SUCCESSFUL("Successful"),
    FAILED("Failed"),
    ENTRY_ALREADY_EXISTS("Entry already exists"),
    ENTRY_NOT_EXISTS("Entry not found"),
    ENTRY_IN_USE("Entry in use"),
    PARENT_NOT_EXISTS("Parent not found"),
    SENDING_EMAIL_FAILED("Email sending failed"),
    PARTIALLY_CREATED("Partially Created"),
    OWN_ENTRY_FAILED("Own Entry Failed"),
    NO_ACCESS("No Access"),
    ENTRY_INVALID("Entry Invalid"),
    NO_CHANGES_FOUND("No Changes Found");

    private String constantName;

    private GeneralConstants(String constantName) {
        this.constantName = constantName;
    }

    public short shortValue() {
        return (short)this.ordinal();
    }

    public String getName() {
        return this.constantName;
    }
}

