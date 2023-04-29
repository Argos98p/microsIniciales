package com.turisup.resources.api;

import com.google.api.Http;
import com.turisup.resources.model.admin.AdminUserResource;
import com.turisup.resources.model.request.get.AdminUser;
import com.turisup.resources.model.request.post.PlaceRequest;
import com.turisup.resources.model.request.post.UpdatePlaceRequest;
import com.turisup.resources.service.AdminService;
import com.turisup.resources.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    PlaceService placeService;

    @Autowired
    AdminService adminService;


    @PostMapping("actualizar")
    public ResponseEntity updatePlace(@RequestBody PlaceRequest placeUpdateInfo) {
        System.out.println(placeUpdateInfo);
        if(placeUpdateInfo.getEstado()==null || placeUpdateInfo.getPlaceid()==null){

            return ResponseEntity.badRequest().body("estado or placeId is null");
        }
        return adminService.updatePlace(placeUpdateInfo);
    }

    @PostMapping("actualizar-recurso")
    public ResponseEntity updatePlace2(@RequestBody UpdatePlaceRequest placeUpdateRequest,@RequestParam("files") MultipartFile[] files){
        Boolean response = adminService.updatePlacefromWeb(placeUpdateRequest,files);
        if(response){
            return ResponseEntity.ok().body("Recurso actualizado");
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/recursos")
    public ResponseEntity<List<AdminUserResource>> userAdminResources(@RequestParam String userId){
        List<AdminUserResource> resources = adminService.userAdminResources(userId);
        return ResponseEntity.ok().body(resources);
    }

    @GetMapping("/users/inOrg")
    public ResponseEntity<List<AdminUser>> getUser(@RequestParam String orgId){
        List<AdminUser> usersInOrg = adminService.getUsersInOrg(orgId);
        return ResponseEntity.ok().body(usersInOrg);
    }

    @PostMapping("actualizar-imagenes")
    public ResponseEntity<?> actualizarImagenesFb(){

        return adminService.actualizarImagenesFB();
    }


    /*
    @GetMapping("/todos")
    public ResponseEntity<List<Place>> allPlacesByRegion(@RequestBody AdminPlaceRequest adminPlaceRequest){
        List<Place> places = placeService.all();
        return ResponseEntity.ok().body(places);
    }*/
}
