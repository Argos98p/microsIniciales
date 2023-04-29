package com.turisup.resources.model;

import lombok.Data;

@Data
public class Region {
    String id;
    String nombre;

    public Region(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
