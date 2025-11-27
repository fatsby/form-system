package org.iic.formsystem.service;

import jakarta.persistence.EntityNotFoundException;
import org.iic.formsystem.dto.ReorderRequestDTO;
import org.iic.formsystem.dto.option.CreateOptionDTO;
import org.iic.formsystem.dto.option.OptionDTO;
import org.iic.formsystem.model.surveyform.Question;
import org.iic.formsystem.model.surveyform.QuestionOption;
import org.iic.formsystem.repository.QuestionOptionRepository;
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
public class QuestionOptionService {
    private final List<String> VALID_QUESTION_TYPES = List.of("MULTIPLE_CHOICE", "CHECKBOX", "DROPDOWN", "LIKERT");
    private final QuestionOptionRepository optionRepository;
    private final SurveySecurityService securityService;

    public QuestionOptionService(QuestionOptionRepository optionRepository, SurveySecurityService securityService) {
        this.optionRepository = optionRepository;
        this.securityService = securityService;
    }

    public OptionDTO getOption(UUID id) {
        QuestionOption option = optionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Option not found."));
        return mapToDTO(option);
    }

    /**
     * Helper: Builds the entity WITHOUT saving it.
     * Used by QuestionService for bulk operations.
     */
    public QuestionOption buildOptionEntity(CreateOptionDTO dto, Question parent) {
        QuestionOption option = new QuestionOption();
        option.setText(dto.getText());
        option.setDisplayOrder(dto.getDisplayOrder());
        option.setQuestion(parent);
        option.setActive(true);
        return option;
    }

    /**
     * Granular: Builds AND Saves.
     * Used for single additions via API.
     */
    @Transactional
    public OptionDTO addOption(UUID questionId, CreateOptionDTO dto) throws IllegalAccessException {
        Question parentQuestion = securityService.getQuestionIfCreator(questionId);

        //validate question type, short answers and paragraphs shouldn't have multiple options
        String parentQuestionType = parentQuestion.getType().toString();
        if (!VALID_QUESTION_TYPES.contains(parentQuestionType)) {
            throw new IllegalArgumentException("Question type is not valid");
        }

        //validate displayOrder
        Optional<QuestionOption> existingDisplayOrder = optionRepository.findByDisplayOrderAndActiveTrue(dto.getDisplayOrder());
        existingDisplayOrder.ifPresent(_ -> {
                    throw new IllegalArgumentException("displayOrder already used by another Option.");
                }
        );

        //create option
        QuestionOption option = buildOptionEntity(dto, parentQuestion);
        OptionDTO savedOptionDTO = mapToDTO(optionRepository.save(option));

        //update updatedAt
        if (parentQuestion.getSurveyForm() != null) {
            parentQuestion.getSurveyForm().setUpdatedAt(LocalDateTime.now());
        }
        return savedOptionDTO;
    }

    @Transactional
    public OptionDTO editOption(String text, UUID optionId) throws IllegalAccessException {
        QuestionOption option = securityService.getOptionIfCreator(optionId);

        if (text != null && !text.isBlank()) {
            option.setText(text);
        }

        return mapToDTO(option);
    }

    @Transactional
    public void activeSwitch(UUID optionId) throws IllegalAccessException {
        QuestionOption option = securityService.getOptionIfCreator(optionId);

        option.setActive(!option.isActive());

        //update updatedAt property
        if (option.getQuestion() != null && option.getQuestion().getSurveyForm() != null) {
            option.getQuestion().getSurveyForm().setUpdatedAt(LocalDateTime.now());
        }
    }

    @Transactional
    public void reorderOptions(UUID questionId, ReorderRequestDTO dto) throws IllegalAccessException {
        Question question = securityService.getQuestionIfCreator(questionId);

        List<QuestionOption> options = question.getOptions();
        Map<UUID, QuestionOption> optionMap = options.stream()
                .collect(Collectors.toMap(QuestionOption::getId, Function.identity()));

        List<UUID> newOrder = dto.getOrderedIds();
        for (int i = 1; i <= newOrder.size(); i++) {
            UUID optionId = newOrder.get(i-1);

            if (optionMap.containsKey(optionId)) {
                QuestionOption opt = optionMap.get(optionId);
                if (opt.getDisplayOrder() != i) {
                    opt.setDisplayOrder(i);
                }
            }
        }

        if (question.getSurveyForm() != null) {
            question.getSurveyForm().setUpdatedAt(LocalDateTime.now());
        }
    }

    /**
     * Batch Delete: Delete all Options in a Question
     * Used by QuestionService
     */
    @Transactional
    public void batchDeleteOptions(Question question) {
        // delete from database
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            optionRepository.deleteAllInBatch(question.getOptions());
        }
    }

    /**
     * Granular single delete for API Endpoint
     */
    @Transactional
    public void hardDeleteOption(UUID optionId) throws IllegalAccessException {
        QuestionOption option = securityService.getOptionIfCreator(optionId);

        Question parentQuestion = option.getQuestion();

        optionRepository.delete(option);

        if (parentQuestion.getSurveyForm() != null) {
            parentQuestion.getSurveyForm().setUpdatedAt(LocalDateTime.now());
        }
    }

    public OptionDTO mapToDTO(QuestionOption save) {
        OptionDTO optionDTO = new OptionDTO();

        optionDTO.setId(save.getId());
        optionDTO.setText(save.getText());
        optionDTO.setDisplayOrder(save.getDisplayOrder());
        optionDTO.setActive(save.isActive());

        return optionDTO;
    }
}
