package com.turisup.resources.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class PlaceResponse {
    String id;
    String nombre;
    String status;
    PlacePoint coordenadas;
    String descripcion;
    ArrayList<String> imagenesPaths;
    ArrayList<String > fbVideoIds;
    ArrayList<String> fbImagenesIds;
    Organization organizacion;
    Region region;
    User user;
    Double distancia;
    String fecha;
    String categoria;
    String favorito;
    Double rate;

    public PlaceResponse(String id, String nombre,String status, PlacePoint coordenadas, String descripcion, ArrayList<String> imagenesPaths, ArrayList<String> fbImagenesIds, Organization organizacion, Region region, User user, String favorito ) {
        this.id = id;
        this.nombre = nombre;
        this.status = status;
        this.coordenadas = coordenadas;
        this.descripcion = descripcion;
        this.imagenesPaths = imagenesPaths;
        this.fbImagenesIds = fbImagenesIds;
        this.organizacion = organizacion;
        this.region = region;
        this.user = user;
        this.favorito = favorito;
    }

    public PlaceResponse() {

    }
}

