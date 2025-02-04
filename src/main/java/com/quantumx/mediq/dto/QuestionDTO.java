package com.quantumx.mediq.dto;

import java.util.List;

public class QuestionDTO {
    private Long id;
    private String question;
    private String justification;
    private List<String> options;
    private String rightAnswer;

    public QuestionDTO(Long id, String question, String justification, List<String> options, String rightAnswer) {
        this.id = id;
        this.question = question;
        this.justification = justification;
        this.options = options;
        this.rightAnswer = rightAnswer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public void setRightAnswer(String rightAnswer) {
        this.rightAnswer = rightAnswer;
    }
}
