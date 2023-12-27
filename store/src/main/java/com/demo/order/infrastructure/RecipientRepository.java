package com.demo.order.infrastructure;

import com.demo.order.domain.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    Optional<Recipient> findByEmailIgnoreCase(String email);
}
