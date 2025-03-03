package com.datmt.keycloak.springbootauth.controller;


import com.datmt.keycloak.springbootauth.config.KeycloakProvider;
import com.datmt.keycloak.springbootauth.http.requests.CreateUserRequest;
import com.datmt.keycloak.springbootauth.http.requests.LoginRequest;
import com.datmt.keycloak.springbootauth.service.KeycloakAdminClientService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.List;

@RestController
@RequestMapping("/public")
public class UserController {
    private final KeycloakAdminClientService kcAdminClient;

    private final KeycloakProvider kcProvider;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UserController.class);


    public UserController(KeycloakAdminClientService kcAdminClient, KeycloakProvider kcProvider) {
        this.kcProvider = kcProvider;
        this.kcAdminClient = kcAdminClient;
    }
	

    @PostMapping(value = "/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest user) {
        Response createdResponse = kcAdminClient.createKeycloakUser(user);
        return ResponseEntity.status(createdResponse.getStatus()).build();

    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@NotNull @RequestBody LoginRequest loginRequest) {
        Keycloak keycloak = kcProvider.newKeycloakBuilderWithPasswordCredentials(loginRequest.getUsername(), loginRequest.getPassword()).build();

        AccessTokenResponse accessTokenResponse = null;
        try {
            accessTokenResponse = keycloak.tokenManager().getAccessToken();
            return ResponseEntity.status(HttpStatus.OK).body(accessTokenResponse);
        } catch (BadRequestException ex) {
            LOG.warn("invalid account. User probably hasn't verified email.", ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(accessTokenResponse);
        }

    }

    @GetMapping("/users")
    public  List<CreateUserRequest> getUsers(){
        return kcAdminClient.getUser();
    }
    @GetMapping("/user/{id}")
    public  CreateUserRequest getUser(@PathVariable("id") String id){
        return kcAdminClient.getUserById(id);
    }

    @PutMapping("/user-update")
    public ResponseEntity<?> updateUser(@NotNull @RequestBody CreateUserRequest user){
        boolean b = kcAdminClient.updateUser(user);
        if (b){
            return ResponseEntity.status(HttpStatus.CREATED).body("User Successfully Updated");

        }
        else {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("Something Went Wrong!");
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") String id){
        boolean b = kcAdminClient.deleteUser(id);
        if (b){
            return ResponseEntity.status(HttpStatus.CREATED).body("User Successfully Deleted");

        }
        else {
            return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body("Something Went Wrong!");
        }
    }
	

}
