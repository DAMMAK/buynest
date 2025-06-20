package dev.dammak.productservice.service;

import dev.dammak.productservice.dto.InventoryUpdateDto;
import dev.dammak.productservice.entity.Product;
import dev.dammak.productservice.exception.ProductException;
import dev.dammak.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @CacheEvict(value = "products", key = "#updateDto.productId")
    public void updateInventory(InventoryUpdateDto updateDto) {
        Product product = productRepository.findById(updateDto.getProductId())
                .orElseThrow(() -> new ProductException("Product not found with id: " + updateDto.getProductId()));

        int oldQuantity = product.getStockQuantity();
        int newQuantity = calculateNewQuantity(oldQuantity, updateDto);

        if (newQuantity < 0) {
            throw new ProductException("Insufficient stock. Available: " + oldQuantity + ", Requested: " + updateDto.getQuantity());
        }

        product.setStockQuantity(newQuantity);
        productRepository.save(product);

        // Publish stock changed event
        Map<String, Object> stockEvent = Map.of(
                "productId", product.getId(),
                "sku", product.getSku(),
                "oldQuantity", oldQuantity,
                "newQuantity", newQuantity,
                "operation", updateDto.getOperation(),
                "reason", updateDto.getReason() != null ? updateDto.getReason() : "Manual update"
        );

        kafkaTemplate.send("stock.changed", stockEvent);
        log.info("Updated inventory for product {} from {} to {}", product.getId(), oldQuantity, newQuantity);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThanMinStockLevel();
    }

    public boolean isInStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException("Product not found with id: " + productId));
        return product.getStockQuantity() >= quantity;
    }

    @CacheEvict(value = "products", key = "#productId")
    public void reserveStock(Long productId, int quantity) {
        InventoryUpdateDto updateDto = InventoryUpdateDto.builder()
                .productId(productId)
                .quantity(quantity)
                .operation("SUBTRACT")
                .reason("Stock reservation")
                .build();
        updateInventory(updateDto);
    }

    @CacheEvict(value = "products", key = "#productId")
    public void releaseStock(Long productId, int quantity) {
        InventoryUpdateDto updateDto = InventoryUpdateDto.builder()
                .productId(productId)
                .quantity(quantity)
                .operation("ADD")
                .reason("Stock release")
                .build();
        updateInventory(updateDto);
    }

    private int calculateNewQuantity(int currentQuantity, InventoryUpdateDto updateDto) {
        return switch (updateDto.getOperation().toUpperCase()) {
            case "ADD" -> currentQuantity + updateDto.getQuantity();
            case "SUBTRACT" -> currentQuantity - updateDto.getQuantity();
            case "SET" -> updateDto.getQuantity();
            default -> throw new ProductException("Invalid operation: " + updateDto.getOperation());
        };
    }
}