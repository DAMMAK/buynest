package dev.dammak.productservice.mapper;


import dev.dammak.productservice.dto.ProductDto;
import dev.dammak.productservice.entity.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .sku(product.getSku())
                .stockQuantity(product.getStockQuantity())
                .minStockLevel(product.getMinStockLevel())
                .active(product.getActive())
                .featured(product.getFeatured())
                .imageUrls(new ArrayList<>(product.getImageUrls()))
                .tags(product.getTags())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .specifications(product.getSpecifications())
                .weight(product.getWeight())
                .length(product.getLength())
                .width(product.getWidth())
                .height(product.getHeight())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public Product toEntity(ProductDto dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .brand(dto.getBrand())
                .price(dto.getPrice())
                .discountPrice(dto.getDiscountPrice())
                .sku(dto.getSku())
                .stockQuantity(dto.getStockQuantity())
                .minStockLevel(dto.getMinStockLevel() != null ? dto.getMinStockLevel() : 5)
                .active(dto.getActive() != null ? dto.getActive() : true)
                .featured(dto.getFeatured() != null ? dto.getFeatured() : false)
                .imageUrls(dto.getImageUrls())
                .tags(dto.getTags())
                .specifications(dto.getSpecifications())
                .weight(dto.getWeight())
                .length(dto.getLength())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .build();
    }

    public Product toEntityWithId(ProductDto dto) {
        if (dto == null) {
            return null;
        }

        Product product = toEntity(dto);
        product.setId(dto.getId());
        return product;
    }

    public void updateEntityFromDto(Product existingProduct, ProductDto dto) {
        if (existingProduct == null || dto == null) {
            return;
        }

        existingProduct.setName(dto.getName());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setBrand(dto.getBrand());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setDiscountPrice(dto.getDiscountPrice());
        existingProduct.setSku(dto.getSku());
        existingProduct.setStockQuantity(dto.getStockQuantity());
        existingProduct.setMinStockLevel(dto.getMinStockLevel() != null ? dto.getMinStockLevel() : existingProduct.getMinStockLevel());
        existingProduct.setActive(dto.getActive() != null ? dto.getActive() : existingProduct.getActive());
        existingProduct.setFeatured(dto.getFeatured() != null ? dto.getFeatured() : existingProduct.getFeatured());
        existingProduct.setImageUrls(dto.getImageUrls());
        existingProduct.setTags(dto.getTags());
        existingProduct.setSpecifications(dto.getSpecifications());
        existingProduct.setWeight(dto.getWeight());
        existingProduct.setLength(dto.getLength());
        existingProduct.setWidth(dto.getWidth());
        existingProduct.setHeight(dto.getHeight());
    }

    public List<ProductDto> toDtoList(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Product> toEntityList(List<ProductDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
