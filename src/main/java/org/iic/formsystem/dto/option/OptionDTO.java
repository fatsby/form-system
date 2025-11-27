package org.iic.formsystem.dto.option;

import lombok.Data;
import java.util.UUID;

@Data
public class OptionDTO {
    private UUID id;
    private String text;
    private int displayOrder;
    private boolean active;
}