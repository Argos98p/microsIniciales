package com.turisup.resources.model.request.get;

import lombok.Data;

@Data
public class AdminUser {
    String nombre;
    String correo;
    String nick;
    String id;

    public AdminUser(String nombre, String nick, String correoM, String userM) {
        this.nombre=nombre;
        this.nick=nick;
        this.correo=correoM;
        this.id=userM;
    }
}
