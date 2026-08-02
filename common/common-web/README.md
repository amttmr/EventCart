# common-web

`common-web` contains reusable web-layer models shared by Spring Boot services.

## Responsibility

This module keeps common API response shapes consistent across services.

## Current Functionality

| Class | Purpose |
| --- | --- |
| `ApiResponse` | Standard success response wrapper |
| `ApiError` | Standard error response model |
| `PageResponse` | Stable pagination response model built from Spring Data `Page` |

## Why This Module Exists

Without a shared web model, each service can accidentally return different response shapes. This module helps us practice consistent API design while still keeping business logic inside each service.

## Interview Angle

You should be able to explain why DTOs and response wrappers are useful, and also when they can become unnecessary ceremony. For EventCart, consistency is helpful because many services expose REST APIs.

