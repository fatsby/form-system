package org.iic.formsystem.dto.question;

import lombok.Data;

@Data
public class EditQuestionDTO {
    private String text;
    private Boolean required;
}