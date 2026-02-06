# 🔐 OAuth2 & Keycloak Learning Project

A comprehensive Spring Boot microservices project demonstrating **four different OAuth2 authentication flows** with Keycloak integration. This repository is designed as a hands-on learning resource for understanding OAuth2, OpenID Connect (OIDC), and Keycloak.

---

## 📚 Table of Contents

1. [Keycloak Fundamentals](#-keycloak-fundamentals)
2. [OAuth2 & OIDC Concepts](#-oauth2--oidc-concepts)
3. [Authentication Flows in This Project](#-authentication-flows-in-this-project)
4. [Project Architecture](#-project-architecture)
5. [Directory Structure](#-directory-structure)
6. [How to Run](#-how-to-run)
7. [Common Errors & Fixes](#-common-errors--fixes)
8. [Learning Path](#-learning-path)

---

## 🎯 Keycloak Fundamentals

### What is Keycloak?

**Keycloak** is an **open-source Identity and Access Management (IAM)** solution. It provides:
- **Single Sign-On (SSO)** across multiple applications
- **User authentication** (login/logout)
- **Authorization** (role-based access control)
- **Token management** (JWT generation, validation, refresh)
- **Social login** (Google, Facebook, GitHub, etc.)
- **Multi-factor authentication (MFA)**

Think of Keycloak as a **centralized security server** that handles all authentication logic, so your applications don't have to.

---

### Core Keycloak Concepts

#### 1. **Realm**
A **Realm** is an **isolated security domain** in Keycloak.

- Each realm manages its own:
  - Users
  - Clients (applications)
  - Roles
  - Sessions
  - Security policies

**Example:** You might have:
- `oauth2-client-flow` realm for production
- `oauth2_learn` realm for development
- `test-realm` for testing

**Analogy:** Think of a realm as a **separate company** in a multi-tenant system. Each company has its own users and apps.

---

#### 2. **Client**
A **Client** represents an **application** that wants to authenticate users or access protected resources.

**Types of Clients:**

| Client Type | Description | Example |
|------------|-------------|---------|
| **Public Client** | Cannot keep secrets (e.g., browser apps, mobile apps) | React SPA, Angular app |
| **Confidential Client** | Can securely store secrets (e.g., backend services) | Spring Boot app, Node.js server |
| **Bearer-only Client** | Only validates tokens, doesn't initiate login | Resource Server (API) |

**In this project:**
- `client-flow` → Confidential client (Spring Boot using client credentials)
- `oauth2-flow` → Confidential client (Spring Boot using authorization code)
- React PKCE app → Public client (browser-based)

---

#### 3. **User**
A **User** is an entity that can **authenticate** (log in) to Keycloak.

Users have:
- **Username** and **password**
- **Email**, **first name**, **last name**
- **Attributes** (custom metadata)
- **Roles** (permissions)

---

#### 4. **Role**
A **Role** defines **permissions** or **access levels**.

**Types:**
- **Realm Roles:** Global to the entire realm (e.g., `admin`, `user`)
- **Client Roles:** Specific to a client (e.g., `order-manager` for an e-commerce app)

**Example:**
```
User: john@example.com
Roles: [user, premium-member]
```

---

#### 5. **Scope**
A **Scope** defines **what data or actions** a client can access.

**Common Scopes:**

| Scope | Description |
|-------|-------------|
| `openid` | Enables OpenID Connect (returns ID token) |
| `profile` | Access to user's profile (name, username) |
| `email` | Access to user's email |
| `roles` | Access to user's roles |
| `offline_access` | Enables refresh tokens |

**Example Request:**
```
scope: openid profile email roles
```

---

## 🔑 OAuth2 & OIDC Concepts

### Tokens Explained

#### 1. **Access Token**
- **Purpose:** Grants access to protected resources (APIs)
- **Format:** JWT (JSON Web Token)
- **Lifetime:** Short (typically 5-15 minutes)
- **Contains:** User ID, roles, scopes, expiration time

**Example Use:**
```http
GET /api/data
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

#### 2. **Refresh Token**
- **Purpose:** Obtain a new access token without re-authenticating
- **Format:** Opaque string or JWT
- **Lifetime:** Long (hours, days, or weeks)
- **Security:** Must be stored securely (never in localStorage for web apps)

**Flow:**
```
Access Token Expired → Send Refresh Token → Get New Access Token
```

---

#### 3. **ID Token** (OIDC only)
- **Purpose:** Proves user identity (who the user is)
- **Format:** JWT
- **Contains:** User info (email, name, username)
- **Use Case:** Display user profile, personalization

**Key Difference:**
- **Access Token** → "What can I do?" (authorization)
- **ID Token** → "Who am I?" (authentication)

---

### JWT Structure

A JWT has **three parts** separated by dots (`.`):

```
header.payload.signature
```

**Example:**
```
eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**Decoded:**
```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",
    "iss": "http://localhost:8089/realms/oauth2-client-flow",
    "exp": 1707234567,
    "iat": 1707233667,
    "scope": "openid profile email"
  },
  "signature": "..."
}
```

---

### JWKS (JSON Web Key Set)

**JWKS** is a set of **public keys** used to **verify JWT signatures**.

**Endpoint:**
```
http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/certs
```

**How it works:**
1. Keycloak signs JWTs with its **private key**
2. Resource servers fetch the **public key** from JWKS endpoint
3. Resource servers verify JWT signature using the public key

---

### Issuer

The **Issuer** (`iss`) is the **URL of the authorization server** that issued the token.

**Example:**
```
iss: http://localhost:8089/realms/oauth2-client-flow
```

**Why it matters:**
- Resource servers **validate** that tokens come from a trusted issuer
- Prevents token forgery from rogue servers

---

### Well-Known Endpoint

The **well-known endpoint** provides **metadata** about the OAuth2/OIDC server.

**Endpoint:**
```
http://localhost:8089/realms/oauth2-client-flow/.well-known/openid-configuration
```

**Returns:**
```json
{
  "issuer": "http://localhost:8089/realms/oauth2-client-flow",
  "authorization_endpoint": "http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/auth",
  "token_endpoint": "http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/token",
  "jwks_uri": "http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/certs",
  "userinfo_endpoint": "http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/userinfo",
  "end_session_endpoint": "http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/logout"
}
```

**Use Case:** Spring Boot auto-configures OAuth2 using this endpoint via `issuer-uri`.

---

## 🔄 Authentication Flows in This Project

This project demonstrates **4 different OAuth2 flows**:

### 1. **Client Credentials Flow** (Machine-to-Machine)

**Location:** `client_credntial_flow/`

**Use Case:** Backend service calling another backend service (no user involved)

**Flow Diagram:**
```
┌─────────────┐                          ┌─────────────┐
│   Client    │                          │  Keycloak   │
│  (Port:8081)│                          │ (Port:8089) │
└──────┬──────┘                          └──────┬──────┘
       │                                        │
       │  1. POST /token                        │
       │     client_id + client_secret          │
       │───────────────────────────────────────>│
       │                                        │
       │  2. Access Token (JWT)                 │
       │<───────────────────────────────────────│
       │                                        │
       │                                        │
┌──────▼──────┐                          ┌─────────────┐
│   Client    │                          │  Resource   │
│             │  3. GET /data            │   Server    │
│             │     Bearer <token>       │ (Port:8082) │
│             │─────────────────────────>│             │
│             │                          │             │
│             │  4. Validate JWT         │             │
│             │     (using JWKS)         │             │
│             │                          │             │
│             │  5. "Hello From Resource │             │
│             │      Server"             │             │
│             │<─────────────────────────│             │
└─────────────┘                          └─────────────┘
```

**Key Points:**
- **No user login** required
- Client authenticates with `client_id` and `client_secret`
- Used for **service-to-service** communication

---

### 2. **Authorization Code Flow** (User Login)

**Location:** `oauth2flow/`

**Use Case:** Web application where users log in via Keycloak

**Flow Diagram:**
```
┌─────────────┐                          ┌─────────────┐
│   Browser   │                          │  Spring App │
│             │                          │ (Port:8080) │
└──────┬──────┘                          └──────┬──────┘
       │                                        │
       │  1. GET /                              │
       │───────────────────────────────────────>│
       │                                        │
       │  2. Redirect to Keycloak Login         │
       │<───────────────────────────────────────│
       │                                        │
┌──────▼──────┐                          ┌─────────────┐
│   Browser   │                          │  Keycloak   │
│             │  3. GET /auth?           │ (Port:8089) │
│             │     client_id=...        │             │
│             │     redirect_uri=...     │             │
│             │─────────────────────────>│             │
│             │                          │             │
│             │  4. Login Page (HTML)    │             │
│             │<─────────────────────────│             │
│             │                          │             │
│  User enters credentials               │             │
│             │                          │             │
│             │  5. POST /auth           │             │
│             │     username + password  │             │
│             │─────────────────────────>│             │
│             │                          │             │
│             │  6. Redirect with code   │             │
│             │     ?code=ABC123         │             │
│             │<─────────────────────────│             │
       │                                        │
┌──────▼──────┐                          ┌─────────────┐
│   Browser   │                          │  Spring App │
│             │  7. GET /login/oauth2/   │ (Port:8080) │
│             │     code/keycloak        │             │
│             │     ?code=ABC123         │             │
│             │─────────────────────────>│             │
       │                                 │             │
       │                          ┌──────▼──────┐      │
       │                          │  Spring App │      │
       │                          │             │      │
       │                          │  8. POST /token    │
       │                          │     code=ABC123    │
       │                          │     client_secret  │
       │                          │────────────────────>│
       │                          │                Keycloak
       │                          │  9. Access Token + │
       │                          │     ID Token       │
       │                          │<────────────────────│
       │                          └─────────────┘      │
       │                                 │             │
       │  10. Show User Profile          │             │
       │<─────────────────────────────────             │
       │                                               │
└─────────────┘                          └─────────────┘
```

**Key Points:**
- **User logs in** via Keycloak UI
- App receives **authorization code**
- App exchanges code for **access token + ID token**
- **Most secure** for web apps with backend

---

### 3. **Authorization Code + PKCE** (Single Page App)

**Location:** `oauth2_pkce/`

**Use Case:** React/Angular/Vue apps (public clients that can't store secrets)

**Flow Diagram:**
```
┌─────────────┐                          ┌─────────────┐
│   Browser   │                          │  React App  │
│             │                          │ (Port:5173) │
└──────┬──────┘                          └──────┬──────┘
       │                                        │
       │  1. Click "Login"                      │
       │───────────────────────────────────────>│
       │                                        │
       │  2. Generate code_verifier (random)    │
       │     Generate code_challenge            │
       │     (SHA256 hash of verifier)          │
       │                                        │
┌──────▼──────┐                          ┌─────────────┐
│   Browser   │                          │  Keycloak   │
│             │  3. GET /auth?           │ (Port:8089) │
│             │     client_id=...        │             │
│             │     redirect_uri=...     │             │
│             │     code_challenge=...   │             │
│             │     code_challenge_method│             │
│             │─────────────────────────>│             │
│             │                          │             │
│             │  4. Login Page           │             │
│             │<─────────────────────────│             │
│             │                          │             │
│  User enters credentials               │             │
│             │                          │             │
│             │  5. POST /auth           │             │
│             │─────────────────────────>│             │
│             │                          │             │
│             │  6. Redirect with code   │             │
│             │<─────────────────────────│             │
       │                                        │
┌──────▼──────┐                          ┌─────────────┐
│   Browser   │                          │  React App  │
│             │  7. GET /?code=ABC123    │             │
│             │─────────────────────────>│             │
       │                                 │             │
       │                          ┌──────▼──────┐      │
       │                          │  React App  │      │
       │                          │             │      │
       │                          │  8. POST /token    │
       │                          │     code=ABC123    │
       │                          │     code_verifier  │
       │                          │────────────────────>│
       │                          │                Keycloak
       │                          │  9. Verify:        │
       │                          │     SHA256(verifier│
       │                          │     ) == challenge │
       │                          │                    │
       │                          │  10. Access Token  │
       │                          │<────────────────────│
       │                          └─────────────┘      │
       │                                 │             │
       │  11. Show User Profile          │             │
       │<─────────────────────────────────             │
       │                                               │
└─────────────┘                          └─────────────┘
```

**Key Points:**
- **PKCE** (Proof Key for Code Exchange) prevents authorization code interception
- **No client secret** (public client)
- Uses `code_verifier` and `code_challenge` for security
- **Best practice** for SPAs and mobile apps

---

### 4. **Hybrid: Client + Resource Server**

**Location:** `client_as_oauth_resourceserver/`

**Use Case:** A service that:
1. **Acts as a Resource Server** (validates incoming tokens)
2. **Acts as an OAuth2 Client** (calls other services)

**Flow Diagram:**
```
┌─────────────┐                          ┌─────────────┐
│  External   │                          │  Hybrid App │
│   Client    │  1. GET /proxy           │ (Port:8081) │
│             │     Bearer <token>       │             │
│             │─────────────────────────>│             │
│             │                          │             │
│             │  2. Validate JWT         │             │
│             │     (Resource Server)    │             │
│             │                          │             │
       │                          ┌──────▼──────┐      │
       │                          │  Hybrid App │      │
       │                          │             │      │
       │                          │  3. Extract token  │
       │                          │     from Security  │
       │                          │     Context        │
       │                          │                    │
       │                          │  4. Propagate token│
       │                          │     to downstream  │
       │                          │────────────────────>│
       │                          │                Resource
       │                          │                Server
       │                          │  5. Response       │
       │                          │<────────────────────│
       │                          └─────────────┘      │
       │                                 │             │
       │  6. Return Response             │             │
       │<─────────────────────────────────             │
       │                                               │
└─────────────┘                          └─────────────┘
```

**Key Points:**
- **Dual role:** Validates incoming tokens + makes authenticated requests
- Uses **token propagation** (forwards the same token downstream)
- Common in **microservices** architectures

---

## 🏗️ Project Architecture

### Component Mapping

| Module | Role | OAuth2 Flow | Port |
|--------|------|-------------|------|
| `client_credntial_flow/client` | **OAuth2 Client** | Client Credentials | 8081 |
| `client_credntial_flow/resourceserver` | **Resource Server** | N/A (validates tokens) | 8082 |
| `oauth2flow` | **OAuth2 Client** | Authorization Code | 8080 |
| `oauth2_pkce` | **Public Client (SPA)** | Authorization Code + PKCE | 5173 |
| `client_as_oauth_resourceserver` | **Hybrid (Client + Resource Server)** | Client Credentials + Resource Server | 8081 |

---

### Token Flow Summary

#### Client Credentials Flow
```
Client → Keycloak (get token) → Resource Server (validate token)
```

#### Authorization Code Flow
```
Browser → Spring App → Keycloak (login) → Spring App (exchange code) → Display User
```

#### PKCE Flow
```
Browser → React App → Keycloak (login) → React App (exchange code + verifier) → Display User
```

#### Hybrid Flow
```
External Client → Hybrid App (validate) → Extract Token → Downstream Service
```

---

## 📁 Directory Structure

```
OAUTH2/
│
├── client_credntial_flow/          # Machine-to-Machine Authentication
│   ├── client/                     # OAuth2 Client (Spring Boot)
│   │   ├── src/main/java/com/learn/client/
│   │   │   └── ClientApplication.java
│   │   │       ├── OAuth2AuthorizedClientManager (generates tokens)
│   │   │       ├── RestTemplate (calls Resource Server)
│   │   │       └── CommandLineRunner (demo flow)
│   │   └── src/main/resources/
│   │       └── application.yaml    # client-id, client-secret, token-uri
│   │
│   └── resourceserver/             # Resource Server (Spring Boot)
│       ├── src/main/java/com/learn/resourceserver/
│       │   ├── SecurityConfig.java
│       │   │   └── oauth2ResourceServer (JWT validation)
│       │   └── DataController.java
│       │       └── GET /data (protected endpoint)
│       └── src/main/resources/
│           └── application.yaml    # issuer-uri (for JWKS)
│
├── oauth2flow/                     # User Login (Authorization Code)
│   ├── src/main/java/com/learn/oauth2flow/
│   │   ├── SecurityConfig.java
│   │   │   └── oauth2Login (enables login flow)
│   │   └── HomeController.java
│   │       ├── GET / (shows user profile)
│   │       └── POST /logout (Keycloak logout)
│   └── src/main/resources/
│       └── application.yaml        # client-id, client-secret, issuer-uri
│
├── oauth2_pkce/                    # React SPA (PKCE)
│   ├── src/
│   │   ├── authConfig.js           # Keycloak endpoints, client-id
│   │   ├── App.jsx                 # Login/Logout UI, token display
│   │   └── main.jsx                # AuthProvider wrapper
│   └── package.json                # react-oauth2-code-pkce library
│
└── client_as_oauth_resourceserver/ # Hybrid Architecture
    └── client_with_both/
        ├── src/main/java/com/learn/client/
        │   ├── config/
        │   │   ├── SecurityConfig.java
        │   │   │   └── oauth2ResourceServer (validates incoming tokens)
        │   │   └── AuthConfig.java
        │   │       └── OAuth2AuthorizedClientManager (for outgoing calls)
        │   ├── controller/
        │   │   └── ProxyController.java
        │   │       └── GET /proxy (protected endpoint)
        │   └── service/
        │       └── Service2Client.java
        │           └── fetchData() (propagates token to downstream service)
        └── src/main/resources/
            └── application.yaml    # Both resourceserver + client config
```

---

### Key Classes & Their Roles

#### 1. **SecurityConfig.java** (Resource Server)
```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```
**What it does:**
- Configures the app as a **Resource Server**
- **Validates JWT tokens** on every request
- Uses **JWKS** from `issuer-uri` to verify signatures

---

#### 2. **OAuth2AuthorizedClientManager** (Client)
```java
@Bean
public OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository repo,
        OAuth2AuthorizedClientService service) {
    
    var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(repo, service);
    OAuth2AuthorizedClientProvider provider = 
        OAuth2AuthorizedClientProviderBuilder.builder()
            .clientCredentials()
            .build();
    manager.setAuthorizedClientProvider(provider);
    return manager;
}
```
**What it does:**
- **Generates access tokens** using client credentials
- **Caches tokens** (reuses until expiration)
- **Auto-refreshes** expired tokens

---

#### 3. **RestTemplate with Bearer Token**
```java
OAuth2AuthorizeRequest authorizeRequest = 
    OAuth2AuthorizeRequest.withClientRegistrationId("keycloak-client")
        .principal("client-app")
        .build();

var client = manager.authorize(authorizeRequest);
String token = client.getAccessToken().getTokenValue();

HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(token);

restTemplate.exchange(
    "http://localhost:8082/data",
    HttpMethod.GET,
    new HttpEntity<>(headers),
    String.class
);
```
**What it does:**
- Requests a token from `OAuth2AuthorizedClientManager`
- Adds token to `Authorization: Bearer <token>` header
- Calls the Resource Server

---

#### 4. **Token Propagation** (Hybrid)
```java
var authentication = SecurityContextHolder.getContext().getAuthentication();
Jwt jwt = (Jwt) authentication.getPrincipal();
String token = jwt.getTokenValue();

HttpHeaders headers = new HttpHeaders();
headers.setBearerAuth(token);

restTemplate.exchange(
    "http://localhost:8082/data",
    HttpMethod.GET,
    new HttpEntity<>(headers),
    String.class
);
```
**What it does:**
- Extracts the **incoming JWT** from Spring Security context
- **Propagates the same token** to downstream services
- Maintains **end-to-end security** (same user context)

---

## 🚀 How to Run

### Prerequisites

1. **Java 21** (or compatible version)
2. **Maven** (for Spring Boot apps)
3. **Node.js & npm** (for React PKCE app)
4. **Keycloak** (running on `localhost:8089`)

---

### Step 1: Start Keycloak

#### Option A: Docker
```bash
docker run -p 8089:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest \
  start-dev
```

#### Option B: Standalone
1. Download Keycloak from [keycloak.org](https://www.keycloak.org/downloads)
2. Extract and run:
   ```bash
   cd keycloak-<version>/bin
   ./kc.sh start-dev --http-port=8089
   ```

3. Access Keycloak Admin Console:
   ```
   http://localhost:8089
   Username: admin
   Password: admin
   ```

---

### Step 2: Configure Keycloak

#### For Client Credentials Flow (`oauth2-client-flow` realm)

1. **Create Realm:**
   - Name: `oauth2-client-flow`

2. **Create Client:**
   - Client ID: `client-flow`
   - Client Authentication: **ON** (confidential)
   - Authorization: **OFF**
   - Valid Redirect URIs: `*` (for testing)
   - Web Origins: `*`
   - Direct Access Grants: **ON**
   - Service Accounts Roles: **ON**

3. **Get Client Secret:**
   - Go to **Credentials** tab
   - Copy the **Client Secret**
   - Update `application.yaml` in `client_credntial_flow/client/`

---

#### For Authorization Code Flow (`oauth2_learn` realm)

1. **Create Realm:**
   - Name: `oauth2_learn`

2. **Create Client:**
   - Client ID: `oauth2-flow`
   - Client Authentication: **ON**
   - Valid Redirect URIs:
     ```
     http://localhost:8080/login/oauth2/code/keycloak
     http://localhost:8080/
     ```
   - Web Origins: `http://localhost:8080`
   - Standard Flow: **ON**

3. **Create User:**
   - Username: `testuser`
   - Email: `test@example.com`
   - First Name: `Test`
   - Last Name: `User`
   - Set Password (Credentials tab, Temporary: **OFF**)

4. **Update `application.yaml`:**
   ```yaml
   spring:
     security:
       oauth2:
         client:
           registration:
             oauth2-code-flow:
               client-id: oauth2-flow
               client-secret: <YOUR_CLIENT_SECRET>
   ```

---

#### For PKCE Flow (React)

1. **Use existing realm:** `oauth2-client-flow`

2. **Create Public Client:**
   - Client ID: `client-flow` (or create new)
   - Client Authentication: **OFF** (public client)
   - Valid Redirect URIs: `http://localhost:5173/*`
   - Web Origins: `http://localhost:5173`
   - Standard Flow: **ON**
   - PKCE Code Challenge Method: **S256**

3. **Update `authConfig.js`:**
   ```javascript
   export const authConfig = {
     clientId: 'client-flow',
     authorizationEndpoint: 'http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/auth',
     tokenEndpoint: 'http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/token',
     redirectUri: 'http://localhost:5173',
   };
   ```

---

### Step 3: Run the Applications

#### Client Credentials Flow

**Terminal 1 (Resource Server):**
```bash
cd client_credntial_flow/resourceserver
mvn spring-boot:run
```
**Runs on:** `http://localhost:8082`

**Terminal 2 (Client):**
```bash
cd client_credntial_flow/client
mvn spring-boot:run
```
**Output:**
```
🔥 ACCESS TOKEN:
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...

🚀 RESPONSE FROM RESOURCE SERVER:
<200 OK, Hello From Resource Server>
```

---

#### Authorization Code Flow

```bash
cd oauth2flow
mvn spring-boot:run
```
**Access:** `http://localhost:8080`

**Steps:**
1. Click "LogIn with Keycloak"
2. Enter credentials (`testuser` / password)
3. View user profile
4. Click "Logout"

---

#### PKCE Flow (React)

```bash
cd oauth2_pkce
npm install
npm run dev
```
**Access:** `http://localhost:5173`

**Steps:**
1. Click "Sign in with Keycloak"
2. Login via Keycloak
3. View token details
4. Click "Logout"

---

#### Hybrid (Client + Resource Server)

**Terminal 1 (Downstream Resource Server):**
```bash
cd client_credntial_flow/resourceserver
mvn spring-boot:run
```

**Terminal 2 (Hybrid App):**
```bash
cd client_as_oauth_resourceserver/client_with_both
mvn spring-boot:run
```

**Test:**
```bash
# Get token from Keycloak
curl -X POST http://localhost:8089/realms/oauth2-client-flow/protocol/openid-connect/token \
  -d "client_id=client-flow" \
  -d "client_secret=<YOUR_SECRET>" \
  -d "grant_type=client_credentials"

# Call hybrid app with token
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8081/proxy
```

---

## ⚠️ Common Errors & Fixes

### 1. **CORS Error with Keycloak**

**Error:**
```
Access to XMLHttpRequest at 'http://localhost:8089/...' has been blocked by CORS policy
```

**Fix:**
- In Keycloak Client settings:
  - **Web Origins:** Add `http://localhost:5173` (or your app URL)
  - Or use `*` for development (NOT for production)

---

### 2. **Invalid Issuer / JWT Validation Failure**

**Error:**
```
An error occurred while attempting to decode the Jwt: 
Signed JWT rejected: Invalid issuer
```

**Cause:** Mismatch between `issuer-uri` in `application.yaml` and actual Keycloak URL

**Fix:**
```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8089/realms/oauth2-client-flow
          # ✅ Must match Keycloak realm exactly
```

**Verify Issuer:**
```bash
curl http://localhost:8089/realms/oauth2-client-flow/.well-known/openid-configuration | jq .issuer
```

---

### 3. **401 Unauthorized vs 403 Forbidden**

| Status | Meaning | Cause |
|--------|---------|-------|
| **401 Unauthorized** | "Who are you?" | Missing or invalid token |
| **403 Forbidden** | "You can't do that" | Valid token, but insufficient permissions (roles/scopes) |

**Fix for 401:**
- Check if `Authorization: Bearer <token>` header is present
- Verify token is not expired

**Fix for 403:**
- Check user roles in Keycloak
- Verify `@PreAuthorize("hasRole('ADMIN')")` matches user's roles

---

### 4. **Missing Redirect URI Error**

**Error:**
```
Invalid parameter: redirect_uri
```

**Fix:**
- In Keycloak Client settings:
  - **Valid Redirect URIs:** Add exact URL:
    ```
    http://localhost:8080/login/oauth2/code/keycloak
    ```
  - Or use wildcard for testing: `http://localhost:8080/*`

---

### 5. **PKCE Misconfiguration**

**Error:**
```
PKCE verification failed
```

**Fix:**
- Ensure Keycloak Client has:
  - **Client Authentication:** OFF (public client)
  - **Proof Key for Code Exchange Code Challenge Method:** S256

- In React app, ensure `react-oauth2-code-pkce` is configured:
  ```javascript
  {
    clientId: 'client-flow',
    authorizationEndpoint: '...',
    tokenEndpoint: '...',
    // PKCE is automatic with this library
  }
  ```

---

### 6. **Token Expired**

**Error:**
```
JWT expired at 2024-02-06T12:00:00Z
```

**Fix:**
- **For development:** Increase token lifespan in Keycloak:
  - Realm Settings → Tokens → Access Token Lifespan: `15 minutes` → `60 minutes`

- **For production:** Implement **refresh token** logic:
  ```java
  OAuth2AuthorizedClientProvider provider = 
      OAuth2AuthorizedClientProviderBuilder.builder()
          .clientCredentials()
          .refreshToken()  // ✅ Add refresh token support
          .build();
  ```

---

## 📖 Learning Path

### Beginner (Start Here)

1. **Understand the Problem:**
   - Why do we need OAuth2?
   - What problems does Keycloak solve?

2. **Learn Basic Concepts:**
   - What is a token?
   - What is authentication vs authorization?
   - What is a JWT?

3. **Run the Examples:**
   - Start with **Client Credentials Flow** (simplest)
   - Then try **Authorization Code Flow**

---

### Intermediate

1. **Study OAuth2 Flows:**
   - When to use Client Credentials vs Authorization Code?
   - What is PKCE and why is it needed?

2. **Explore JWT:**
   - Decode a JWT at [jwt.io](https://jwt.io)
   - Understand `iss`, `sub`, `exp`, `iat`, `scope`

3. **Learn Spring Security:**
   - How does `oauth2ResourceServer()` work?
   - What is `OAuth2AuthorizedClientManager`?
   - How to extract user info from `SecurityContext`?

---

### Advanced

1. **Keycloak Deep Dive:**
   - Realm roles vs client roles
   - Custom claims in tokens
   - Token mappers
   - User federation (LDAP, Active Directory)

2. **Microservices Patterns:**
   - Token propagation
   - API Gateway with OAuth2
   - Service-to-service authentication

3. **Security Best Practices:**
   - Token storage (never in localStorage for web apps)
   - HTTPS in production
   - Token rotation
   - Scope-based authorization

4. **Study Standards:**
   - [OAuth 2.0 RFC 6749](https://datatracker.ietf.org/doc/html/rfc6749)
   - [OpenID Connect Core](https://openid.net/specs/openid-connect-core-1_0.html)
   - [JWT RFC 7519](https://datatracker.ietf.org/doc/html/rfc7519)

---

### Recommended Study Order

```
1. OAuth2 Basics (flows, tokens)
   ↓
2. OpenID Connect (ID tokens, userinfo)
   ↓
3. JWT (structure, validation)
   ↓
4. Spring Security OAuth2 (client, resource server)
   ↓
5. Keycloak (realms, clients, users, roles)
   ↓
6. Microservices Security (token propagation, API gateway)
```

---

## 🔗 Useful Resources

- **Keycloak Documentation:** https://www.keycloak.org/documentation
- **Spring Security OAuth2:** https://spring.io/projects/spring-security-oauth
- **OAuth2 Simplified:** https://aaronparecki.com/oauth-2-simplified/
- **JWT.io:** https://jwt.io (decode and verify JWTs)
- **OAuth2 Playground:** https://www.oauth.com/playground/

---

## 📝 Summary

This project demonstrates:

| Flow | Use Case | Client Type | User Login? | Token Type |
|------|----------|-------------|-------------|------------|
| **Client Credentials** | Service-to-service | Confidential | ❌ No | Access Token |
| **Authorization Code** | Web app with backend | Confidential | ✅ Yes | Access + ID Token |
| **PKCE** | SPA / Mobile app | Public | ✅ Yes | Access + ID Token |
| **Hybrid** | Microservice gateway | Confidential | ✅ Yes | Access Token (propagated) |

**Key Takeaways:**
- **Keycloak** centralizes authentication/authorization
- **OAuth2** provides secure token-based access
- **OIDC** adds user identity on top of OAuth2
- **JWT** is a self-contained, verifiable token format
- **Spring Security** simplifies OAuth2 integration

---

## 🤝 Contributing

Feel free to:
- Add more examples
- Improve documentation
- Fix bugs
- Suggest best practices

---

## 📄 License

This project is for educational purposes. Use freely for learning!

---

**Happy Learning! 🚀**
