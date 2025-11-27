package org.iic.formsystem.repository;

import org.iic.formsystem.model.surveyform.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findBySurveyFormIdAndActiveTrue(UUID surveyId);
    Optional<Question> findByDisplayOrderAndActiveTrue(int displayOrder);
}
