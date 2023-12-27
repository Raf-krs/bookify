package com.demo.order.web;

import com.demo.order.application.OrderStatusRequest;
import com.demo.order.application.RichOrder;
import com.demo.order.application.placeOrder.commands.PlaceOrderCommand;
import com.demo.order.application.placeOrder.commands.UpdateStatusCommand;
import com.demo.order.application.services.ManipulateOrderService;
import com.demo.order.application.services.QueryOrderService;
import com.demo.order.domain.OrderStatus;
import com.demo.security.UserSecurity;
import com.demo.shared.web.CreatedUri;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@AllArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Orders API")
class OrderController {
    private final ManipulateOrderService manipulateOrder;
    private final QueryOrderService queryOrder;
    private final UserSecurity userSecurity;

    @Operation(
            summary = "Get orders",
            description = "Get orders by status, page and page size. Secured endpoint, only for admins"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Get orders",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @GetMapping
    public Page<RichOrder> getOrders(
            @RequestParam(required = false, name = "status") Optional<String> status,
            @RequestParam(required = false, name = "page") Optional<Integer> page,
            @RequestParam(required = false, name = "pageSize") Optional<Integer> pageSize
    ) {
        return queryOrder.findAll(new OrderRequestParams(status, page, pageSize));
    }

    @Operation(
            summary = "Get order",
            description = "Get order by id. Secured endpoint, only for admins and owners"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Get order",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RichOrder.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<RichOrder> getOrderById(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        return queryOrder.findById(id)
                         .map(order -> authorize(order, user))
                         .orElse(ResponseEntity.notFound().build());
    }

    private ResponseEntity<RichOrder> authorize(RichOrder order, UserDetails user) {
        if (userSecurity.isOwnerOrAdmin(order.recipient().getEmail(), user)) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.status(FORBIDDEN).build();
    }

    @Operation(
            summary = "Create order",
            description = "Get order by id. Secured endpoint, only for admins and owners"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Get order",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity createOrder(@RequestBody PlaceOrderCommand command) {
        return manipulateOrder
                .placeOrder(command)
                .handle(
                        orderId -> ResponseEntity.created(orderUri(orderId)).build(),
                        error -> ResponseEntity.badRequest().body(error)
                );
    }

    private URI orderUri(Long orderId) {
        return new CreatedUri("/" + orderId).uri();
    }

    @Operation(
            summary = "Update order status",
            description = "Update order status by id. Secured endpoint for admins and owners"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "202",
                    description = "Update order status",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not found",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Object> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        OrderStatus orderStatus = OrderStatus
                .parseString(request.status())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Unknown status: " + request.status()));
        UpdateStatusCommand command = new UpdateStatusCommand(id, orderStatus, user);
        return manipulateOrder.updateOrderStatus(command)
                              .handle(
                                      newStatus -> ResponseEntity.accepted().build(),
                                      error -> ResponseEntity.status(error.getStatus()).build()
                              );
    }

    @Operation(
            summary = "Delete order",
            description = "Delete order by id. Secured endpoint for admins"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Delete order",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(
                            mediaType = "application/problem+json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @Secured("ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        manipulateOrder.deleteOrderById(id);
    }
}
