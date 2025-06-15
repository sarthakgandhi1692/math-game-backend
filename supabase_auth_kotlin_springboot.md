
# High-Level Architecture: Supabase Auth with Kotlin Spring Boot (Gradle)

## 🧱 Components

| Component                | Role                                                                 |
|--------------------------|----------------------------------------------------------------------|
| **Supabase Auth**        | Handles authentication (email/password, OAuth, etc.)                |
| **Supabase Postgres DB** | Stores user data and additional metadata                            |
| **Spring Boot App (Kotlin)** | Acts as backend API gateway and application logic                |
| **Gradle Build System**  | Project dependency & build management                                |
| **Supabase Admin API / JWT** | Used to verify or fetch user metadata from Supabase securely     |

---

## 🗂️ Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   ├── com.example.app/
│   │   │   ├── config/            # Supabase config and security
│   │   │   ├── controller/        # API Controllers
│   │   │   ├── service/           # Business logic
│   │   │   ├── model/             # Data models
│   │   │   └── security/          # JWT decoding, filters
│   └── resources/
│       └── application.yml        # Supabase URL/keys config
build.gradle.kts
```

---

## 🔐 Authentication Flow

### 1. **User Auth via Supabase**
- The frontend uses Supabase SDK to **sign up / login** users using email/password or OAuth.
- Supabase returns a **JWT access token** and refresh token.

### 2. **Frontend Sends Token to Spring Boot**
- On each protected API call, frontend sends the **access token** in the `Authorization` header (`Bearer <JWT>`).

### 3. **Token Verification in Spring Boot**
- Spring Boot validates the token using:
  - Supabase's **JWT public keys** (JWKs from `https://<supabase_project>.supabase.co/auth/v1/keys`)
  - OR use Supabase's Admin SDK to introspect/verify the token

### 4. **User Data Extraction**
- After successful validation, the decoded JWT is used to:
  - Extract user ID, email, etc.
  - Check roles, permissions (custom claims)
  - Optionally: fetch user profile from Supabase DB or cache

---

## 🛡️ Security Configuration

Use Spring Security with a custom JWT filter:

```kotlin
class JwtAuthenticationFilter : OncePerRequestFilter() {
    override fun doFilterInternal(...) {
        val token = extractJwtFromRequest(request)
        val decoded = verifyJwt(token) // Using JWKs or Supabase Admin SDK
        // Set security context
    }
}
```

---

## ⚙️ Gradle Dependencies

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.auth0:java-jwt:4.4.0") // Optional for JWT decoding
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}
```

---

## 🔑 Supabase Config (`application.yml`)

```yaml
supabase:
  url: https://<your-project-id>.supabase.co
  anon-key: <anon-key>
  service-role-key: <service-role-key> # For backend if needed
  jwt:
    jwks-url: https://<your-project-id>.supabase.co/auth/v1/keys
    issuer: https://<your-project-id>.supabase.co/auth/v1
```

---

## 🧠 Best Practices

- Cache JWKS for performance.
- Use `OncePerRequestFilter` for minimal performance impact.
- Never expose service-role key to frontend.
- Optional: Mirror user profiles in your own DB for faster access.

---

## 📌 Summary

**Supabase handles authentication and user management**, while **Spring Boot verifies and enforces access control** using Supabase-generated JWTs. This separation of concerns keeps your backend lightweight and secure.
