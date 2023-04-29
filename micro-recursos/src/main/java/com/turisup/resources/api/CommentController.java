package com.turisup.resources.api;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.jena.SDJenaFactory;
import com.google.gson.Gson;
import com.turisup.resources.model.Place;
import com.turisup.resources.model.request.post.AddComment;
import com.turisup.resources.model.request.post.PlaceRequest;
import com.turisup.resources.repository.DBConnection;
import com.turisup.resources.service.CommentService;
import graphql.language.Comment;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/api/comentario")
public class CommentController {

    @Autowired
    CommentService commentService;

    @RequestMapping(path = "/nuevo", method = POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> newCommment(@RequestParam("comentario") String jsonString, @RequestParam(value = "files",required = false) MultipartFile[] files) throws IOException {
        HashMap<String,Object> comment= new HashMap<>();
        Gson g = new Gson();
        AddComment newComment = g.fromJson(jsonString, AddComment.class);
        if(newComment.getComentario() == null || newComment.getUserId()==null ||newComment.getLugarId() == null || newComment.getPuntaje() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan datos");
        }
        comment = commentService.save(newComment,files);

        if(comment.containsKey("error")){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(comment);
        }

        return ResponseEntity.ok().body(comment);

    }

    @GetMapping()
    public ResponseEntity<?> getCommentsByPlace(@RequestParam String lugarId){
        ArrayList<Map<String,Object>> comentarios = new ArrayList<>();
        comentarios=commentService.getByPlaces(lugarId);
        if(comentarios.size()==1){
            if (comentarios.get(0).containsKey("error")){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontro el lugar");
            }
        }
        System.out.println(comentarios);
        return ResponseEntity.status(HttpStatus.OK).body(comentarios);
    }

    @PostMapping("/borrar")
    public ResponseEntity<?> delete(@RequestParam  String comentarioId){
        boolean resultado = commentService.delteComent(comentarioId);
        if(!resultado){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("El comentario no existe");
        }
        else {
            return ResponseEntity.status(HttpStatus.OK).body("Comentario eliminado");
        }

    }

    @GetMapping("/update_comentarios")
    public ResponseEntity<?> updateImageURls(){

        Boolean response = true;
        Boolean result = commentService.updateImages();

        if(response){
            return ResponseEntity.ok("");
        }else{
            return ResponseEntity.status(500).body("");
        }
    }



}
