package dev.dammak.productservice.util;

import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public final class SearchUtil {

    private SearchUtil() {
        // Private constructor to prevent instantiation
    }

    public static Sort createSort(String sortBy, String sortDirection) {
        String validatedSortBy = validateAndGetSortField(sortBy);
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, validatedSortBy);
    }

    public static String validateAndGetSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "createdAt"; // Default sort field
        }

        // Validate sort field to prevent injection attacks
        return switch (sortBy.toLowerCase()) {
            case "name" -> "name";
            case "price" -> "price";
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            case "brand" -> "brand";
            case "stockquantity", "stock_quantity" -> "stockQuantity";
            default -> "createdAt";
        };
    }

    public static void validatePriceRange(Double minPrice, Double maxPrice) {
        if (minPrice != null && minPrice < 0) {
            throw new IllegalArgumentException("Minimum price cannot be negative");
        }

        if (maxPrice != null && maxPrice < 0) {
            throw new IllegalArgumentException("Maximum price cannot be negative");
        }

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
    }
}