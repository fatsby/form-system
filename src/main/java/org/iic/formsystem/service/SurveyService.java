package org.iic.formsystem.service;

import jakarta.persistence.EntityNotFoundException;
import org.iic.formsystem.dto.question.QuestionDTO;
import org.iic.formsystem.dto.survey.CreateSurveyFormDTO;
import org.iic.formsystem.dto.survey.EditSurveyDTO;
import org.iic.formsystem.dto.survey.SurveyDTO;
import org.iic.formsystem.model.User;
import org.iic.formsystem.model.surveyform.Question;
import org.iic.formsystem.model.surveyform.SurveyForm;
import org.iic.formsystem.repository.SurveyFormRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SurveyService {

    private final SurveyFormRepository surveyRepository;
    private final UserService userService;
    private final SurveySecurityService securityService;
    private final QuestionService questionService;
    private final SubmissionService submissionService;

    public SurveyService(SurveyFormRepository surveyRepository,
                         UserService userService, SurveySecurityService securityService,
                         QuestionService questionService, SubmissionService submissionService) {
        this.surveyRepository = surveyRepository;
        this.userService = userService;
        this.securityService = securityService;
        this.questionService = questionService;
        this.submissionService = submissionService;
    }

    @Transactional
    public SurveyDTO createSurvey(CreateSurveyFormDTO dto) {
        User creator = userService.findUserFromToken();

        SurveyForm form = new SurveyForm();
        form.setTitle(dto.getTitle());
        form.setDescription(dto.getDescription());
        form.setCreator(creator);
        form.setActive(true);

        // DELEGATION
        if (dto.getQuestions() != null) {
            List<Question> questions = dto.getQuestions().stream()
                    .map(qDto -> questionService.buildQuestionEntity(qDto, form))
                    .collect(Collectors.toList());

            form.setQuestions(questions);
        }

        // SAVE ONCE. cascades all is enabled
        surveyRepository.save(form);

        return mapToDTO(form);
    }

    @Transactional // edit TITLE and DESCRIPTION
    public SurveyDTO editSurvey(UUID id, EditSurveyDTO dto) throws IllegalAccessException {
        SurveyForm form = securityService.getFormIfCreator(id);

        if (dto.getTitle() != null && !dto.getTitle().isEmpty()) {
            form.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            form.setDescription(dto.getDescription());
        }

        form.setUpdatedAt(LocalDateTime.now());

        surveyRepository.save(form);
        return mapToDTO(form);
    }

    @Transactional
    public void activeSwitch(UUID id) throws IllegalAccessException {
        SurveyForm form = securityService.getFormIfCreator(id);

        boolean newActive = !form.isActive();
        form.setActive(newActive);
    }

    @Transactional
    public SurveyDTO updateExpiry(UUID id, LocalDateTime expiresAt) throws IllegalAccessException {
        SurveyForm form = securityService.getFormIfCreator(id);

        form.setExpiresAt(expiresAt);

        form.setUpdatedAt(LocalDateTime.now());

        surveyRepository.save(form);
        return mapToDTO(form);
    }

    @Transactional
    public void hardDeleteSurvey(UUID id) throws IllegalAccessException {
        // Creator check
        SurveyForm form = securityService.getFormIfCreator(id);

        // Check for existing submission before deleting
        long submissionCount = submissionService.getSubmissionCountOfSurvey(id);
        if (submissionCount > 0) {
            throw new IllegalStateException("Cannot hard delete this survey because it has " + submissionCount + " submissions. Please use Soft Delete (Archive) instead, or delete the submissions first.");
        }

        // DELEGATION: Clean up Questions => Questions will clean up Options
        questionService.batchDeleteQuestions(form);

        // same logic in QuestionService, clear the Questions list so Hibernate doesn't resave it
        if (form.getQuestions() != null) {
            form.getQuestions().clear();
        }

        // Delete the Form
        surveyRepository.deleteAllInBatch(List.of(form));
    }


    @Transactional(readOnly = true)
    public SurveyDTO getSurveyDTO(UUID id) {
        SurveyForm form = surveyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));

        return mapToDTO(form);
    }

    public List<SurveyDTO> getSurveyByCreatorId(UUID id) {
        User creator = userService.findUserById(id);
        List<SurveyForm> formList = surveyRepository.findByCreator(creator);
        return formList.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SurveyDTO> getAllSurveys() {
        List<SurveyForm> formList = surveyRepository.findAll();
        return formList.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    //private helpers section

    private SurveyDTO mapToDTO(SurveyForm form) {
        SurveyDTO dto = new SurveyDTO();
        dto.setId(form.getId());
        dto.setTitle(form.getTitle());
        dto.setDescription(form.getDescription());
        dto.setActive(form.isActive());
        dto.setCreatedAt(form.getCreatedAt());
        dto.setUpdatedAt(form.getUpdatedAt());
        dto.setExpiresAt(form.getExpiresAt());

        if (form.getCreator() != null) {
            dto.setCreatorName(form.getCreator().getUsername());
        }

        if (form.getQuestions() != null) {
            List<QuestionDTO> questionDTOs = form.getQuestions().stream()
                    .map(questionService::mapToDTO)
                    .collect(Collectors.toList());

            dto.setQuestions(questionDTOs);
        }

        return dto;
    }

}
