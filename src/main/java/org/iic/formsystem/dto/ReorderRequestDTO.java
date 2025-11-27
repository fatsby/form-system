package org.iic.formsystem.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ReorderRequestDTO {
    @NotEmpty(message = "The order list cannot be empty")
    private List<UUID> orderedIds;
}
