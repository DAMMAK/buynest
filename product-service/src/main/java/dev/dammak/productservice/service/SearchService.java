package dev.dammak.productservice.service;

import dev.dammak.productservice.dto.ProductDto;
import dev.dammak.productservice.dto.ProductSearchDto;
import dev.dammak.productservice.mapper.ProductMapper;
import dev.dammak.productservice.repository.ProductRepository;
import dev.dammak.productservice.util.SearchUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import static dev.dammak.productservice.util.SearchUtil.createSort;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public Page<ProductDto> searchProducts(ProductSearchDto searchDto) {
        validateSearchRequest(searchDto);

        Sort sort = createSort(searchDto.getSortBy(), searchDto.getSortDirection());
        Pageable pageable = createPageable(searchDto, sort);

        log.debug("Searching products with filters: categoryId={}, brand={}, minPrice={}, maxPrice={}, keyword={}",
                searchDto.getCategoryId(), searchDto.getBrand(), searchDto.getMinPrice(),
                searchDto.getMaxPrice(), searchDto.getKeyword());

        return productRepository.findProductsWithFilters(
                searchDto.getCategoryId(),
                searchDto.getBrand(),
                searchDto.getMinPrice(),
                searchDto.getMaxPrice(),
                searchDto.getKeyword(),
                pageable
        ).map(productMapper::toDto);
    }

    public Page<ProductDto> searchByKeyword(String keyword, Pageable pageable) {
        validateKeywordSearch(keyword);

        log.debug("Searching products by keyword: {}", keyword);

        return productRepository.searchProducts(keyword, pageable)
                .map(productMapper::toDto);
    }

    public Page<ProductDto> searchByCategory(Long categoryId, Pageable pageable) {
        validateCategorySearch(categoryId);

        log.debug("Searching products by category: {}", categoryId);

        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(productMapper::toDto);
    }

    public Page<ProductDto> searchByBrand(String brand, Pageable pageable) {
        validateBrandSearch(brand);

        log.debug("Searching products by brand: {}", brand);

        return productRepository.findByBrandIgnoreCaseAndActiveTrue(brand, pageable)
                .map(productMapper::toDto);
    }

    public Page<ProductDto> searchByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        validatePriceRange(minPrice, maxPrice);

        log.debug("Searching products by price range: {} - {}", minPrice, maxPrice);

        return productRepository.findByPriceBetweenAndActiveTrue(BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice), pageable)
                .map(productMapper::toDto);
    }



    private Pageable createPageable(ProductSearchDto searchDto, Sort sort) {
        int page = Math.max(0, searchDto.getPage()); // Ensure page is not negative
        int size = Math.min(Math.max(1, searchDto.getSize()), 100); // Limit size between 1 and 100

        return PageRequest.of(page, size, sort);
    }

    private void validateSearchRequest(ProductSearchDto searchDto) {
        if (searchDto == null) {
            throw new IllegalArgumentException("Search request cannot be null");
        }

        if (searchDto.getMinPrice() != null && searchDto.getMaxPrice() != null) {
            validatePriceRange(searchDto.getMinPrice().doubleValue(), searchDto.getMaxPrice().doubleValue());
        }
    }

    private void validateKeywordSearch(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("Search keyword cannot be null or empty");
        }

        if (keyword.trim().length() < 2) {
            throw new IllegalArgumentException("Search keyword must be at least 2 characters long");
        }
    }

    private void validateCategorySearch(Long categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive number");
        }
    }

    private void validateBrandSearch(String brand) {
        if (!StringUtils.hasText(brand)) {
            throw new IllegalArgumentException("Brand cannot be null or empty");
        }
    }

    private void validatePriceRange(Double minPrice, Double maxPrice) {
        SearchUtil.validatePriceRange(minPrice, maxPrice);
    }
}