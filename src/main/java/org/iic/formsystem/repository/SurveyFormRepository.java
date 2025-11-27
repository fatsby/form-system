package org.iic.formsystem.repository;

import org.iic.formsystem.model.User;
import org.iic.formsystem.model.surveyform.SurveyForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SurveyFormRepository extends JpaRepository<SurveyForm, UUID> {

    // SELECT * FROM forms WHERE is_active = true
    List<SurveyForm> findByIsActiveTrue();

    List<SurveyForm> findByCreator(User creator);

    // find all active AND not expired
    // SELECT * FROM forms WHERE is_active = true AND (expires_at > ? OR expires_at IS NULL)
    List<SurveyForm> findByIsActiveTrueAndExpiresAtAfterOrExpiresAtIsNull(LocalDateTime now);

}
