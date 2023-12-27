package com.demo.shared.money;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;

@Embeddable
public class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private BigDecimal value;

    public Money() {}

    public Money(BigDecimal value) {
        this.value = value;
    }

    public Money add(Money other) {
        return new Money(value.add(other.value));
    }

    public Money subtract(Money other) {
        return new Money(value.subtract(other.value));
    }

    public Money percentage(int percentage) {
        var result = value.multiply(new BigDecimal(percentage))
                          .divide(new BigDecimal(100), RoundingMode.HALF_UP);
        return new Money(result);
    }

    public Money percentage(Double percentage) {
        var result = value.multiply(new BigDecimal(percentage))
                          .divide(new BigDecimal(100), RoundingMode.HALF_UP);
        return new Money(result);
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return value.equals(money.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.2f", value);
    }
}
