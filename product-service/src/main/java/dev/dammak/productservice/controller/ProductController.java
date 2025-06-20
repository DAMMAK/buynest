package dev.dammak.productservice.controller;

import dev.dammak.productservice.dto.InventoryUpdateDto;
import dev.dammak.productservice.dto.ProductDto;
import dev.dammak.productservice.dto.ProductSearchDto;
import dev.dammak.productservice.service.InventoryService;
import dev.dammak.productservice.service.ProductService;
import dev.dammak.productservice.service.SearchService;
import dev.dammak.productservice.util.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "APIs for managing products, inventory, and product search")
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final SearchService searchService;
    private final FileUtil fileUtil;

    @GetMapping
    @Operation(
            summary = "Get all products with pagination",
            description = "Retrieve a paginated list of all products"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a specific product by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(
            summary = "Get product by SKU",
            description = "Retrieve a specific product by its Stock Keeping Unit (SKU)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved product",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<ProductDto> getProductBySku(
            @Parameter(description = "Product SKU", required = true)
            @PathVariable String sku) {
        ProductDto product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(
            summary = "Get products by category",
            description = "Retrieve products belonging to a specific category with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Page<ProductDto>> getProductsByCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long categoryId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDto> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    @Operation(
            summary = "Get featured products",
            description = "Retrieve all products marked as featured"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved featured products",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<ProductDto>> getFeaturedProducts() {
        List<ProductDto> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brands")
    @Operation(
            summary = "Get all brands",
            description = "Retrieve a list of all unique product brands"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved brands",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<String>> getAllBrands() {
        List<String> brands = productService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    @PostMapping("/search")
    @Operation(
            summary = "Advanced product search",
            description = "Search products using advanced criteria including filters and sorting"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search criteria",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @Parameter(description = "Search criteria", required = true)
            @RequestBody ProductSearchDto searchDto) {
        Page<ProductDto> products = searchService.searchProducts(searchDto);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Simple keyword search",
            description = "Search products by keyword with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Page<ProductDto>> searchByKeyword(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ProductDto> products = searchService.searchByKeyword(keyword, pageable);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @Operation(
            summary = "Create a new product",
            description = "Create a new product in the catalog"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Product SKU already exists",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<ProductDto> createProduct(
            @Parameter(description = "Product data", required = true)
            @Valid @RequestBody ProductDto productDto) {
        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an existing product",
            description = "Update an existing product by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "Product SKU already exists",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<ProductDto> updateProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated product data", required = true)
            @Valid @RequestBody ProductDto productDto) {
        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a product",
            description = "Delete a product by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload product images",
            description = "Upload multiple images for a specific product"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file format or size",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<String>> uploadProductImages(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Image files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files) {
        List<String> imageUrls = fileUtil.uploadProductImages(id, files);
        return ResponseEntity.ok(imageUrls);
    }

    @PutMapping("/inventory")
    @Operation(
            summary = "Update product inventory",
            description = "Update the inventory quantity for a product"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventory updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid inventory update data",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Void> updateInventory(
            @Parameter(description = "Inventory update data", required = true)
            @Valid @RequestBody InventoryUpdateDto updateDto) {
        inventoryService.updateInventory(updateDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/inventory/low-stock")
    @Operation(
            summary = "Get low stock products",
            description = "Retrieve products that are below their minimum stock level"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved low stock products",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<List<ProductDto>> getLowStockProducts() {
        List<ProductDto> products = inventoryService.getLowStockProducts()
                .stream()
                .map(product -> ProductDto.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .sku(product.getSku())
                        .stockQuantity(product.getStockQuantity())
                        .minStockLevel(product.getMinStockLevel())
                        .build())
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}/stock-check")
    @Operation(
            summary = "Check product stock availability",
            description = "Check if a product has sufficient stock for a given quantity"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock check completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "boolean"))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Boolean> checkStock(
            @Parameter(description = "Product ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Quantity to check", required = true)
            @RequestParam int quantity) {
        boolean inStock = inventoryService.isInStock(id, quantity);
        return ResponseEntity.ok(inStock);
    }
}