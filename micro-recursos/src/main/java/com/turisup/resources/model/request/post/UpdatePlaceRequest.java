package com.turisup.resources.model.request.post;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
public class UpdatePlaceRequest {
    String nombre;
    double latitud;
    double longitud;
    String descripcion;
    String usuarioId;
    String categoria;
    String estado;
    @NotNull
    String placeid;
    ArrayList<String> fbImagesId;
    ArrayList<String> fbVideoId;
}
