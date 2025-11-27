package org.iic.formsystem.model.submission;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.iic.formsystem.model.User;
import org.iic.formsystem.model.surveyform.SurveyForm;

@Entity
@Data
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private SurveyForm surveyForm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User respondent; //nullable, anonymous submission

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    private List<Answer> answers;
}
