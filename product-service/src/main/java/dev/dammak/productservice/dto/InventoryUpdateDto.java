package dev.dammak.productservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Inventory update request")
public class InventoryUpdateDto {

    @Schema(description = "Product ID to update inventory for", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(description = "Quantity for the operation", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Schema(description = "Operation type", example = "ADD", allowableValues = {"ADD", "SUBTRACT", "SET"})
    private String operation;

    @Schema(description = "Reason for inventory update", example = "Stock replenishment")
    private String reason;
}