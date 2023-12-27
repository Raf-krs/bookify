package com.demo.order.application.services;

import com.demo.catalog.domain.Book;
import com.demo.catalog.infrastructure.BookRepository;
import com.demo.order.application.exceptions.OutOfStockException;
import com.demo.order.application.placeOrder.Error;
import com.demo.order.application.placeOrder.commands.OrderItemCommand;
import com.demo.order.application.placeOrder.commands.PlaceOrderCommand;
import com.demo.order.application.placeOrder.commands.UpdateStatusCommand;
import com.demo.order.application.placeOrder.responses.PlaceOrderResponse;
import com.demo.order.application.placeOrder.responses.UpdateStatusResponse;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderItem;
import com.demo.order.domain.Recipient;
import com.demo.order.domain.UpdateStatusResult;
import com.demo.order.infrastructure.OrderRepository;
import com.demo.order.infrastructure.RecipientRepository;
import com.demo.security.UserSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ManipulateOrderService {
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final RecipientRepository recipientRepository;
    private final UserSecurity userSecurity;

    public PlaceOrderResponse placeOrder(PlaceOrderCommand command) {
        Set<OrderItem> items = command
                .getItems()
                .stream()
                .map(this::toOrderItem)
                .collect(Collectors.toSet());
        Order order = Order
                .builder()
                .recipient(getOrCreateRecipient(command.getRecipient()))
                .delivery(command.getDelivery())
                .items(items)
                .build();
        Order savedOrder = orderRepository.save(order);
        bookRepository.saveAll(reduceBooks(items));
        return PlaceOrderResponse.success(savedOrder.getId());
    }

    private Recipient getOrCreateRecipient(Recipient recipient) {
        return recipientRepository
                .findByEmailIgnoreCase(recipient.getEmail())
                .orElse(recipient);
    }

    private Set<Book> reduceBooks(Set<OrderItem> items) {
        return items
                .stream()
                .map(item -> {
                    Book book = item.getBook();
                    book.setAvailable(book.getAvailable() - item.getQuantity());
                    return book;
                })
                .collect(Collectors.toSet());
    }

    private OrderItem toOrderItem(OrderItemCommand command) {
        Book book = bookRepository.getReferenceById(command.bookId());
        int quantity = command.quantity();
        if (book.getAvailable() >= quantity) {
            return new OrderItem(book, quantity);
        }
        throw new OutOfStockException(book.getId(), quantity, book.getAvailable());
    }

    public void deleteOrderById(Long id) {
        orderRepository.deleteById(id);
    }

    public UpdateStatusResponse updateOrderStatus(UpdateStatusCommand command) {
        return orderRepository
                .findById(command.orderId())
                .map(order -> {
                    if(userSecurity.isOwnerOrAdmin(order.getRecipient().getEmail(), command.user())) {
                        UpdateStatusResult result = order.updateStatus(command.status());
                        if (result.isRevoked()) {
                            bookRepository.saveAll(revokeBooks(order.getItems()));
                        }
                        orderRepository.save(order);
                        return UpdateStatusResponse.success(order.getStatus());
                    }
                    return UpdateStatusResponse.failure(Error.FORBIDDEN);
                })
                .orElse(UpdateStatusResponse.failure(Error.NOT_FOUND));
    }

    private Set<Book> revokeBooks(Set<OrderItem> items) {
        return items
                .stream()
                .map(item -> {
                    Book book = item.getBook();
                    book.setAvailable(book.getAvailable() + item.getQuantity());
                    return book;
                })
                .collect(Collectors.toSet());
    }
}
