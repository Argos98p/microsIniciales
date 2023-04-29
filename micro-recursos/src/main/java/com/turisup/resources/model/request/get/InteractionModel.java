package com.turisup.resources.model.request.get;

import com.turisup.resources.model.PlaceResponse;
import lombok.Data;

import java.time.ZonedDateTime;


@Data
public class InteractionModel {
    ZonedDateTime date ;
    int rating;
    String contenido;
    String comentarioId;
    String placeId;
    PlaceResponse place;


    public InteractionModel(ZonedDateTime date, int rating, String contenido, String comentarioId, String placeId) {
        this.date = date;
        this.rating = rating;
        this.contenido = contenido;
        this.comentarioId = comentarioId;
        this.placeId = placeId;
    }

}
