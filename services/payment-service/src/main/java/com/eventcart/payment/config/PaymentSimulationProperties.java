package com.eventcart.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Configuration properties that control the mock payment provider behavior.
 *
 * @param providerName mock payment provider name
 * @param failureAmountThreshold amount at or above which the mock provider declines payment
 */
@ConfigurationProperties(prefix = "eventcart.payment.simulation")
public record PaymentSimulationProperties(
        String providerName,
        BigDecimal failureAmountThreshold
) {
    /**
     * Creates payment simulation properties with local defaults.
     *
     * @param providerName mock payment provider name
     * @param failureAmountThreshold failure threshold
     */
    public PaymentSimulationProperties {
        if (providerName == null || providerName.isBlank()) {
            providerName = "MockPay";
        }
        if (failureAmountThreshold == null) {
            failureAmountThreshold = new BigDecimal("50000.00");
        }
    }
}
