package com.example.springsocial.payload;

import javax.validation.constraints.NotBlank;

public class AddUserToOrganizationRequest {
    @NotBlank
    private long userID;
    @NotBlank
    private String organizationID;

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public void setOrganizationID(String organizationID) {
        this.organizationID = organizationID;
    }
}
