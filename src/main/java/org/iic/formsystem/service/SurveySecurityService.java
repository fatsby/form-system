package org.iic.formsystem.service;

import jakarta.persistence.EntityNotFoundException;
import org.iic.formsystem.model.User;
import org.iic.formsystem.model.surveyform.Question;
import org.iic.formsystem.model.surveyform.QuestionOption;
import org.iic.formsystem.model.surveyform.SurveyForm;
import org.iic.formsystem.repository.QuestionOptionRepository;
import org.iic.formsystem.repository.QuestionRepository;
import org.iic.formsystem.repository.SurveyFormRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SurveySecurityService {

    private final SurveyFormRepository surveyRepository;
    private final QuestionOptionRepository optionRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;

    public SurveySecurityService(SurveyFormRepository surveyRepository, QuestionOptionRepository optionRepository,
                                 QuestionRepository questionRepository,
                                 UserService userService) {
        this.surveyRepository = surveyRepository;
        this.optionRepository = optionRepository;
        this.questionRepository = questionRepository;
        this.userService = userService;
    }

    /**
     * Checks if current user is creator.
     * Returns the Form if allowed.
     * Throws exception if not found or not authorized.
     */
    public SurveyForm getFormIfCreator(UUID formId) throws IllegalAccessException {
        User currentUser = userService.findUserFromToken();

        SurveyForm form = surveyRepository.findById(formId)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));

        if (!form.getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalAccessException("You do not have permission to modify this form.");
        }

        return form;
    }

    /**
     * Checks if current user is creator of the form that owns this question.
     * Returns the Question if allowed.
     */
    public Question getQuestionIfCreator(UUID questionId) throws IllegalAccessException {
        User currentUser = userService.findUserFromToken();

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        if (!question.getSurveyForm().getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalAccessException("You do not have permission to modify this question.");
        }

        return question;
    }

    /**
     * Checks if current user is creator of the form that owns this question option.
     * Returns the QuestionOption if allowed.
     */
    public QuestionOption getOptionIfCreator(UUID optionId) throws IllegalAccessException {
        User currentUser = userService.findUserFromToken();

        QuestionOption option = optionRepository.findById(optionId)
                .orElseThrow(() -> new EntityNotFoundException("Option not found"));

        // Option -> Question -> Form -> Creator
        if (!option.getQuestion().getSurveyForm().getCreator().getId().equals(currentUser.getId())) {
            throw new IllegalAccessException("You do not have permission to delete this option.");
        }

        return option;
    }
}