package com.demo.catalog.application.responses;

import java.math.BigDecimal;
import java.util.List;

public interface CatalogDto {
    long getId();
    String getTitle();
    long getAvailable();
    int getYear();
    Long getCoverId();
    BigDecimal getPrice();
    List<String> getAuthors();
}
