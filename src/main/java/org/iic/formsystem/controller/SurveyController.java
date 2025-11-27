package org.iic.formsystem.controller;

import jakarta.validation.Valid;
import org.iic.formsystem.dto.ReorderRequestDTO;
import org.iic.formsystem.dto.survey.ExpiryRequest;
import org.iic.formsystem.dto.survey.CreateSurveyFormDTO;
import org.iic.formsystem.dto.survey.EditSurveyDTO;
import org.iic.formsystem.dto.survey.SurveyDTO;
import org.iic.formsystem.service.QuestionService;
import org.iic.formsystem.service.SurveyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/forms")
public class SurveyController {

    private final SurveyService surveyService;
    private final QuestionService questionService;

    public SurveyController(SurveyService surveyService, QuestionService questionService) {
        this.surveyService = surveyService;
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<SurveyDTO>> Get(
            @RequestParam(required = false) UUID creatorId
    ) {
        if (creatorId != null) {
            return ResponseEntity.ok(surveyService.getSurveyByCreatorId(creatorId));
        }

        return ResponseEntity.ok(surveyService.getAllSurveys());
    }

    @PostMapping("/create")
    public ResponseEntity<SurveyDTO> createSurvey(@Valid @RequestBody CreateSurveyFormDTO dto) {
        SurveyDTO createdSurvey = surveyService.createSurvey(dto);
        return ResponseEntity.ok(createdSurvey);
    }

    @PatchMapping("/{id}/edit")
    public ResponseEntity<SurveyDTO> editSurvey(@PathVariable UUID id, @RequestBody EditSurveyDTO dto) throws IllegalAccessException {
        SurveyDTO editSurvey = surveyService.editSurvey(id, dto);
        return ResponseEntity.ok(editSurvey);
    }

    //this is basically soft delete
    @PatchMapping("{id}/active-switch")
    public ResponseEntity<Void> activeSwitch(@PathVariable UUID id) throws IllegalAccessException {
        surveyService.activeSwitch(id);
        return ResponseEntity.ok().build();
    }

    //To Set: Send { "expiresAt": "2025-12-31T23:59:59" }
    //To Clear: Send { "expiresAt": null }
    @PatchMapping("/{id}/expire")
    public ResponseEntity<SurveyDTO> updateExpiry(@PathVariable UUID id, @RequestBody ExpiryRequest request) throws IllegalAccessException {
        return ResponseEntity.ok(surveyService.updateExpiry(id, request.getExpiresAt()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveyDTO> getSurvey(@PathVariable UUID id) {
        return ResponseEntity.ok(surveyService.getSurveyDTO(id));
    }

    @DeleteMapping("/{id}/hard-delete")
    public ResponseEntity<Void> hardDeleteSurvey(@PathVariable UUID id) throws IllegalAccessException {
        surveyService.hardDeleteSurvey(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/questions/reorder")
    public ResponseEntity<Void> reorderQuestions(
            @PathVariable UUID id,
            @RequestBody @Valid ReorderRequestDTO dto) throws IllegalAccessException {

        questionService.reorderQuestions(id, dto);
        return ResponseEntity.ok().build();
    }
}
