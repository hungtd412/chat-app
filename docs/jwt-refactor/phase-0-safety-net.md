# Phase 0 — Safety Net (JWT Refactor)

## Goal
Create a minimal but reliable test safety net before changing JWT logic, so we can refactor with confidence and catch regressions early.

---

## Scope of Phase 0

This phase only adds **tests and test fixtures**.
No JWT behavior changes yet.

- Add integration-style tests for authentication endpoints.
- Add tests for token lifecycle (login, refresh, logout/revocation).
- Add tests for protected endpoints with valid/invalid tokens.
- Add WebSocket auth handshake tests (CONNECT/SEND auth pass/fail).
- Add deterministic user fixtures with known roles.

---

## Why this phase is required

Current JWT/auth code has high-risk areas:
- `sub` semantic inconsistency (id vs username usage).
- Duplicate token validation path.
- Refresh token currently not rotated.
- WebSocket authority mapping is hard-coded.

Without a safety net, fixing these can introduce hidden regressions.

---

## Deliverables

Implemented test classes:

1. `src/test/java/com/hungtd/chatapp/service/auth/AuthenticationServiceImplTest.java`
2. `src/test/java/com/hungtd/chatapp/util/JwtUtilTest.java`
3. `src/test/java/com/hungtd/chatapp/configuration/WebSocketAuthInterceptorTest.java`

These are currently **service/interceptor-level tests** (fast safety net) rather than full HTTP + DB integration tests.

---

## Checklist status (updated)

- [x] Login success
- [x] Login failure
- [ ] Access protected endpoint with valid token
- [ ] Access protected endpoint with expired/invalid token
- [x] Refresh success with valid refresh token
- [x] Refresh failure with wrong token type
- [x] Logout blacklists access token
- [x] Logout blacklists refresh token
- [x] WebSocket CONNECT with valid token succeeds
- [x] WebSocket CONNECT with invalid token fails

---

## What exactly was implemented

## A) Authentication service tests

In `AuthenticationServiceImplTest`:
- login success returns access + refresh tokens.
- login failure throws `UNAUTHENTICATED`.
- refresh success returns new access token and authenticated status.
- refresh with access token as refresh token throws `SESSION_EXPIRED`.
- logout attempts to blacklist provided access/refresh tokens.

## B) JWT utility tests

In `JwtUtilTest`:
- valid non-blacklisted token verifies successfully.
- blacklisted token throws `UNAUTHENTICATED`.
- using access token in refresh-token verification throws `INVALID_TOKEN_TYPE`.

## C) WebSocket auth interceptor tests

In `WebSocketAuthInterceptorTest`:
- CONNECT with valid bearer token sets authenticated principal.
- CONNECT with invalid token throws `UNAUTHENTICATED`.

---

## Notes / Known limitations in current Phase 0 output

1. The two protected-endpoint checks are still pending because they require fuller HTTP integration setup (controller + security + test data wiring).
2. Current tests are intentionally lightweight and isolated to speed up confidence before refactor.
3. One refresh-path test currently uses username `"1"` in fixture to align with existing `sub` handling behavior in codebase.

---

## Next step to complete Phase 0 fully

Add `@SpringBootTest` + `@AutoConfigureMockMvc` integration tests for:

- calling one secured endpoint (e.g. `GET /conversations/current-user`) with:
  - valid access token -> success,
  - invalid/expired token -> unauthorized.

After that, the two remaining checklist items can be marked done.
