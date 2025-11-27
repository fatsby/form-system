package org.iic.formsystem.dto.survey;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpiryRequest {
    private LocalDateTime expiresAt;
}
