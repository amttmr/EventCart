package com.eventcart.order.controller;

import com.eventcart.common.web.ApiResponse;
import com.eventcart.order.dto.OrderResponse;
import com.eventcart.order.dto.PlaceOrderRequest;
import com.eventcart.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller that exposes order APIs.
 */
@Tag(name = "Orders", description = "Order placement and lookup APIs")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    /**
     * Creates an order controller.
     *
     * @param orderService order business service
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Places an order from the customer's current cart.
     *
     * @param request validated place-order request
     * @return created order response
     */
    @Operation(summary = "Place order", description = "Creates an order from the customer's current cart and publishes OrderCreated to Kafka.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order placed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cart is empty"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Cart service unavailable")
    })
    @PostMapping
    public ApiResponse<OrderResponse> placeOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer whose cart should be converted into an order",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Place order for demo customer",
                                            summary = "Uses the cart for customer-1",
                                            value = """
                                                    {
                                                      "customerId": "customer-1"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Place order for another customer",
                                            summary = "Same API for any customer cart",
                                            value = """
                                                    {
                                                      "customerId": "customer-2"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            @Valid @RequestBody PlaceOrderRequest request
    ) {
        return ApiResponse.success(orderService.placeOrder(request), "Order placed");
    }

    /**
     * Retrieves one order by ID.
     *
     * @param orderId order ID
     * @return order response
     */
    @Operation(summary = "Get order", description = "Returns one order by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @Parameter(description = "Order ID", example = "66b1f9f48f8c1c4df8f8a001") @PathVariable String orderId
    ) {
        return ApiResponse.success(orderService.getOrder(orderId));
    }

    /**
     * Retrieves customer orders.
     *
     * @param customerId customer ID
     * @return customer orders
     */
    @Operation(summary = "List customer orders", description = "Returns orders for one customer, newest first.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders returned")
    @GetMapping("/customer/{customerId}")
    public ApiResponse<List<OrderResponse>> getOrdersForCustomer(
            @Parameter(description = "Customer ID", example = "customer-1") @PathVariable String customerId
    ) {
        return ApiResponse.success(orderService.getOrdersForCustomer(customerId));
    }
}
