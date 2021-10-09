package com.bkav.lk.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "detail_medical_declaration_info")
public class DetailMedicalDeclarationInfo extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medical_declaration_info_id")
    private MedicalDeclarationInfo medicalDeclarationInfo;

    @ManyToOne
    @JoinColumn(name = "declaration_question_id")
    private DeclarationQuestion declarationQuestion;

    @Column(name = "answer")
    private String answer;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedicalDeclarationInfo getMedicalDeclarationInfo() {
        return medicalDeclarationInfo;
    }

    public void setMedicalDeclarationInfo(MedicalDeclarationInfo medicalDeclarationInfo) {
        this.medicalDeclarationInfo = medicalDeclarationInfo;
    }

    public DeclarationQuestion getDeclarationQuestion() {
        return declarationQuestion;
    }

    public void setDeclarationQuestion(DeclarationQuestion declarationQuestion) {
        this.declarationQuestion = declarationQuestion;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
