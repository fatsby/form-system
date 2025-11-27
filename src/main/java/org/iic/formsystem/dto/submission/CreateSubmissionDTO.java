package org.iic.formsystem.dto.submission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CreateSubmissionDTO {
    @NotNull(message = "Form ID is required")
    private UUID formId;

    private List<CreateAnswerDTO> answers;
}
