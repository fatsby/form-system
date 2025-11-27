package org.iic.formsystem.repository;

import org.iic.formsystem.model.surveyform.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {
    Optional<QuestionOption> findByDisplayOrderAndActiveTrue (int displayOrder);
}
