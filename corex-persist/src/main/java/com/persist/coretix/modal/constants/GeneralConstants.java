/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
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





