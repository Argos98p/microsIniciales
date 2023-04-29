package com.turisup.resources.model.request.get;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AdminPlaceRequest {
    @NotNull
    String userId;
    @NotNull
    String regionId;
    @NotNull
    String organizationId;
    @NotNull
    String estado;
}
