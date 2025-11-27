package org.iic.formsystem.controller;

import jakarta.validation.Valid;
import org.iic.formsystem.dto.ReorderRequestDTO;
import org.iic.formsystem.dto.question.CreateQuestionDTO;
import org.iic.formsystem.dto.question.EditQuestionDTO;
import org.iic.formsystem.dto.question.QuestionDTO;
import org.iic.formsystem.service.QuestionOptionService;
import org.iic.formsystem.service.QuestionService;
import org.iic.formsystem.service.SurveyService;
import org.iic.formsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;
    private final QuestionOptionService questionOptionService;

    public QuestionController(QuestionService questionService, QuestionOptionService questionOptionService) {
        this.questionService = questionService;
        this.questionOptionService = questionOptionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestionById(@PathVariable UUID id) {
        QuestionDTO question = questionService.getQuestion(id);
        return ResponseEntity.ok(question);
    }

    //example
//    {
//        "text": "Write sth idk",
//            "type": "SHORT_ANSWER",
//            "displayOrder": "5"
//    }
    @PostMapping("/create")
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestParam UUID formId,
            @Valid @RequestBody CreateQuestionDTO dto) throws IllegalAccessException {

        QuestionDTO createdQuestion = questionService.addQuestion(dto, formId);

        return ResponseEntity.ok(createdQuestion);
    }

    @DeleteMapping("/{id}/hard-delete")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID id) throws IllegalAccessException {
        questionService.hardDeleteQuestion(id);
        return ResponseEntity.noContent().build();
    }

    //example: PATCH /api/questions/{id} -> { "text": "What is your corrected name?" }
    //example 2: PATCH /api/questions/{id} -> { "required": true }
    @PatchMapping("/{id}/edit")
    public ResponseEntity<QuestionDTO> editQuestion(
            @PathVariable UUID id,
            @RequestBody EditQuestionDTO dto) throws IllegalAccessException {

        QuestionDTO updatedQuestion = questionService.editQuestion(id, dto);
        return ResponseEntity.ok(updatedQuestion);
    }

    @PatchMapping("/{id}/active-switch")
    public ResponseEntity<Void> activeSwitchQuestion(@PathVariable UUID id) throws IllegalAccessException {
        questionService.activeSwitch(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/options/reorder")
    public ResponseEntity<Void> reorderOptions(
            @PathVariable("id") UUID questionId,
            @RequestBody @Valid ReorderRequestDTO dto) throws IllegalAccessException {

        questionOptionService.reorderOptions(questionId, dto);
        return ResponseEntity.ok().build();
    }
}
