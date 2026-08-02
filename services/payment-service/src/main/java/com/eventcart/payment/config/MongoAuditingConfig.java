package com.eventcart.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables Spring Data MongoDB auditing for payment documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
