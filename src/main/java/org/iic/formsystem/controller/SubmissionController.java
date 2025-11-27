package org.iic.formsystem.controller;

import jakarta.persistence.EntityNotFoundException;
import org.iic.formsystem.dto.submission.CreateSubmissionDTO;
import org.iic.formsystem.dto.submission.SubmissionDTO;
import org.iic.formsystem.service.SubmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/submissions")
public class SubmissionController {
    private final SubmissionService submissionService;
    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("{id}")

    public ResponseEntity<SubmissionDTO> getSubmissionById(@PathVariable UUID id) throws EntityNotFoundException {

        SubmissionDTO submissionDTO = submissionService.getSubmissionById(id);

        return ResponseEntity.ok(submissionDTO);

    }

    // URL: GET /api/submissions?formId=...
    @GetMapping
    public ResponseEntity<List<SubmissionDTO>> getSubmissions(
            @RequestParam(required = false) UUID formId,
            @RequestParam(required = false) UUID respondentId) {

        if (formId != null) {
            return ResponseEntity.ok(submissionService.getSubmissionByFormId(formId));
        } else if (respondentId != null) {
            return ResponseEntity.ok(submissionService.getSubmissionByRespondentId(respondentId));
        }

        return ResponseEntity.ok(submissionService.getAllSubmissions()); // Or return all
    }

    @PostMapping("/create")
    public ResponseEntity<SubmissionDTO> create(@RequestBody CreateSubmissionDTO createSubmissionDTO) throws IllegalAccessException {
        SubmissionDTO submissionDTO =  submissionService.createSubmission(createSubmissionDTO);
        return ResponseEntity.ok().body(submissionDTO);
    }

    @DeleteMapping("{id}/soft-delete")
    public ResponseEntity<SubmissionDTO> softDelete(@PathVariable UUID id) {
        submissionService.softDeleteSubmission(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{id}/hard-delete")
    public ResponseEntity<SubmissionDTO> hardDelete(@PathVariable UUID id) {
        submissionService.hardDeleteSubmission(id);
        return ResponseEntity.noContent().build();
    }
}
