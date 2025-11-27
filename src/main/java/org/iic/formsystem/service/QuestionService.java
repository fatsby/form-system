package org.iic.formsystem.service;

import jakarta.persistence.EntityNotFoundException;
import org.iic.formsystem.dto.question.CreateQuestionDTO;
import org.iic.formsystem.dto.option.OptionDTO;
import org.iic.formsystem.dto.question.EditQuestionDTO;
import org.iic.formsystem.dto.question.QuestionDTO;
import org.iic.formsystem.dto.ReorderRequestDTO;
import org.iic.formsystem.model.surveyform.Question;
import org.iic.formsystem.model.surveyform.QuestionOption;
import org.iic.formsystem.model.surveyform.QuestionType;
import org.iic.formsystem.model.surveyform.SurveyForm;
import org.iic.formsystem.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SurveySecurityService securityService;
    private final QuestionOptionService optionService;
    private final SubmissionService submissionService; // replace this with submission service later

    public QuestionService(QuestionRepository questionRepository, SurveySecurityService securityService,
                           QuestionOptionService optionService, SubmissionService submissionService) {
        this.questionRepository = questionRepository;
        this.securityService = securityService;
        this.optionService = optionService;
        this.submissionService = submissionService;
    }

    public QuestionDTO getQuestion(UUID id) {
        Question question = questionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Question not found."));

        return mapToDTO(question);
    }

    @Transactional
    public void reorderQuestions(UUID surveyId, ReorderRequestDTO dto) throws IllegalAccessException {
        //creator check
        SurveyForm form = securityService.getFormIfCreator(surveyId);

        List<Question> questions = form.getQuestions();

        Map<UUID, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        List<UUID> newOrder = dto.getOrderedIds();

        for (int i = 1; i <= newOrder.size(); i++) {
            UUID questionId = newOrder.get(i-1);

            // only update if the question actually exists in this form
            if (questionMap.containsKey(questionId)) {
                Question question = questionMap.get(questionId);

                // only update if the order actually changed (optimization)
                if (question.getDisplayOrder() != i) {
                    question.setDisplayOrder(i);
                }
            }
        }

        // update updatedAt
        form.setUpdatedAt(LocalDateTime.now());

        // hibernate handles update
    }

    //edit question text and the boolean required
    @Transactional
    public QuestionDTO editQuestion(UUID questionId, EditQuestionDTO dto) throws IllegalAccessException {
        // creator check
        Question question = securityService.getQuestionIfCreator(questionId);

        if (dto.getText() != null && !dto.getText().isBlank()) {
            question.setText(dto.getText());
        }

        if (dto.getRequired() != null) {
            question.setRequired(dto.getRequired());
        }

        // update updatedAt property
        if (question.getSurveyForm() != null) {
            question.getSurveyForm().setUpdatedAt(LocalDateTime.now());
        }

        return mapToDTO(questionRepository.save(question));
    }

    /**
     * Helper: Builds the entity (and children) WITHOUT saving.
     * Used by SurveyService for bulk operations.
     */
    public Question buildQuestionEntity(CreateQuestionDTO dto, SurveyForm parent) {
        Question question = new Question();
        question.setText(dto.getText());
        question.setType(QuestionType.valueOf(dto.getType()));
        question.setDisplayOrder(dto.getDisplayOrder());
        question.setRequired(dto.getRequired() != null && dto.getRequired());
        question.setSurveyForm(parent);
        question.setActive(true);

        // delegate, let QuestionOptionService create QuestionOption
        if (dto.getOptions() != null) {
            List<QuestionOption> options = dto.getOptions().stream()
                    .map(oDto -> optionService.buildOptionEntity(oDto, question))
                    .collect(Collectors.toList());

            question.setOptions(options);
        }

        return question;
    }

    /**
     * Granular: Builds AND Saves.
     */
    @Transactional
    public QuestionDTO addQuestion(CreateQuestionDTO dto, UUID parentId) throws IllegalAccessException {
        SurveyForm parent = securityService.getFormIfCreator(parentId);

        //validate displayOrder
        Optional<Question> existingDisplayOrder = questionRepository.findByDisplayOrderAndActiveTrue(dto.getDisplayOrder());
        existingDisplayOrder.ifPresent(_ -> {
                    throw new IllegalArgumentException("displayOrder already used by another Question.");
                }
        );

        //save question
        Question question = buildQuestionEntity(dto, parent);
        QuestionDTO savedQuestionDTO = mapToDTO(questionRepository.save(question));

        //updatedAt
        parent.setUpdatedAt(LocalDateTime.now());
        return savedQuestionDTO;
    }

    @Transactional
    public void activeSwitch(UUID id) throws IllegalAccessException {
        Question question = securityService.getQuestionIfCreator(id);

        boolean newActive = !question.isActive();
        question.setActive(newActive);
    }

    /**
     * Granular: Single delete for API Endpoint
     */
    @Transactional
    public void hardDeleteQuestion(UUID questionId) throws IllegalAccessException {
        // Creator check
        Question question = securityService.getQuestionIfCreator(questionId);
        SurveyForm parentForm = question.getSurveyForm();

        // If submissions exist, deleting a question corrupts the data
        if (submissionService.getSubmissionCountOfSurvey(parentForm.getId()) > 0) {
            throw new IllegalStateException("Cannot delete question. Submissions exist for this survey.");
        }

        // DELEGATE: Cleanup Children
        optionService.batchDeleteOptions(question);

        // this if condition prevents Hibernate from trying to "Resave" the deleted options
        // because the entity has the CascadeType.MERGE/PERSIST rules
        if (question.getOptions() != null) {
            question.getOptions().clear();
        }

        questionRepository.delete(question);

        // update the updatedAt property
        parentForm.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Hard delete all questions for a specific form
     * DELEGATION: Called by SurveyService
     */
    @Transactional
    public void batchDeleteQuestions(SurveyForm form) {
        List<Question> questions = form.getQuestions();

        if (questions == null || questions.isEmpty()) {
            return;
        }

        //Delegation: clean up children
        for (Question question : questions) {
            optionService.batchDeleteOptions(question);
        }


        // Database delete
        questionRepository.deleteAllInBatch(questions);
    }

    public QuestionDTO mapToDTO(Question save) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setId(save.getId());
        questionDTO.setText(save.getText());
        questionDTO.setDisplayOrder(save.getDisplayOrder());
        questionDTO.setType(save.getType().name());
        questionDTO.setRequired(save.isRequired());
        questionDTO.setActive(save.isActive());
        if (save.getOptions() != null) {
            List<OptionDTO> optionDTOs = save.getOptions().stream()
                    .map(optionService::mapToDTO)
                    .collect(Collectors.toList());

            questionDTO.setOptions(optionDTOs);
        }

        return questionDTO;
    }

}
