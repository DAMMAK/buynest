package dev.dammak.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product search criteria")
public class ProductSearchDto {

    @Schema(description = "Search keyword", example = "iPhone")
    private String keyword;

    @Schema(description = "Category ID to filter by", example = "1")
    private Long categoryId;

    @Schema(description = "Brand to filter by", example = "Apple")
    private String brand;

    @Schema(description = "Minimum price filter", example = "100.00")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price filter", example = "2000.00")
    private BigDecimal maxPrice;

    @Schema(description = "Filter for featured products only", example = "false")
    private Boolean featured;

    @Schema(description = "Filter for products in stock only", example = "true")
    private Boolean inStock;

    @Schema(description = "Sort field", example = "name", allowableValues = {"name", "price", "createdAt", "brand"})
    private String sortBy = "name";

    @Schema(description = "Sort direction", example = "asc", allowableValues = {"asc", "desc"})
    private String sortDirection = "asc";

    @Schema(description = "Page number (0-based)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "20")
    private Integer size = 20;
}