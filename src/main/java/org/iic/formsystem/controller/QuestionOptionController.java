package org.iic.formsystem.controller;

import jakarta.validation.Valid;
import org.iic.formsystem.dto.option.CreateOptionDTO;
import org.iic.formsystem.dto.option.OptionDTO;
import org.iic.formsystem.service.QuestionOptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/options")
public class QuestionOptionController {
    private final QuestionOptionService questionOptionService;

    public QuestionOptionController(QuestionOptionService questionOptionService) {
        this.questionOptionService = questionOptionService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OptionDTO> getOption(@PathVariable UUID id) {
        OptionDTO option = questionOptionService.getOption(id);
        return ResponseEntity.ok(option);
    }

    @PostMapping("/create")
    public ResponseEntity<OptionDTO> createQuestion(
            @RequestParam UUID questionId,
            @Valid @RequestBody CreateOptionDTO dto) throws IllegalAccessException {

        OptionDTO createdOption = questionOptionService.addOption(questionId, dto);

        return ResponseEntity.ok(createdOption);
    }

    @DeleteMapping("/{id}/hard-delete")
    public ResponseEntity<Void> deleteOption(@PathVariable UUID id) throws IllegalAccessException {
        questionOptionService.hardDeleteOption(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/edit")
    public ResponseEntity<OptionDTO> editOption(
            @PathVariable UUID id,
            @RequestParam String text) throws IllegalAccessException {
        return ResponseEntity.ok(questionOptionService.editOption(text, id));
    }

    @PatchMapping("/{id}/active-switch")
    public ResponseEntity<Void> toggleActive(@PathVariable UUID id) throws IllegalAccessException {
        questionOptionService.activeSwitch(id);
        return ResponseEntity.ok().build();
    }
}
