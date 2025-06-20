package dev.dammak.productservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.dammak.productservice.mapper.LocalDateTimeDeserializer;
import dev.dammak.productservice.mapper.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product data transfer object")
public class ProductDto {

    @Schema(description = "Unique identifier of the product", example = "1")
    private Long id;

    @Schema(description = "Name of the product", example = "iPhone 15 Pro", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 255, message = "Product name must be between 2 and 255 characters")
    private String name;

    @Schema(description = "Detailed description of the product", example = "Latest iPhone with advanced camera system")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Schema(description = "Brand of the product", example = "Apple", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Brand is required")
    private String brand;

    @Schema(description = "Price of the product", example = "999.99", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Schema(description = "Discounted price of the product", example = "899.99")
    @DecimalMin(value = "0.01", message = "Discount price must be greater than 0")
    private BigDecimal discountPrice;

    @Schema(description = "Stock Keeping Unit", example = "IPH15PRO256GB", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "SKU is required")
    private String sku;

    @Schema(description = "Current stock quantity", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Schema(description = "Minimum stock level threshold", example = "10")
    @Min(value = 0, message = "Minimum stock level cannot be negative")
    private Integer minStockLevel;

    @Schema(description = "Whether the product is active", example = "true")
    private Boolean active;

    @Schema(description = "Whether the product is featured", example = "false")
    private Boolean featured;

    @Schema(description = "List of product image URLs")
    private List<String> imageUrls;

    @Schema(description = "List of product tags")
    private Set<String> tags;

    @Schema(description = "Category ID the product belongs to", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Schema(description = "Category name", example = "Smartphones")
    private String categoryName;

    @Schema(description = "Product specifications in JSON format")
    private String specifications;

    @Schema(description = "Product weight in kilograms", example = "0.201")
    @DecimalMin(value = "0.001", message = "Weight must be positive")
    private BigDecimal weight;

    @Schema(description = "Product length in centimeters", example = "15.9")
    @DecimalMin(value = "0.01", message = "Length must be positive")
    private BigDecimal length;

    @Schema(description = "Product width in centimeters", example = "7.6")
    @DecimalMin(value = "0.01", message = "Width must be positive")
    private BigDecimal width;

    @Schema(description = "Product height in centimeters", example = "0.78")
    @DecimalMin(value = "0.01", message = "Height must be positive")
    private BigDecimal height;

    @Schema(description = "Product creation timestamp")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @Schema(description = "Product last update timestamp")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}