package com.turisup.resources.model.request.post;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PlaceRequest {
    @NotNull
    String nombre;
    @NotNull
    double latitud;
    @NotNull
    double longitud;
    @NotNull
    String descripcion;
    @NotNull
    String usuarioId;
    @NotNull
    String categoria;
    String estado;
    String placeid;
}
