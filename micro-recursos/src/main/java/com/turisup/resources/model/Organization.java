package com.turisup.resources.model;

import lombok.Data;

@Data
public class Organization {
    String id;
    String nombre;

    public Organization(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }
}
