package com.demo.order.infrastructure;

import com.demo.catalog.application.responses.CatalogDto;
import com.demo.order.domain.Order;
import com.demo.order.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(
            countQuery = "SELECT count(o.id) FROM Order o",
            value = """
                    SELECT o FROM Order AS o
                    LEFT JOIN FETCH o.items AS oi
                    LEFT JOIN FETCH o.recipient AS r
                    LEFT JOIN FETCH oi.book AS b
                    """
    )
    Page<Order> findAllPage(Pageable pageable);

    List<Order> findByStatusAndCreatedAtLessThanEqual(OrderStatus status, LocalDateTime timestamp);
}
