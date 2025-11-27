package org.iic.formsystem.dto.submission;

import lombok.Data;

import java.util.UUID;

@Data
public class AnswerDTO {
    private UUID id;
    private UUID questionId;
    private String value;
}
