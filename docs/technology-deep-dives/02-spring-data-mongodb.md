# Spring Data MongoDB

Spring Data MongoDB is the Java persistence layer used by EventCart services to map Java classes to MongoDB documents and to create repository-based data access code.

## Where It Is Used

Spring Data MongoDB appears in every data-owning service:

- `services/catalog-service/src/main/java/com/eventcart/catalog/domain`
- `services/cart-service/src/main/java/com/eventcart/cart/domain`
- `services/order-service/src/main/java/com/eventcart/order/domain`
- `services/order-service/src/main/java/com/eventcart/order/outbox`
- `services/inventory-service/src/main/java/com/eventcart/inventory/domain`
- `services/inventory-service/src/main/java/com/eventcart/inventory/outbox`
- `services/payment-service/src/main/java/com/eventcart/payment/domain`
- `services/payment-service/src/main/java/com/eventcart/payment/outbox`
- `services/notification-service/src/main/java/com/eventcart/notification/domain`

The application properties use `spring.mongodb.uri` to connect each service to its database.

## Why It Is Used

Spring Data MongoDB gives us:

- Object mapping between Java classes and MongoDB documents.
- Repository interfaces such as `MongoRepository`.
- Derived query methods such as `findByCustomerIdOrderByCreatedAtDesc`.
- Annotation-based indexes.
- Auditing fields with `@CreatedDate` and `@LastModifiedDate`.
- Optimistic locking through `@Version`.
- Testcontainers-friendly integration testing.

It lets the project focus on business behavior while still exposing important persistence concepts.

## Important Annotations

| Annotation | Usage |
| --- | --- |
| `@Document(collection = "...")` | Maps a Java class to a MongoDB collection. |
| `@Id` | Maps the document ID. |
| `@Indexed` | Creates an index for a field. |
| `@CompoundIndex` | Creates an index across multiple fields. |
| `@Version` | Enables optimistic locking. |
| `@CreatedDate` | Populates creation timestamp. |
| `@LastModifiedDate` | Populates update timestamp. |
| `@EnableMongoAuditing` | Enables auditing annotations. |

## Repository Pattern In EventCart

Examples:

- `ProductRepository` manages products.
- `CartRepository` manages carts.
- `OrderRepository` manages orders.
- `InventoryItemRepository` manages stock.
- `InventoryReservationRepository` manages reservations.
- `PaymentAttemptRepository` manages payment attempts.
- `NotificationRepository` manages notifications.
- `OrderOutboxRepository`, `InventoryOutboxRepository`, and `PaymentOutboxRepository` manage outbox events.

Repository methods express query intent:

```java
List<OrderDocument> findByCustomerIdOrderByCreatedAtDesc(String customerId);
Optional<CartDocument> findByCustomerId(String customerId);
List<OrderOutboxEventDocument> findTop20ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
```

## Best Practices

- Keep repository interfaces small and focused on one aggregate.
- Put business rules in services, not repositories.
- Return DTOs from controllers, not document classes.
- Use repository derived queries for simple lookups.
- Use `MongoTemplate` only when query complexity requires it.
- Keep indexing intentional. Too many indexes slow writes.
- Use `@Version` where concurrent updates can happen.
- Use auditing for traceability.
- Keep database names service-specific.
- Do not share repositories across services.
- Test important repository behavior with real MongoDB using Testcontainers.

## How To Verify Behavior

Run unit and integration tests:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -P integration-tests verify
```

Run a Mongo-backed integration test:

```powershell
.\mvnw.cmd -P integration-tests -pl services/catalog-service verify
```

Verify indexes from MongoDB:

```javascript
use eventcart_catalog
db.products.getIndexes()

use eventcart_cart
db.carts.getIndexes()

use eventcart_order
db.orders.getIndexes()
db.outbox_events.getIndexes()
```

Verify optimistic version fields:

```javascript
db.products.findOne({ sku: "SKU-1001" }, { version: 1, updatedAt: 1 })
```

After updating a product through the API, the `version` should increment and `updatedAt` should change.

## How To Debug

Enable MongoDB driver logs temporarily when needed:

```yaml
logging:
  level:
    org.springframework.data.mongodb.core.MongoTemplate: DEBUG
    org.mongodb.driver.protocol.command: DEBUG
```

Debug checklist:

| Problem | Check |
| --- | --- |
| Repository does not find data | Confirm the service is connected to the expected database. |
| Duplicate key error | Inspect unique indexes such as SKU or customer cart. |
| Version conflict | Check whether two updates are writing the same document concurrently. |
| Auditing fields null | Confirm Mongo auditing configuration is active in the service. |
| Query is slow | Check indexes with `getIndexes()` and query plan with `explain()`. |

## Developer Verification Points

When adding a new MongoDB-backed feature:

1. Add a document class with `@Document`.
2. Add indexes for query fields.
3. Add a repository.
4. Add a service method with business validation.
5. Add DTO mapping.
6. Add tests for repository/service behavior.
7. Verify data in MongoDB after API calls.

## Interview Preparation

You should be able to explain:

- Difference between Spring Data repository and MongoDB itself.
- How `@Document`, `@Id`, `@Indexed`, and `@Version` work.
- How derived query methods are implemented by Spring Data.
- Why DTOs should be separate from document classes.
- Difference between `MongoRepository` and `MongoTemplate`.
- How optimistic locking helps with concurrent updates.
- How Spring Data auditing works.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| What does `MongoRepository` provide? | CRUD operations, pagination support, and derived query method support. |
| When would you use `MongoTemplate`? | For dynamic queries, aggregations, partial updates, or operations that are awkward as repository methods. |
| What does `@Version` do? | It adds optimistic locking so concurrent writes do not silently overwrite each other. |
| Why avoid exposing documents from controllers? | Persistence shape and API shape change for different reasons. DTOs keep the API stable. |
| How are indexes created? | Spring Data can create indexes from annotations when auto-index creation is enabled. |

## EventCart Takeaway

Spring Data MongoDB is where MongoDB becomes productive in Java. In EventCart it teaches repositories, document mapping, indexes, auditing, DTO separation, optimistic locking, and integration testing with real MongoDB.

