package org.iic.formsystem.dto.question;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.iic.formsystem.dto.option.CreateOptionDTO;

import java.util.List;

@Data
public class CreateQuestionDTO {
    @NotBlank(message = "Question text is required")
    private String text;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Display Order is required")
    private Integer displayOrder;

    private Boolean required;

    @Valid
    private List<CreateOptionDTO> options;
}