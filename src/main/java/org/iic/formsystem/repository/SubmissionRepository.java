package org.iic.formsystem.repository;

import org.iic.formsystem.model.User;
import org.iic.formsystem.model.submission.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    long countBySurveyFormId(UUID surveyFormId);
    List<Submission> findBySurveyFormId(UUID surveyFormId);
    List<Submission> findBySurveyFormIdAndIsDeletedFalse(UUID surveyFormId);
    List<Submission> findByRespondent(User respondent);
}
