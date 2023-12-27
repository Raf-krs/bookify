package com.demo.order.domain;

import com.demo.shared.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @Singular
    private Set<OrderItem> items;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Recipient recipient;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Delivery delivery = Delivery.COURIER;

    @Builder.Default
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public UpdateStatusResult updateStatus(OrderStatus newStatus) {
        UpdateStatusResult result = this.status.updateStatus(newStatus);
        this.status = result.getNewStatus();
        return result;
    }

    public BigDecimal getItemsPrice() {
        return items.stream()
                    .map(item -> item.getBook()
                                     .getPrice()
                                     .toBigDecimal()
                                     .multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getDeliveryPrice() {
        if(items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return delivery.getPrice();
    }
}
