package org.iic.formsystem.service;


import jakarta.persistence.EntityNotFoundException;
import org.iic.formsystem.dto.submission.*;
import org.iic.formsystem.model.submission.Answer;
import org.iic.formsystem.model.submission.Submission;
import org.iic.formsystem.model.User;
import org.iic.formsystem.model.surveyform.Question;
import org.iic.formsystem.model.surveyform.SurveyForm;
import org.iic.formsystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final SurveyFormRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final UserService userService;

    public SubmissionService(SubmissionRepository submissionRepository,
                             SurveyFormRepository surveyRepository,
                             QuestionRepository questionRepository,
                             UserService userService) {
        this.submissionRepository = submissionRepository;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.userService = userService;
    }

    public SubmissionDTO getSubmissionById(UUID id) throws EntityNotFoundException {
        Submission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));
        return mapToDTO(submission);
    }

    public List<SubmissionDTO> getSubmissionByRespondentId(UUID id) throws EntityNotFoundException {
        User respondent = userService.findUserById(id);
        List<Submission> submission = submissionRepository.findByRespondent(respondent);
        return  submission.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SubmissionDTO> getSubmissionByFormId(UUID id) throws EntityNotFoundException {
        List<Submission> submission = submissionRepository.findBySurveyFormId(id);
        return  submission.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<SubmissionDTO> getAllSubmissions() throws EntityNotFoundException {
        List<Submission> submission = submissionRepository.findAll();
        return  submission.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public long getSubmissionCountOfSurvey(UUID surveyFormId){
        return submissionRepository.countBySurveyFormId(surveyFormId);
    }

    @Transactional
    public SubmissionDTO createSubmission(CreateSubmissionDTO dto) {
        // fetch form
        SurveyForm form = surveyRepository.findById(dto.getFormId())
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));

        // validate form status
        if (!form.isActive()) {
            throw new IllegalStateException("Form is inactive.");
        }
        if (form.getExpiresAt() != null && form.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Form has expired.");
        }

        // create submission
        Submission submission = new Submission();
        submission.setSurveyForm(form);
        submission.setSubmittedAt(LocalDateTime.now());

        // handle User
        try {
            User currentUser = userService.findUserFromToken();
            submission.setRespondent(currentUser);
        } catch (Exception e) {
            // catch anonymous submission
        }

        // process Answers
        if (dto.getAnswers() != null && !dto.getAnswers().isEmpty()) {
            List<Answer> answerEntities = processAnswers(dto.getAnswers(), form, submission);
            submission.setAnswers(answerEntities);
        }

        // save
        Submission saved = submissionRepository.save(submission);
        return mapToDTO(saved);
    }

    @Transactional
    public void softDeleteSubmission(UUID submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found"));

        submission.setDeleted(true);
        submissionRepository.save(submission);
    }

    @Transactional
    public void hardDeleteSubmission(UUID submissionId) {
        if (!submissionRepository.existsById(submissionId)) {
            throw new EntityNotFoundException("Submission not found");
        }
        // Submission -> Answer has CascadeType.ALL
        submissionRepository.deleteById(submissionId);
    }

    //helper
    private List<Answer> processAnswers(List<CreateAnswerDTO> answerDtos, SurveyForm form, Submission submission) {
        List<Answer> result = new ArrayList<>();

        Map<UUID, Question> questionMap = questionRepository.findBySurveyFormIdAndActiveTrue(form.getId())
                .stream().collect(Collectors.toMap(Question::getId, Function.identity()));

        for (CreateAnswerDTO ansDto : answerDtos) {
            Question question = questionMap.get(ansDto.getQuestionId());

            if (question == null) continue;

            String value = ansDto.getValue();

            if (question.isRequired() && (value == null || value.isBlank())) {
                throw new IllegalArgumentException("Question '" + question.getText() + "' is required.");
            }

            if (value != null && !value.isBlank()) {
                Answer answer = new Answer();
                answer.setSubmission(submission);
                answer.setQuestion(question);

                // save the string provided by frontend
                answer.setValue(value);

                result.add(answer);
            }
        }

        return result;
    }

    private SubmissionDTO mapToDTO(Submission submission) {
        SubmissionDTO dto = new SubmissionDTO();
        dto.setId(submission.getId());
        dto.setSubmittedAt(submission.getSubmittedAt());
        dto.setSurveyFormTitle(submission.getSurveyForm().getTitle());
        if (submission.getRespondent() != null) {
            dto.setRespondentUsername(submission.getRespondent().getUsername());
        }

        // map Answers
        if (submission.getAnswers() != null) {
            List<AnswerDTO> answerDTOs = submission.getAnswers().stream().map(ans -> {
                AnswerDTO aDto = new AnswerDTO();
                aDto.setId(ans.getId());
                aDto.setQuestionId(ans.getQuestion().getId());
                aDto.setValue(ans.getValue());
                return aDto;
            }).collect(Collectors.toList());
            dto.setAnswers(answerDTOs);
        }
        return dto;
    }
}
