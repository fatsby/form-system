package org.iic.formsystem.dto.question;

import lombok.Data;
import org.iic.formsystem.dto.option.OptionDTO;

import java.util.List;
import java.util.UUID;

@Data
public class QuestionDTO {
    private UUID id;
    private String text;
    private String type;
    private boolean required;
    private int displayOrder;
    private boolean active;

    private List<OptionDTO> options;
}