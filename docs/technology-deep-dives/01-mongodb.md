# MongoDB

MongoDB is the document database used by EventCart services. Each service owns its own database and collections so the system follows the microservice rule: a service owns its data and other services should not write directly into it.

## Where It Is Used

| Service | Database | Collections |
| --- | --- | --- |
| catalog-service | `eventcart_catalog` | `products` |
| cart-service | `eventcart_cart` | `carts` |
| order-service | `eventcart_order` | `orders`, `outbox_events` |
| inventory-service | `eventcart_inventory` | `inventory_items`, `inventory_reservations`, `outbox_events` |
| payment-service | `eventcart_payment` | `payment_attempts`, `outbox_events` |
| notification-service | `eventcart_notification` | `notifications` |

Local MongoDB is started from [compose.yaml](../../compose.yaml). The default credentials are:

```text
username: eventcart
password: eventcart
authSource: admin
port: 27017
```

Example local URI:

```text
mongodb://eventcart:eventcart@localhost:27017/eventcart_catalog?authSource=admin
```

## Why MongoDB Is Used

MongoDB fits this project because most service data is aggregate-oriented:

- Product documents are read as whole product records.
- Cart documents embed cart items because the cart is usually loaded and saved as one aggregate.
- Order documents store immutable item snapshots so order history is not affected when catalog data changes later.
- Inventory reservation documents store the reservation result for one order.
- Outbox documents store event payloads and publication status.
- Notification documents store customer notification history.

This is a good example of document modeling: store data together when it is read together, and split data when ownership or lifecycle differs.

## Important Concepts

| Concept | Meaning In EventCart |
| --- | --- |
| Database per service | Each service has its own MongoDB database. |
| Collection | Similar to a table, but stores flexible JSON-like documents. |
| Document | One aggregate record such as product, cart, order, or notification. |
| Embedded document | Child data stored inside the parent document, such as cart items. |
| Index | Data structure used to speed up lookups and enforce uniqueness. |
| ObjectId | MongoDB-generated document identifier. |
| Optimistic locking | Version field prevents silent overwrite of concurrent updates. |
| Outbox collection | Durable storage for events before Kafka publishing. |

## Best Practices

- Model documents around query patterns, not around normalized SQL-style tables.
- Keep service ownership strict. For example, order-service should not update catalog-service products directly.
- Store snapshots for historical records. Orders keep product name, SKU, price, and quantity at order time.
- Use indexes for frequent filters such as `customerId`, `sku`, `category`, `active`, and status fields.
- Use unique indexes for business keys such as product SKU and customer cart ownership.
- Avoid unbounded embedded arrays. Cart items are safe because carts are naturally small; audit logs would not be.
- Use optimistic locking when concurrent updates are possible.
- Keep money values as precise numeric types in Java, and avoid floating-point calculations in business logic.
- Do not expose MongoDB internals directly in API contracts. Return DTOs.
- Never store secrets in MongoDB documents unless encrypted and access-controlled.

## How To Verify Locally

Start MongoDB:

```powershell
docker compose up -d mongodb
docker compose ps mongodb
```

Connect with `mongosh`:

```powershell
docker exec -it eventcart-mongodb mongosh -u eventcart -p eventcart --authenticationDatabase admin
```

List databases:

```javascript
show dbs
```

Check products:

```javascript
use eventcart_catalog
db.products.find().pretty()
db.products.find({ sku: "SKU-1001" }).pretty()
```

Check a product by API ID. Spring Data may store a valid ObjectId-like string as an ObjectId, so try ObjectId first and then string if needed:

```javascript
db.products.findOne({ _id: ObjectId("<product-id>") })
db.products.findOne({ _id: "<product-id>" })
```

Check an order:

```javascript
use eventcart_order
db.orders.find({ customerId: "customer-1" }).pretty()
db.outbox_events.find({ aggregateId: "<order-id>" }).pretty()
```

Check inventory:

```javascript
use eventcart_inventory
db.inventory_items.find({ _id: ObjectId("<product-id>") }).pretty()
db.inventory_items.find({ _id: "<product-id>" }).pretty()
db.inventory_reservations.find({ orderId: "<order-id>" }).pretty()
db.outbox_events.find({ aggregateId: "<order-id>" }).pretty()
```

Check payments and notifications:

```javascript
use eventcart_payment
db.payment_attempts.find({ orderId: "<order-id>" }).pretty()
db.outbox_events.find({ aggregateId: "<order-id>" }).pretty()

use eventcart_notification
db.notifications.find({ customerId: "customer-1" }).pretty()
```

## How To Debug

Use these checks when behavior looks wrong:

| Symptom | Check |
| --- | --- |
| API returns product but Mongo query finds nothing | Query `_id` as both `ObjectId("<id>")` and `"<id>"`. |
| Order stuck at `CREATED` | Check `eventcart_order.outbox_events` and Kafka topics. |
| Inventory did not reserve | Check `eventcart_inventory.inventory_items` quantity and `inventory_reservations`. |
| Payment not created | Check whether reservation became `RESERVED`. Payment only runs after inventory success. |
| Duplicate order confusion | Check Redis idempotency key and `eventcart_order.orders`. |
| Notification missing | Check notification-service logs and `eventcart_notification.notifications`. |

Useful Mongo commands:

```javascript
db.collection.getIndexes()
db.collection.countDocuments()
db.collection.find().sort({ createdAt: -1 }).limit(5).pretty()
db.collection.find({ status: "PENDING" }).pretty()
db.collection.explain().find({ customerId: "customer-1" })
```

## Real-Time Monitoring

For local development:

- Use `docker logs eventcart-mongodb`.
- Use MongoDB Compass to inspect documents visually.
- Use service `/actuator/health` to confirm MongoDB health.
- Use service logs to correlate API calls with inserts and updates.

For production-style monitoring:

- Track connection pool usage.
- Track operation latency.
- Track slow queries.
- Track index usage.
- Track document growth for embedded arrays.
- Alert on primary unavailable, high replication lag, disk pressure, and slow query spikes.

## Interview Preparation

You should be able to explain:

- Why MongoDB was chosen for cart and order snapshots.
- Difference between embedding and referencing.
- Why each service owns its own database.
- How indexing affects query performance.
- Why optimistic locking is useful.
- Why the outbox pattern uses MongoDB before Kafka publishing.
- Why MongoDB is not a drop-in replacement for relational databases.

Common interview questions:

| Question | Good Answer |
| --- | --- |
| Why use MongoDB here? | The data is aggregate-oriented, flexible, and service-owned. Carts and orders are naturally document-shaped. |
| When would you not use MongoDB? | When the domain needs complex joins, strict cross-aggregate transactions, or strong relational constraints as the main access pattern. |
| What is an index? | A structure that speeds up reads and can enforce uniqueness, but costs storage and write overhead. |
| What is embedding? | Storing child objects inside the parent document. It is good when the child is usually read with the parent. |
| What is eventual consistency? | One service commits its local state, then other services catch up asynchronously through events. |

## EventCart Takeaway

MongoDB is not just a storage choice in EventCart. It teaches service data ownership, document modeling, snapshots, indexing, optimistic locking, and the transactional outbox pattern.

