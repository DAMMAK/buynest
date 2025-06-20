package dev.dammak.orderservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private List<String> imageUrls;
    private String category;
    private Boolean active;
    private Integer stockQuantity;
}