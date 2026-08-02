package com.eventcart.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables MongoDB auditing fields for notification documents.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
