package com.example.springsocial.controller;

import com.example.springsocial.exception.ResourceNotFoundException;
import com.example.springsocial.model.User;
import com.example.springsocial.payload.AddUserToOrganizationRequest;
import com.example.springsocial.payload.UpdateRequest;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.CurrentUser;
import com.example.springsocial.security.UserPrincipal;
import com.example.springsocial.triplestore.QueryTemplates;
import com.example.springsocial.triplestore.Triplestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Triplestore triplestore;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @PostMapping("/users/update")
    @PreAuthorize("permitAll()")
    public ResponseEntity updateRole(@RequestBody UpdateRequest updateRequest){
        Optional<User> findUser = userRepository.findByEmail(updateRequest.getEmail());
        if(findUser.isPresent()){
            User user = findUser.get();
            String oldRole = user.getRole();
            user.setRole(updateRequest.getRole());
            userRepository.save(user);

            ResponseEntity<?> response = triplestore.PostToTriplestore(QueryTemplates.updateRoleUser(updateRequest.getRole(),oldRole,user.getId().toString()));
            if(response.getStatusCodeValue()==200){
                return new ResponseEntity(HttpStatus.OK);
            }else{
                user.setRole(oldRole);
                userRepository.save(user);
                return new ResponseEntity(HttpStatus.CONFLICT);
            }

        }else{
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/users/all")
    @PreAuthorize("permitAll()")
    public ArrayList<User> allUsers (){

        return (ArrayList<User>) userRepository.findAll();

    }
    @GetMapping("/users/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<User> searchUser(@RequestParam String correo){
        System.out.println(correo);
        Optional<User> findUser = userRepository.findByEmail(correo);
        if(findUser.isPresent()){
            User user = findUser.get();
            return ResponseEntity.ok(user);
        }
        return  ResponseEntity.notFound().build();
    }

    @PostMapping("/user/matchOrganization")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> machOrganization (@RequestBody AddUserToOrganizationRequest request){

        Optional<User> findUser = userRepository.findById(request.getUserID());
        if(findUser.isPresent()){
            User user = findUser.get();
            if(user.getRole().equalsIgnoreCase("admin")){
                String query =  QueryTemplates.AddUserInOrganization(user.getId().toString(), request.getOrganizationID());
                System.out.println(query);
                ResponseEntity<?> response = triplestore.PostToTriplestore(query);
                if(response.getStatusCodeValue()==200){
                    return new ResponseEntity(HttpStatus.OK);
                }else{
                    return new ResponseEntity(HttpStatus.CONFLICT);
                }
            }else{
                return new ResponseEntity(HttpStatus.UNAUTHORIZED);
            }
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }
}
