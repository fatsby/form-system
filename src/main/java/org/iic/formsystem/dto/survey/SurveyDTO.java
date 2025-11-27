package org.iic.formsystem.dto.survey;

import lombok.Data;
import org.iic.formsystem.dto.question.QuestionDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SurveyDTO {
    private UUID id;
    private String title;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private String creatorName;
    private List<QuestionDTO> questions;
}