package com.demo.catalog.domain;

import com.demo.shared.jpa.BaseEntity;
import com.demo.shared.money.Money;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString(exclude = "authors")
@RequiredArgsConstructor
public class Book extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String title;

    @Column(nullable = false)
    private Integer year;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "price"))
    })
    @Column(nullable = false, precision = 10, scale = 2)
    private Money price;

    @Column(nullable = false)
    private Long coverId;

    @Column(nullable = false)
    private Long available;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("books")
    private Set<Author> authors = new HashSet<>();

    public Book(String title, Integer year, BigDecimal price, Long available) {
        this.title = title;
        this.year = year;
        this.price = new Money(price);
        this.available = available;
    }

    public void addAuthor(Author author) {
        authors.add(author);
        author.getBooks().add(this);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
        author.getBooks().remove(this);
    }

    public void removeAuthors() {
        authors.forEach(this::removeAuthor);
        authors.clear();
    }
}
