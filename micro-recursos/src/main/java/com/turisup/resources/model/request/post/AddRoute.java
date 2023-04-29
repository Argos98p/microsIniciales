package com.turisup.resources.model.request.post;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@Data
public class AddRoute {

    @NotNull
    String userId;
    @NotNull
    String nombre;
    @NotNull
    String descripcion;
    String rutaId;
    @NotNull
    ArrayList<String> lugares;

}
