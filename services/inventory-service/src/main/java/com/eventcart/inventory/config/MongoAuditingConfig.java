package com.eventcart.inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables Spring Data MongoDB auditing for inventory documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
