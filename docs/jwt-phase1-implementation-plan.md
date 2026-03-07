# JWT Refactor – Phase 1 Implementation Plan

## Objective
Implement Phase 1 from `docs/jwt-refactor-plan.md`:
1. Unify `sub` semantics in refresh flow.
2. Replace manual JWT payload parsing with verified claim extraction.
3. Normalize auth exception handling for invalid/expired/blacklisted/token-type errors.

## Scope
- In scope: `AuthenticationServiceImpl.refreshToken(...)`, `JwtUtil`, related auth tests.
- Out of scope: rotation, Redis key optimization, websocket authority mapping.

## Design Decisions
1. Canonical subject policy: `sub = userId`, refresh uses `findById(...)`.
2. Verified claim extraction only: remove manual payload parsing, use verified `SignedJWT` claims.
3. Error mapping:
   - malformed/signature failure -> `INVALID_KEY`
   - expired -> `SESSION_EXPIRED`
   - blacklisted -> `UNAUTHENTICATED`
   - wrong token_type in refresh -> `INVALID_TOKEN_TYPE`
   - empty refresh/access in request -> `EMPTY_REFRESHTOKEN` / `EMPTY_ACCESSTOKEN`

## Implementation Steps
1. Refactor `JwtUtil.verifyToken` for explicit signature/expiry/blacklist checks.
2. Refactor `JwtUtil.verifyRefreshToken` to enforce `token_type=refresh`.
3. Remove manual `extractUserIdFromToken`, use verified claims in websocket extraction.
4. Fix refresh lookup to `findById(Long.parseLong(sub))`.
5. Normalize refresh error handling and keep specific `AppException` types.
6. Update tests for `findById` and token-type expectations.

## Verification Checklist
- Refresh works for valid refresh token and existing user id in `sub`.
- Access token as refresh token returns `INVALID_TOKEN_TYPE`.
- Expired token path returns `SESSION_EXPIRED`.
- Blacklisted token path returns `UNAUTHENTICATED`.
- No manual base64 payload parsing for user id extraction.
