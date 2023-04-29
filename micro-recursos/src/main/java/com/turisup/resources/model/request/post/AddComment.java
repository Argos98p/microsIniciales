package com.turisup.resources.model.request.post;

import lombok.Data;

import javax.validation.constraints.NotNull;
@Data
public class AddComment {
    @NotNull
    String lugarId;
    @NotNull
    String userId;
    @NotNull
    String comentario;
    Integer puntaje;
}
