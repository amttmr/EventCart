package com.eventcart.notification.controller;

import com.eventcart.common.web.ApiResponse;
import com.eventcart.notification.dto.NotificationResponse;
import com.eventcart.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller that exposes customer notification APIs.
 */
@Tag(name = "Notifications", description = "Customer notification lookup APIs")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Creates a notification controller.
     *
     * @param notificationService notification business service
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Lists notifications for one customer.
     *
     * @param customerId customer ID
     * @return customer notifications
     */
    @Operation(summary = "List customer notifications", description = "Returns notifications created from order and payment events.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notifications returned")
    @GetMapping("/customers/{customerId}")
    public ApiResponse<List<NotificationResponse>> getNotificationsForCustomer(
            @Parameter(description = "Customer ID", example = "customer-1") @PathVariable String customerId
    ) {
        return ApiResponse.success(notificationService.getNotificationsForCustomer(customerId));
    }

    /**
     * Retrieves one notification.
     *
     * @param notificationId notification ID
     * @return notification response
     */
    @Operation(summary = "Get notification", description = "Returns one notification by ID.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @GetMapping("/{notificationId}")
    public ApiResponse<NotificationResponse> getNotification(
            @Parameter(description = "Notification ID", example = "66b1f9f48f8c1c4df8f8a222") @PathVariable String notificationId
    ) {
        return ApiResponse.success(notificationService.getNotification(notificationId));
    }

    /**
     * Marks a notification as read.
     *
     * @param notificationId notification ID
     * @return updated notification response
     */
    @Operation(summary = "Mark notification read", description = "Marks one in-app notification as read.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Notification updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markRead(
            @Parameter(description = "Notification ID", example = "66b1f9f48f8c1c4df8f8a222") @PathVariable String notificationId
    ) {
        return ApiResponse.success(notificationService.markRead(notificationId), "Notification marked read");
    }
}
