package com.eventcart.catalog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables Spring Data MongoDB auditing for catalog documents.
 *
 * <p>With this configuration active, fields annotated with
 * {@code @CreatedDate} and {@code @LastModifiedDate} are maintained
 * automatically by Spring Data.</p>
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
