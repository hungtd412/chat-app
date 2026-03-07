# JWT Refactor Plan for Chat App

## 1) Current constraints in this project

Based on the current codebase (`SecurityConfig`, `CustomJwtDecoder`, `JwtUtil`, `AuthenticationServiceImpl`, `WebSocketAuthInterceptor`, `TokenBlacklistServiceImpl`):

- **Algorithm/keying**: symmetric signing with `HS512` and one shared secret (`jwt.signerKey`).
- **Token state model**: mostly stateless JWT, but with **stateful logout/revoke** via Redis blacklist.
- **Token pair model**: access + refresh token exists, refresh endpoint currently keeps same refresh token.
- **Validation path**:
  - Spring Security Resource Server uses a custom decoder.
  - `CustomJwtDecoder` calls `introspect()` (which verifies token), then decodes again with Nimbus -> duplicate work per request.
- **WebSocket auth**:
  - Manual token extraction from STOMP headers/session attributes.
  - Role assignment is hard-coded as `ROLE_USER` in interceptor.
- **Infrastructure constraints**:
  - Redis must be available for blacklist checks.
  - Secret key loaded from env file (`env.properties` via `application.yaml`).
- **Potential functional inconsistency**:
  - `JwtUtil` sets `sub` to **user id**.
  - `refreshToken()` reads `sub` into variable named `username` then queries `findByUsername(...)` -> likely bug path.

---

## 2) Pros and cons of current JWT implementation

### Pros

- Uses strong HMAC algorithm (`HS512`).
- Access/refresh token separation already exists (`token_type` claim).
- Blacklist in Redis supports logout and revocation before token expiry.
- Integrates with Spring Security Resource Server rather than fully custom filter chain.
- Includes token expiration + `jti` claim generation.

### Cons / Risks

- **Subject inconsistency bug risk** (`sub` treated as username in refresh flow while generated from user id).
- **Double verification/decoding** on each secured request (performance + complexity overhead).
- **Refresh token rotation not implemented** (replay window if refresh token is leaked).
- **No dedicated key versioning (`kid`) / key rotation strategy**.
- **Blacklist key stores entire raw token** as Redis key (large key size, possible operational inefficiency).
- **Claims mapping is not strongly normalized** (`scope` is space-separated roles string; websocket role mapping is hard-coded).
- **Limited centralized error taxonomy for auth flows** (mixed exception handling paths).
- **Potential overexposure to token parsing hacks** (`extractUserIdFromToken` manually base64 parses payload instead of always using verified JWT parser).

---

## 3) What it should be (target architecture)

### Best-practice signals from 10 GitHub repositories (via GitHub CLI search)

Repositories sampled:

1. https://github.com/bezkoder/angular-11-spring-boot-jwt-authentication
2. https://github.com/knowledgefactory4u/springboot-reactjs-jwt-authentication
3. https://github.com/irojascorsico/spring-boot-jwt-authentication
4. https://github.com/IMS94/spring-boot-jwt-authentication
5. https://github.com/bezkoder/angular-10-spring-boot-jwt-authentication
6. https://github.com/hendisantika/spring-boot-jwt-authentication
7. https://github.com/Rapter1990/Spring-Boot-JWT-Authentication
8. https://github.com/leetjourney/spring-boot-jwt-authentication
9. https://github.com/rabdelhamid/Rehab-Academy-For-Programming-Spring-Boot-JWT-Authentication
10. https://github.com/mehmetpekdemir/Spring-Boot-Jwt-Authentication

Common patterns/best practices reflected across Spring JWT projects and modern Spring Security guidance:

- Keep resource-server validation path simple and single-pass.
- Use short-lived access token + long-lived refresh token.
- Prefer refresh token rotation for higher security.
- Keep claims minimal and consistent (`sub`, `roles/scope`, `iat`, `exp`, `jti`, optional `iss`, `aud`).
- Centralize token creation/verification and avoid ad-hoc parsing.
- Handle revocation with Redis using TTL and compact key strategy.
- Make authorization mapping deterministic (roles -> authorities).
- Keep auth exceptions and HTTP responses consistent.

### Target state for this project

- One canonical subject policy: `sub = userId` (or username), used consistently everywhere.
- One validation flow in `JwtDecoder` without redundant introspection calls.
- Refresh token rotation enabled:
  - each refresh invalidates old refresh token (`jti` blacklist/denylist)
  - issues new access + new refresh token.
- Redis blacklist stores compact identifiers (e.g., `jti`) rather than whole token string.
- WebSocket auth uses verified claims and real authorities from token claims.
- Strict claim schema documented and enforced.
- Secrets and key lifecycle strategy documented (including planned rotation path).

---

## 4) Detailed refactor plan (step-by-step)

## Phase 0 — Safety net

1. Add focused auth tests before changing logic:
   - login success/failure
   - access token authorization
   - refresh success/failure
   - logout revocation behavior
   - websocket connect with valid/invalid token.
2. Add fixtures/users with known roles for deterministic claim mapping checks.

## Phase 1 — Fix correctness issues first

1. **Unify `sub` semantics**:
   - Decide canonical subject (`userId` recommended).
   - Update refresh flow to lookup user by subject type correctly (`findById` if `sub=userId`).
2. Replace manual payload parsing (`extractUserIdFromToken`) with verified JWT claim extraction from Nimbus parser + signature validation.
3. Normalize exception handling so invalid/expired/blacklisted/token-type errors map to clear error codes.

## Phase 2 — Simplify validation flow

1. Refactor `CustomJwtDecoder`:
   - remove recursive introspection dependency for internal request auth path.
   - perform signature + expiry (+ revocation) checks once.
2. Keep `/auth/introspect` endpoint only for external clients/admin diagnostics if needed; otherwise deprecate.
3. Ensure `SecurityConfig` uses configured `JwtAuthenticationConverter` explicitly for authority mapping.

## Phase 3 — Refresh-token hardening

1. Implement refresh token rotation:
   - on refresh: validate refresh token, revoke old refresh token, issue new access + new refresh.
2. Add `jti`-based revocation strategy:
   - store `blacklisted_jti:{jti}` with TTL to token expiry.
   - optionally include token family/session id to support “logout all devices”.
3. Update API contract and frontend handling for rotated refresh token return.

## Phase 4 — Redis/blacklist optimization

1. Move from full-token Redis keys to hashed token or `jti` keys.
2. Add namespace/version prefix for migration safety: `auth:v1:blacklist:jti:{id}`.
3. Add observability:
   - metrics for blacklist hit/miss
   - refresh success/fail rates
   - auth failure reason counters.

## Phase 5 — WebSocket auth alignment

1. Validate JWT once in `WebSocketAuthInterceptor` using same token service.
2. Map authorities from claims (not hard-coded `ROLE_USER`).
3. Set principal in a shared format with HTTP security context conventions.
4. Add tests for CONNECT/SEND auth behavior.

## Phase 6 — Configuration and secrets hardening

1. Add explicit JWT settings in config:
   - access TTL, refresh TTL
   - issuer/audience
   - clock skew.
2. Add key rotation readiness:
   - introduce `kid` support design even if still symmetric short-term.
3. Mask/guard sensitive logs; never log raw JWTs.

## Phase 7 — Migration and rollout strategy

1. **Backward-compatible window**:
   - temporarily accept old refresh tokens until migration date.
2. Release plan:
   - deploy server with dual validation support
   - deploy frontend refresh-rotation handling
   - enforce strict mode after grace period.
3. Runbook:
   - emergency token invalidation process
   - Redis outage behavior and fallback policy.

---

## 5) Proposed implementation backlog (prioritized)

### P0 (must do now)

- Fix `sub`/lookup inconsistency in refresh flow.
- Remove duplicate token verification path.
- Remove manual JWT payload extraction helper.
- Add auth flow tests for login/refresh/logout.

### P1 (security hardening)

- Refresh token rotation.
- `jti`-based blacklist and Redis key optimization.
- WebSocket authority mapping from claims.

### P2 (operability)

- Metrics + structured auth error logging.
- Configurable issuer/audience/clock-skew.
- Migration toggles and rollout docs.

---

## 6) Suggested concrete code touchpoints

- `src/main/java/com/hungtd/chatapp/util/JwtUtil.java`
- `src/main/java/com/hungtd/chatapp/service/auth/impl/AuthenticationServiceImpl.java`
- `src/main/java/com/hungtd/chatapp/configuration/CustomJwtDecoder.java`
- `src/main/java/com/hungtd/chatapp/configuration/SecurityConfig.java`
- `src/main/java/com/hungtd/chatapp/configuration/WebSocketAuthInterceptor.java`
- `src/main/java/com/hungtd/chatapp/service/auth/impl/TokenBlacklistServiceImpl.java`
- `src/main/resources/application.yaml`

---

## 7) Definition of done for JWT refactor

- All auth flows pass tests (HTTP + WebSocket).
- Refresh rotation works and old refresh token reuse is rejected.
- Single-pass JWT validation path in request pipeline.
- Subject/claims semantics are consistent and documented.
- Redis revocation keys are compact and TTL-managed.
- No raw token leakage in logs.
- Frontend compatibility confirmed for new refresh contract.
