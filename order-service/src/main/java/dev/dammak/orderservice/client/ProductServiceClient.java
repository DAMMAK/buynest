
package dev.dammak.orderservice.client;

import dev.dammak.orderservice.dto.ProductDto;
import dev.dammak.orderservice.exception.OrderException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "PRODUCT-CATALOG-SERVICE", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    ProductDto getProductById(@PathVariable("id") Long id);
}

// Fallback implementation
@Component
class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductDto getProductById(Long id) {
        // Return a default product or throw an exception
        throw new OrderException("Product service is currently unavailable. Product ID: " + id);
    }
}