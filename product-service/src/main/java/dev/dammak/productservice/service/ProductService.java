package dev.dammak.productservice.service;

import dev.dammak.productservice.dto.ProductDto;
import dev.dammak.productservice.dto.ProductSearchDto;
import dev.dammak.productservice.entity.Category;
import dev.dammak.productservice.entity.Product;
import dev.dammak.productservice.exception.ProductException;
import dev.dammak.productservice.mapper.ProductMapper;
import dev.dammak.productservice.repository.CategoryRepository;
import dev.dammak.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findByIdWithImagesAndTags(id)
                .orElseThrow(() -> new ProductException("Product not found with id: " + id));
        // Ensure all lazy collections are initialized before DTO conversion
        Hibernate.initialize(product.getImageUrls());
        Hibernate.initialize(product.getTags());
        Hibernate.initialize(product.getCategory());

        return productMapper.toDto(product);
    }

    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySkuWithTags(sku)
                .orElseThrow(() -> new ProductException("Product not found with SKU: " + sku));

        return productMapper.toDto(product);
    }

    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(productMapper::toDto);
    }

    public Page<ProductDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(productMapper::toDto);
    }

    public Page<ProductDto> searchProducts(ProductSearchDto searchDto) {
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(searchDto.getSortDirection()) ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                searchDto.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                searchDto.getPage(),
                searchDto.getSize(),
                sort
        );

        return productRepository.findProductsWithFilters(
                searchDto.getCategoryId(),
                searchDto.getBrand(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getKeyword(),
                pageable
        ).map(productMapper::toDto);
    }

    @Cacheable(value = "featuredProducts")
    public List<ProductDto> getFeaturedProducts() {
        List<Product> products = productRepository.findByFeaturedTrueAndActiveTrue();
        return productMapper.toDtoList(products);
    }

    public List<String> getAllBrands() {
        return productRepository.findDistinctBrands();
    }

    @CacheEvict(value = {"products", "featuredProducts"}, allEntries = true)
    public ProductDto createProduct(ProductDto productDto) {
        validateUniqueSkuForCreate(productDto.getSku());
        Category category = getCategoryById(productDto.getCategoryId());

        Product product = productMapper.toEntity(productDto);
        product.setCategory(category);
        product = productRepository.save(product);

        ProductDto createdProductDto = productMapper.toDto(product);
        publishProductEvent("product.created", createdProductDto);

        log.info("Created new product with id: {}", product.getId());
        return createdProductDto;
    }

    @CacheEvict(value = {"products", "featuredProducts"}, allEntries = true)
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = getProductEntityById(id);
        validateUniqueSkuForUpdate(existingProduct, productDto.getSku());
        Category category = getCategoryById(productDto.getCategoryId());

        productMapper.updateEntityFromDto(existingProduct, productDto);
        existingProduct.setCategory(category);
        existingProduct = productRepository.save(existingProduct);

        ProductDto updatedProductDto = productMapper.toDto(existingProduct);
        publishProductEvent("product.updated", updatedProductDto);

        log.info("Updated product with id: {}", existingProduct.getId());
        return updatedProductDto;
    }

    @CacheEvict(value = {"products", "featuredProducts"}, allEntries = true)
    public void deleteProduct(Long id) {
        Product product = getProductEntityById(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Soft deleted product with id: {}", id);
    }

    // Private helper methods
    private Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductException("Product not found with id: " + id));
    }

    private Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ProductException("Category not found with id: " + categoryId));
    }

    private void validateUniqueSkuForCreate(String sku) {
        if (productRepository.existsBySku(sku)) {
            throw new ProductException("Product with SKU '" + sku + "' already exists");
        }
    }

    private void validateUniqueSkuForUpdate(Product existingProduct, String newSku) {
        if (!existingProduct.getSku().equals(newSku) && productRepository.existsBySku(newSku)) {
            throw new ProductException("Product with SKU '" + newSku + "' already exists");
        }
    }

    private void publishProductEvent(String topic, ProductDto productDto) {
        try {
            kafkaTemplate.send(topic, productDto);
        } catch (Exception e) {
            log.error("Failed to publish product event to topic: {}", topic, e);
            // Consider whether to throw exception or just log
        }
    }
}