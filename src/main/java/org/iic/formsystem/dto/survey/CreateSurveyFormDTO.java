package org.iic.formsystem.dto.survey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.iic.formsystem.dto.question.CreateQuestionDTO;

import java.util.List;

@Data
public class CreateSurveyFormDTO {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @Valid
    private List<CreateQuestionDTO> questions;
}