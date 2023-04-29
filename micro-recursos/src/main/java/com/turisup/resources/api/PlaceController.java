package com.turisup.resources.api;

import com.complexible.common.base.Bool;
import com.google.gson.Gson;
import com.turisup.resources.model.Place;
import com.turisup.resources.model.PlaceResponse;
import com.turisup.resources.model.parser.QueryOptions;
import com.turisup.resources.model.request.get.InteractionModel;
import com.turisup.resources.model.request.post.AddRoute;
import com.turisup.resources.model.request.post.PlaceRequest;
import com.turisup.resources.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/recurso")
public class PlaceController {

    @Autowired
    PlaceService placeService;

    @RequestMapping(path = "/nuevo", method = POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity <?> newPlace(@RequestParam("recurso") String jsonString, @RequestParam("files")MultipartFile[] files) throws IOException {
        Gson g = new Gson();
        PlaceRequest placeRequest = g.fromJson(jsonString, PlaceRequest.class);
        Place myPlace =  placeService.save(placeRequest,files);
        return ResponseEntity.ok().body(myPlace);
    }

    @PostMapping("/nuevaRuta")
    public ResponseEntity<?> newRoute (@RequestBody (required=true) AddRoute nuevaRutaInfo){
        String newRouteId = placeService.addRoute(nuevaRutaInfo);
        if(newRouteId != null){
            return ResponseEntity.ok().body(newRouteId);
        }
        return ResponseEntity.badRequest().body("error");
    }



    @GetMapping()
    public ResponseEntity<PlaceResponse> getPlace (@RequestParam("recursoId") String placeId){
       PlaceResponse place= placeService.get(placeId);
       return  ResponseEntity.ok().body(place);
    }

    @GetMapping("/historial")
    public ResponseEntity getInteractionsRequest (@RequestParam("fechaInicio") String fechaInicioParam, @RequestParam("fechaFin") String fechaFinParam,@RequestParam("userId") String userId){


        System.out.println(fechaInicioParam);
        System.out.println(fechaFinParam);

        ArrayList<InteractionModel> interactions = placeService.getInteractions( fechaInicioParam,  fechaFinParam, userId);

        return  ResponseEntity.ok().body(interactions);
    }

    @GetMapping("/todos")
    public ResponseEntity<?> allPlaces(
            @RequestParam(name="organizacionId") Optional<String> organizacionId,
            @RequestParam(name="regionId") Optional<String>  regionId,
            @RequestParam(name="creadorId") Optional<String>  creadorId,
            @RequestParam(name="lugarId") Optional<String>  lugarId,
            @RequestParam(name="estadoLugar") Optional<String>  estadoLugar,
            @RequestParam(name="latitud") Optional<String> latitud,
            @RequestParam(name="longitud") Optional<String> longitud,
            @RequestParam(name="distancia") Optional<String> distancia,
            @RequestParam(name="buscar") Optional<String> buscar,
            @RequestParam(name="userId") Optional<String> userId
    ){


        QueryOptions queryOptions = new QueryOptions();


        if((latitud.isPresent() && !longitud.isPresent()) || (!latitud.isPresent() && longitud.isPresent())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Se encontro longitud pero no latitud o viceversa");
        }
        if(longitud.isPresent() && latitud.isPresent()){
            double lat = Double.parseDouble(latitud.get());
            double longi= Double.parseDouble(longitud.get());
            if(lat<90 && lat >-90){
                queryOptions.setLatitud(lat);
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Latitud no valida");
            }

            if(longi<180 && longi>-180){
                queryOptions.setLongitud(longi);
            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Longitud no valida");
            }
        }
        if( distancia.isPresent() ){
            if(Double.parseDouble(distancia.get())<0){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Distancia invalida");
            }
        }

        if (organizacionId.isPresent()) {
            queryOptions.setOrganizacionId(organizacionId.get());
        } else {
            queryOptions.setOrganizacionId(null);
        }
        if (regionId.isPresent()) {
            queryOptions.setRegionId(regionId.get());
        } else {
            queryOptions.setRegionId(null);
        }
        if (creadorId.isPresent()) {
            queryOptions.setCreadorId(creadorId.get());
        } else {
            queryOptions.setCreadorId(null);
        }
        if (lugarId.isPresent()) {
            queryOptions.setLugarId(lugarId.get());
        } else {
            queryOptions.setLugarId(null);
        }if (estadoLugar.isPresent()) {
            queryOptions.setEstadoLugar(estadoLugar.get());
        } else {
            queryOptions.setEstadoLugar(null);
        }if(buscar.isPresent()){
            queryOptions.setBuscar(buscar.get());
        }else{
            queryOptions.setBuscar(null);
        }if(userId.isPresent()){
            queryOptions.setUserId(userId.get());
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Id del usuario es obligatorio");
        }
        distancia.ifPresent(s -> queryOptions.setDistanciaMax(Double.parseDouble(s)));


        List<PlaceResponse> places = placeService.all( queryOptions);
        //
        return ResponseEntity.ok().body(places);
    }




}
