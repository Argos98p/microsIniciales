package com.turisup.resources.api;

import com.turisup.resources.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorito")
public class FavoriteController {
    @Autowired
    FavoriteService favoriteService;

    @PostMapping("")
    public ResponseEntity<?> insert(@RequestBody Map<String,String> body){
        if(body.get("userId")==null){
            return ResponseEntity.badRequest().body("El usuario es obligatorio");
        }
        if(body.get("placeId")==null){
            return ResponseEntity.badRequest().body("El lugar es obligatorio");
        }
        HashMap<String,Object> mapPlaces= new HashMap<>();
        mapPlaces = favoriteService.insertFavorite(body.get("userId"),body.get("placeId"));
        if(mapPlaces.containsKey("error")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapPlaces.get("error"));
        }
        return ResponseEntity.ok().body("");
    }

    @GetMapping("")
    public ResponseEntity<?> get(@RequestParam String userId){

        HashMap<String,Object> mapPlaces= new HashMap<>();
        mapPlaces = favoriteService.getFavorites(userId);
        if(mapPlaces.containsKey("error")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapPlaces.get("error"));
        }
        return ResponseEntity.ok().body(mapPlaces.get("places"));
    }

    @PostMapping("/eliminar")
    public ResponseEntity<?> delete(@RequestBody Map<String,String> body){
        if(body.get("userId")==null){
            return ResponseEntity.badRequest().body("El usuario es obligatorio");
        }
        if(body.get("placeId")==null){
            return ResponseEntity.badRequest().body("El lugar es obligatorio");
        }
        HashMap<String,String> mapPlaces= new HashMap<>();
        mapPlaces = favoriteService.removeFavorite(body.get("userId"),body.get("placeId"));
        if(mapPlaces.containsKey("error")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapPlaces.get("error"));
        }
        return ResponseEntity.ok().body("");
    }
}
