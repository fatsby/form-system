package org.iic.formsystem.dto.submission;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class SubmissionDTO {
    private UUID id;
    private LocalDateTime submittedAt;
    private String surveyFormTitle;
    private String respondentUsername;
    private List<AnswerDTO> answers;
}
