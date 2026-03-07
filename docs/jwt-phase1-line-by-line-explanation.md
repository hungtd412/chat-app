# JWT Refactor Phase 1 — Line-by-Line Explanation

This document explains the code written for Phase 1.

## Files changed
- `src/main/java/com/hungtd/chatapp/service/auth/impl/AuthenticationServiceImpl.java`
- `src/main/java/com/hungtd/chatapp/util/JwtUtil.java`
- `src/test/java/com/hungtd/chatapp/service/auth/AuthenticationServiceImplTest.java`

## 1) `AuthenticationServiceImpl.refreshToken(...)`

### What was written
- Read `accessToken` and `refreshToken` from request.
- Added null/blank guards:
  - refresh missing -> `EMPTY_REFRESHTOKEN`
  - access missing -> `EMPTY_ACCESSTOKEN`
- Verified refresh token with `jwtUtil.verifyRefreshToken(refreshToken)`.
- Verified current access token in a tolerant way:
  - allowed errors to continue refresh: `UNAUTHENTICATED`, `SESSION_EXPIRED`, `INVALID_KEY`
  - rethrow any other auth error.
- Blacklisted old access token if its expiration can be read.
- Parsed subject from refresh JWT as `Long userId`.
- Loaded user by `userRepository.findById(userId)`.
- Generated new access token and returned it while keeping refresh token unchanged.
- Preserved `AppException` as-is; mapped `ParseException`/`NumberFormatException` to `INVALID_KEY`.

### Why each line matters
- Early input checks avoid ambiguous downstream failures.
- `verifyRefreshToken` enforces all core token checks plus token type.
- Access token tolerance lets refresh work even when access token is already invalid/expired.
- Blacklisting old access token reduces reuse risk.
- `sub` is now consistently interpreted as user ID.
- `findById` fixes the old `sub`/username mismatch bug.

## 2) `JwtUtil.verifyToken(...)`

### What was written
- `SignedJWT signedJWT = parseAndVerifySignature(token);`
- Read expiration from claims and throw `SESSION_EXPIRED` if missing/expired.
- Check blacklist and throw `UNAUTHENTICATED` if revoked.
- Return verified `SignedJWT`.
- Map claim parse issues to `INVALID_KEY`.

### Why each line matters
- Splits signature verification from claim/business checks.
- Gives deterministic error taxonomy:
  - invalid signature/parse => `INVALID_KEY`
  - expired => `SESSION_EXPIRED`
  - blacklisted => `UNAUTHENTICATED`

## 3) `JwtUtil.verifyRefreshToken(...)`

### What was written
- Reused `verifyToken(token)` first.
- Read claim `token_type`.
- Enforced `token_type == refresh` else `INVALID_TOKEN_TYPE`.
- Returned verified refresh `SignedJWT`.

### Why each line matters
- Prevents access tokens from being used in refresh endpoint.
- Keeps one canonical verification flow.

## 4) `JwtUtil.extractUserIdByTokenStompHeader(...)`

### What was written
- Extract token from STOMP headers/session.
- Verify token using `verifyToken`.
- Read `sub` from verified claims.
- Validate non-null subject.
- Parse to `Long` and return.
- On parse/format errors: throw `UNAUTHENTICATED`.

### Why each line matters
- Removes insecure manual payload decoding.
- Ensures websocket user id comes only from verified JWT claims.

## 5) Removed method: `extractUserIdFromToken(...)`

### What changed
- Deleted manual Base64 payload parsing + string scanning for `"sub"`.

### Why
- Manual parsing can read untrusted payload data.
- Verified claim extraction is safer and consistent.

## 6) Added helper: `parseAndVerifySignature(...)`

### What was written
- Build `MACVerifier` with `SIGNER_KEY`.
- Parse token into `SignedJWT`.
- Verify signature.
- Throw `INVALID_KEY` if parse/signature fails.

### Why each line matters
- Centralizes cryptographic checks.
- Reduces duplication in token verification methods.

## 7) Test updates

### In `AuthenticationServiceImplTest`
- Refresh-success test:
  - Added `when(userRepository.findById(1L)).thenReturn(Optional.of(user));`
- Refresh misuse test:
  - Updated expectation from `SESSION_EXPIRED` to `INVALID_TOKEN_TYPE`.

### Why
- Tests now match the fixed `sub=userId` behavior and normalized error mapping.

## Final outcome of Phase 1
- Subject semantics are consistent (`sub` treated as user ID in refresh).
- No manual JWT payload parsing remains.
- Auth errors are clearer and mapped to specific error codes.
