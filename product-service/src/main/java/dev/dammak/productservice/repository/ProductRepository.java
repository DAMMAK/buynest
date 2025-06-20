package dev.dammak.productservice.repository;


import dev.dammak.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Replace the problematic method with @EntityGraph
    @EntityGraph(attributePaths = {"imageUrls", "tags"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithImagesAndTags(@Param("id") Long id);

    // Also update the SKU method
    @EntityGraph(attributePaths = {"imageUrls", "tags"})
    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Optional<Product> findBySkuWithTags(@Param("sku") String sku);

    // Keep your other methods as they are...
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.imageUrls WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByPriceBetweenAndActiveTrue(BigDecimal price, BigDecimal price2, Pageable pageable);


    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByBrandIgnoreCaseAndActiveTrue(String brand, Pageable pageable);

    List<Product> findByFeaturedTrueAndActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.stockQuantity < p.minStockLevel")
    List<Product> findByStockQuantityLessThanMinStockLevel();

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
            "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
            "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findProductsWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.active = true ORDER BY p.brand")
    List<String> findDistinctBrands();

    boolean existsBySku(String sku);
}