package com.bkav.lk.dto;

public class DetailMedicalDeclarationInfoDTO {

    private Long id;

    private Long medicalDeclarationInfoId;

    private Long questionId;

    private String questionType;

    private String questionContent;

    private String questionValue;

    private Integer questionStatus;

    private String answer;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMedicalDeclarationInfoId() {
        return medicalDeclarationInfoId;
    }

    public void setMedicalDeclarationInfoId(Long medicalDeclarationInfoId) {
        this.medicalDeclarationInfoId = medicalDeclarationInfoId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getQuestionContent() {
        return questionContent;
    }

    public void setQuestionContent(String questionContent) {
        this.questionContent = questionContent;
    }

    public String getQuestionValue() {
        return questionValue;
    }

    public void setQuestionValue(String questionValue) {
        this.questionValue = questionValue;
    }

    public Integer getQuestionStatus() {
        return questionStatus;
    }

    public void setQuestionStatus(Integer questionStatus) {
        this.questionStatus = questionStatus;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
