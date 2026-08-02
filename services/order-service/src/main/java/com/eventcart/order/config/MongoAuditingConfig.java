package com.eventcart.order.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables Spring Data MongoDB auditing for order documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
