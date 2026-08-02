package com.eventcart.payment.controller;

import com.eventcart.common.web.ApiResponse;
import com.eventcart.payment.dto.PaymentAttemptResponse;
import com.eventcart.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes payment lookup APIs.
 */
@Tag(name = "Payments", description = "Payment attempt lookup APIs")
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService paymentService;

    /**
     * Creates a payment controller.
     *
     * @param paymentService payment business service
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Retrieves the payment attempt for one order.
     *
     * @param orderId order ID
     * @return payment attempt response
     */
    @Operation(summary = "Get payment by order", description = "Returns the payment attempt created after inventory was reserved.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payment attempt returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payment attempt not found")
    })
    @GetMapping("/orders/{orderId}")
    public ApiResponse<PaymentAttemptResponse> getPaymentAttemptForOrder(
            @Parameter(description = "Order ID", example = "66b1f9f48f8c1c4df8f8a001") @PathVariable String orderId
    ) {
        return ApiResponse.success(paymentService.getPaymentAttemptForOrder(orderId));
    }
}
