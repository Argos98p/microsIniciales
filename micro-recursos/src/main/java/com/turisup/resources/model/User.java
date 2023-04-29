package com.turisup.resources.model;

import lombok.Data;

@Data
public class User {
    String id;
    String nombre;
    String image;

    public User(String id, String nombre, String image) {
        this.id = id;
        this.nombre = nombre;
        this.image = image;
    }
}
