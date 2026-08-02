package com.eventcart.cart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables Spring Data MongoDB auditing for cart documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}

