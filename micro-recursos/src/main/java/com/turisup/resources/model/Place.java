package com.turisup.resources.model;

import lombok.Data;
import java.util.ArrayList;

@Data
public class Place {
    String id;
    String nombre;

    PlacePoint coordenadas;
    String descripcion;
    String categoria;
    String usuarioId;
    ArrayList<String> imagenesPaths;
    ArrayList<String> fbImagenesIds;
    ArrayList<String> fbVideoIds;
    public Place(String id,String nombre, PlacePoint coordenadas,  String descripcion, String usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.usuarioId = usuarioId;
        this.coordenadas=coordenadas;
    }

    public Place() {

    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }


}
