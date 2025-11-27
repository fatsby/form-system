package org.iic.formsystem.dto.survey;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EditSurveyDTO {
    //id is sent using api path variables
    private String title;

    private String description;
}
