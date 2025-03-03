# Keycloak Authentication Projects

This documentation provides setup and implementation details for **two projects** that use **Keycloak for authentication and role-based access control**.

## **Prerequisites**
- Docker installed on your machine.
- Java 17+
- Spring Boot

## **Step 1: Install and Run Keycloak using Docker**
Run the following command to start Keycloak in **developer mode**:
```sh
docker run -d -p 8072:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:24.0.3 start-dev
```

### **Run Keycloak Database**
```sh
docker run -d --name keycloakdb -e POSTGRES_PASSWORD=root123 -p 5432:5432 -e POSTGRES_DB=keycloakdb postgres
```

## **Step 2: Configure Keycloak**
1. **Access Keycloak Admin Panel**: `http://localhost:8072`
2. Log in with **admin/admin** (default credentials).
3. **Create a Realm** (e.g., `myrealm`).
4. **Create a Client** (e.g., `myapp-client`).
   - Set **Access Type**: `confidential` or `public`
   - Enable `Direct Access Grants`.
5. **Create Roles** (`ADMIN`, `USER` etc.).
6. **Create Users** and Assign Roles.

---

## **Project 1: Role Management API**
This project provides **API endpoints** to manage Keycloak roles and users.

### **Features**
- Create, Update, Delete roles.
- Add, Update, Delete users.
- Assign roles to users.
- Authenticate users and retrieve an access token.

### **Endpoints**
| HTTP Method | Endpoint | Description |
|------------|---------|-------------|
| `POST` | `/roles` | Create a new role |
| `PUT` | `/roles/{roleName}` | Update a role |
| `DELETE` | `/roles/{roleName}` | Delete a role |
| `POST` | `/users` | Create a new user |
| `PUT` | `/users/{userId}` | Update user details |
| `DELETE` | `/users/{userId}` | Delete a user |
| `POST` | `/users/{userId}/assign-role/{roleName}` | Assign a role to user |
| `POST` | `/auth/login` | Authenticate user and get token |


### **Authentication & Token Retrieval**
To authenticate a user and get a token:
```sh
curl -X POST "http://localhost:8072/realms/myrealm/protocol/openid-connect/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "client_id=myapp-client" \
     -d "username=myuser" \
     -d "password=mypassword" \
     -d "grant_type=password"
```

---

## **Project 2: Role-Based Access with Token Verification**
This project **verifies tokens** and performs **role-based authentication**.

### **Features**
- Authenticate users using Keycloak tokens.
- Restrict access to APIs based on roles.

### **Spring Boot Security Configuration**
Modify `application.properties`:
```properties
keycloak.auth-server-url=http://localhost:8072
keycloak.realm=myrealm
keycloak.resource=myapp-client
keycloak.credentials.secret=<your-client-secret>
keycloak.use-resource-role-mappings=true
```

### **Role-Based Endpoint Access**
```java
 public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity serverHttpSecurity) {
        serverHttpSecurity.authorizeExchange(exchanges -> exchanges.pathMatchers("/public/**").permitAll()
                        .pathMatchers("/member/**").hasRole("member")
                        .pathMatchers("/moderator/**").hasRole("moderator")
                        .pathMatchers("/admin/**").hasRole("admin"))
                .oauth2ResourceServer(oAuth2ResourceServerSpec -> oAuth2ResourceServerSpec
                        .jwt(jwtSpec -> jwtSpec.
                                jwtAuthenticationConverter(grantedAuthoritiesExtractor())
                                .jwkSetUri("http://localhost:7090/realms/<realme-name>/protocol/openid-connect/certs")
                        )

                );
        serverHttpSecurity.csrf(csrfSpec -> csrfSpec.disable());
        return serverHttpSecurity.build();
    }
```

### **Access API with Token**
```sh
curl -H "Authorization: Bearer <access_token>" http://localhost:8080/secure/admin
```

---

## **Future Enhancements**
- Integrate **refresh tokens** for session management.
- Implement **multi-factor authentication**.
- Add **frontend integration** using React or Angular.

This documentation provides a **secure authentication flow using Keycloak** with **role-based access control** in **Spring Boot**. 🚀

