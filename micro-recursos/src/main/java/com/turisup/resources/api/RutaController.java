package com.turisup.resources.api;

import com.complexible.stardog.plan.filter.functions.rdfterm.Str;
import com.google.api.Http;
import com.turisup.resources.model.PlaceResponse;
import com.turisup.resources.service.PlaceService;
import com.turisup.resources.service.RutaService;
import org.json.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ruta")
public class RutaController {

    @Autowired
    RutaService rutaService;

    @GetMapping("")
    public ResponseEntity <ArrayList<Map<String,String>>> getRutasByUser (@RequestParam("userId") String userId){
        ArrayList<Map<String,String>> rutasDelUsuario = rutaService.getRutasUser(userId);

        return  ResponseEntity.ok().body(rutasDelUsuario);
    }

    @GetMapping("/id")
    public  ResponseEntity<?> getRoute(@RequestParam("rutaId") String rutaId, @RequestParam("userId") String userId){
        Map<String,Object> ruta = rutaService.getOneRuta(rutaId, userId);
        if(ruta.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ruta no encontrada");
        }
        return ResponseEntity.ok().body(ruta);

    }

    @PostMapping("/agregarLugares")
    public ResponseEntity<?> addPlaceToRoute(@RequestBody Map<String,Object> body){
        if(body.get("rutaId")==null){
            return ResponseEntity.badRequest().body("La ruta es obligatoria");
        } else if (body.get("lugares")==null) {
            return ResponseEntity.badRequest().body("Los lugares son obligatorios");
        } else if (body.get("userId")==null) {
            return ResponseEntity.badRequest().body("Los lugares son obligatorios");
        }
        Map<String,Object> ruta  = rutaService.agregarLugar((String) body.get("rutaId"), (ArrayList<String>) body.get("lugares"),(String) body.get("userId"));
        if(ruta.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ruta no encontrada");
        }
        return ResponseEntity.ok().body(ruta);
    }

    @PostMapping("/eliminarLugar")
    public ResponseEntity<?> removePlaceToRoute(@RequestParam("rutaId") String rutaId, @RequestParam("lugarId")String lugarId,  @RequestParam("userId")String userId){
        Map<String,Object> ruta  = rutaService.eliminarLugar(rutaId,lugarId, userId);
        if(ruta.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ruta no encontrada");
        }
        return ResponseEntity.ok().body(ruta);
    }

    @PostMapping("/eliminarRuta")
    public ResponseEntity<?> removeRoute(@RequestParam("rutaId")String rutaId, @RequestParam("userId") String userId){
        ArrayList<Map<String,String>> rutasDelUsuario = rutaService.removeRuta(userId,rutaId);

        return  ResponseEntity.ok().body(rutasDelUsuario);
    }
}
