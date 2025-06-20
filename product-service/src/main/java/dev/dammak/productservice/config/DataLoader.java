package dev.dammak.productservice.config;

import dev.dammak.productservice.dto.CategoryDto;
import dev.dammak.productservice.dto.ProductDto;
import dev.dammak.productservice.service.CategoryService;
import dev.dammak.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed-data.enabled", havingValue = "true", matchIfMissing = false)
public class DataLoader implements ApplicationRunner {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final Map<String, Long> categoryIdMap = new HashMap<>();

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting to load seed data...");

        try {
            loadCategories();
            loadProducts();
            log.info("Seed data loaded successfully!");
        } catch (Exception e) {
            log.error("Error loading seed data: ", e);
        }
    }

    private void loadCategories() {
        log.info("Loading categories...");

        // Root categories
        createCategory("Electronics", "Electronic devices and accessories", null, 1);
        createCategory("Clothing", "Fashion and apparel", null, 2);
        createCategory("Books", "Books and literature", null, 3);
        createCategory("Home & Garden", "Home improvement and garden supplies", null, 4);
        createCategory("Sports & Outdoors", "Sports equipment and outdoor gear", null, 5);
        createCategory("Automotive", "Car parts and accessories", null, 6);
        createCategory("Health & Beauty", "Health and beauty products", null, 7);
        createCategory("Toys & Games", "Toys and entertainment", null, 8);

        // Sub-categories
        createCategory("Smartphones", "Mobile phones and accessories", categoryIdMap.get("Electronics"), 1);
        createCategory("Laptops", "Portable computers", categoryIdMap.get("Electronics"), 2);
        createCategory("Tablets", "Tablet computers", categoryIdMap.get("Electronics"), 3);
        createCategory("Audio", "Headphones and speakers", categoryIdMap.get("Electronics"), 4);

        createCategory("Men's Clothing", "Clothing for men", categoryIdMap.get("Clothing"), 1);
        createCategory("Women's Clothing", "Clothing for women", categoryIdMap.get("Clothing"), 2);
        createCategory("Shoes", "Footwear for all", categoryIdMap.get("Clothing"), 3);

        createCategory("Fiction", "Fiction books", categoryIdMap.get("Books"), 1);
        createCategory("Non-Fiction", "Non-fiction books", categoryIdMap.get("Books"), 2);

        log.info("Categories loaded successfully");
    }

    private void createCategory(String name, String description, Long parentId, Integer displayOrder) {
        try {
            CategoryDto categoryDto = CategoryDto.builder()
                    .name(name)
                    .description(description)
                    .parentId(parentId)
                    .displayOrder(displayOrder)
                    .active(true)
                    .build();

            CategoryDto created = categoryService.createCategory(categoryDto);
            categoryIdMap.put(name, created.getId());
            log.debug("Created category: {}", name);
        } catch (Exception e) {
            log.warn("Category '{}' might already exist: {}", name, e.getMessage());
        }
    }

    private void loadProducts() {
        log.info("Loading products...");

        // Electronics - Smartphones
        createProduct("iPhone 15 Pro", "Latest iPhone with advanced camera system", "Apple",
                new BigDecimal("999.99"), new BigDecimal("949.99"), "IPH15PRO256", 50, 10,
                "Smartphones", Set.of("smartphone", "iOS", "premium"),
                "{\"storage\":\"256GB\",\"ram\":\"8GB\",\"camera\":\"48MP\"}",
                new BigDecimal("0.201"), true, true);

        createProduct("Samsung Galaxy S24", "Flagship Android smartphone", "Samsung",
                new BigDecimal("899.99"), null, "SGS24256", 75, 15,
                "Smartphones", Set.of("smartphone", "android", "flagship"),
                "{\"storage\":\"256GB\",\"ram\":\"12GB\",\"camera\":\"50MP\"}",
                new BigDecimal("0.195"), true, true);

        createProduct("Google Pixel 8", "Pure Android experience", "Google",
                new BigDecimal("699.99"), new BigDecimal("649.99"), "GPX8128", 30, 5,
                "Smartphones", Set.of("smartphone", "android", "pixel"),
                "{\"storage\":\"128GB\",\"ram\":\"8GB\",\"camera\":\"50MP\"}",
                new BigDecimal("0.187"), true, false);

        // Electronics - Laptops
        createProduct("MacBook Pro 14\"", "Professional laptop for creators", "Apple",
                new BigDecimal("1999.99"), null, "MBP14512", 25, 5,
                "Laptops", Set.of("laptop", "macOS", "professional"),
                "{\"cpu\":\"M3 Pro\",\"ram\":\"18GB\",\"storage\":\"512GB SSD\"}",
                new BigDecimal("1.6"), true, true);

        createProduct("Dell XPS 13", "Ultrabook for productivity", "Dell",
                new BigDecimal("1299.99"), new BigDecimal("1199.99"), "DXPS13512", 40, 8,
                "Laptops", Set.of("laptop", "windows", "ultrabook"),
                "{\"cpu\":\"Intel i7\",\"ram\":\"16GB\",\"storage\":\"512GB SSD\"}",
                new BigDecimal("1.2"), true, false);

        createProduct("ThinkPad X1 Carbon", "Business laptop", "Lenovo",
                new BigDecimal("1599.99"), null, "TPX1C1TB", 20, 3,
                "Laptops", Set.of("laptop", "business", "durable"),
                "{\"cpu\":\"Intel i7\",\"ram\":\"32GB\",\"storage\":\"1TB SSD\"}",
                new BigDecimal("1.1"), true, true);

        // Electronics - Audio
        createProduct("AirPods Pro 2", "Wireless earbuds with ANC", "Apple",
                new BigDecimal("249.99"), new BigDecimal("229.99"), "APP2GEN2", 100, 20,
                "Audio", Set.of("earbuds", "wireless", "anc"),
                "{\"battery\":\"6 hours\",\"anc\":\"Yes\",\"spatial_audio\":\"Yes\"}",
                new BigDecimal("0.056"), true, true);

        createProduct("Sony WH-1000XM5", "Premium noise-canceling headphones", "Sony",
                new BigDecimal("399.99"), new BigDecimal("349.99"), "SYWH1000XM5", 60, 12,
                "Audio", Set.of("headphones", "anc", "premium"),
                "{\"battery\":\"30 hours\",\"anc\":\"Yes\",\"driver\":\"30mm\"}",
                new BigDecimal("0.25"), true, true);

        // Clothing
        createProduct("Levi's 501 Jeans", "Classic straight-fit jeans", "Levi's",
                new BigDecimal("89.99"), null, "LV501W32L32", 80, 15,
                "Men's Clothing", Set.of("jeans", "denim", "classic"),
                "{\"fit\":\"straight\",\"material\":\"100% cotton\",\"size\":\"32x32\"}",
                new BigDecimal("0.6"), true, false);

        createProduct("Nike Air Max 90", "Iconic running shoes", "Nike",
                new BigDecimal("119.99"), new BigDecimal("99.99"), "NAM90WHT42", 120, 25,
                "Shoes", Set.of("shoes", "sneakers", "running"),
                "{\"size\":\"US 9\",\"color\":\"White\",\"material\":\"Leather/Mesh\"}",
                new BigDecimal("0.8"), true, true);

        createProduct("Zara Blazer", "Professional women's blazer", "Zara",
                new BigDecimal("79.99"), new BigDecimal("59.99"), "ZBLZRM001", 45, 8,
                "Women's Clothing", Set.of("blazer", "professional", "formal"),
                "{\"size\":\"M\",\"color\":\"Navy\",\"material\":\"Polyester blend\"}",
                new BigDecimal("0.4"), true, false);

        // Books
        createProduct("The Great Gatsby", "Classic American novel", "Penguin Classics",
                new BigDecimal("12.99"), null, "TGG001", 200, 50,
                "Fiction", Set.of("classic", "american", "literature"),
                "{\"pages\":\"180\",\"format\":\"Paperback\",\"language\":\"English\"}",
                new BigDecimal("0.2"), true, false);

        createProduct("Atomic Habits", "Transform your life with tiny changes", "Random House",
                new BigDecimal("18.99"), new BigDecimal("16.99"), "ATHAB001", 150, 30,
                "Non-Fiction", Set.of("self-help", "productivity", "habits"),
                "{\"pages\":\"320\",\"format\":\"Hardcover\",\"language\":\"English\"}",
                new BigDecimal("0.45"), true, true);

        // Home & Garden
        createProduct("Instant Pot Duo", "7-in-1 pressure cooker", "Instant Pot",
                new BigDecimal("99.99"), new BigDecimal("79.99"), "IPDUO7QT", 90, 18,
                "Home & Garden", Set.of("kitchen", "appliance", "pressure-cooker"),
                "{\"capacity\":\"6 quarts\",\"functions\":\"7\",\"material\":\"Stainless steel\"}",
                new BigDecimal("5.5"), true, true);

        createProduct("Dyson V15 Detect", "Cordless vacuum cleaner", "Dyson",
                new BigDecimal("749.99"), new BigDecimal("699.99"), "DV15DET", 35, 7,
                "Home & Garden", Set.of("vacuum", "cordless", "premium"),
                "{\"battery\":\"60 minutes\",\"filtration\":\"HEPA\",\"display\":\"LCD\"}",
                new BigDecimal("3.1"), true, true);

        // Sports & Outdoors
        createProduct("Yoga Mat Premium", "Non-slip exercise mat", "Manduka",
                new BigDecimal("89.99"), null, "YMPREM6MM", 70, 14,
                "Sports & Outdoors", Set.of("yoga", "exercise", "fitness"),
                "{\"thickness\":\"6mm\",\"material\":\"Natural rubber\",\"size\":\"71x24\"}",
                new BigDecimal("2.5"), true, false);

        createProduct("Hydro Flask 32oz", "Insulated water bottle", "Hydro Flask",
                new BigDecimal("44.99"), new BigDecimal("39.99"), "HF32OZBLK", 100, 20,
                "Sports & Outdoors", Set.of("water-bottle", "insulated", "outdoor"),
                "{\"capacity\":\"32oz\",\"insulation\":\"TempShield\",\"color\":\"Black\"}",
                new BigDecimal("0.5"), true, true);

        // Automotive
        createProduct("Michelin Pilot Sport 4", "High-performance tire", "Michelin",
                new BigDecimal("289.99"), null, "MPS4225/45R17", 40, 8,
                "Automotive", Set.of("tire", "performance", "summer"),
                "{\"size\":\"225/45R17\",\"speed_rating\":\"Y\",\"compound\":\"Bi-compound\"}",
                new BigDecimal("8.5"), true, false);

        createProduct("Garmin DashCam 67W", "Wide-angle dash camera", "Garmin",
                new BigDecimal("199.99"), new BigDecimal("179.99"), "GDC67W", 55, 11,
                "Automotive", Set.of("dashcam", "camera", "safety"),
                "{\"resolution\":\"1440p\",\"field_of_view\":\"180°\",\"gps\":\"Yes\"}",
                new BigDecimal("0.15"), true, true);

        // Health & Beauty
        createProduct("Olaplex No.3 Treatment", "Hair repair treatment", "Olaplex",
                new BigDecimal("28.99"), null, "OLPX3100ML", 80, 16,
                "Health & Beauty", Set.of("hair-care", "treatment", "repair"),
                "{\"volume\":\"100ml\",\"hair_type\":\"All\",\"application\":\"Weekly\"}",
                new BigDecimal("0.12"), true, true);

        log.info("Products loaded successfully");
    }

    private void createProduct(String name, String description, String brand, BigDecimal price,
                               BigDecimal discountPrice, String sku, Integer stockQuantity,
                               Integer minStockLevel, String categoryName, Set<String> tags,
                               String specifications, BigDecimal weight, Boolean active, Boolean featured) {
        try {
            Long categoryId = categoryIdMap.get(categoryName);
            if (categoryId == null) {
                log.warn("Category '{}' not found for product '{}'", categoryName, name);
                return;
            }

            ProductDto productDto = ProductDto.builder()
                    .name(name)
                    .description(description)
                    .brand(brand)
                    .price(price)
                    .discountPrice(discountPrice)
                    .sku(sku)
                    .stockQuantity(stockQuantity)
                    .minStockLevel(minStockLevel)
                    .active(active)
                    .featured(featured)
                    .categoryId(categoryId)
                    .tags(tags)
                    .specifications(specifications)
                    .weight(weight)
                    .length(new BigDecimal("10.0"))  // Default dimensions
                    .width(new BigDecimal("5.0"))
                    .height(new BigDecimal("2.0"))
                    .imageUrls(List.of(
                            "https://example.com/images/" + sku.toLowerCase() + "_1.jpg",
                            "https://example.com/images/" + sku.toLowerCase() + "_2.jpg"
                    ))
                    .build();

            productService.createProduct(productDto);
            log.debug("Created product: {}", name);
        } catch (Exception e) {
            log.warn("Product '{}' might already exist: {}", name, e.getMessage());
        }
    }
}