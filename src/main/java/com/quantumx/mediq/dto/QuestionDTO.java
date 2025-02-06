package com.quantumx.mediq.dto;

import java.util.List;

public class QuestionDTO {
    private Long id;
    private String question;
    private String justification;
    private List<String> options;
    private String rightAnswer;
    private String imageUrl;
    private String imageFilename;
    private String justificationImageUrl;
    private String justificationImageName;


    public QuestionDTO(Long id, String question, String justification, List<String> options, String rightAnswer, String imageUrl, String imageFilename, String justificationImageUrl, String justificationImageName) {
        this.id = id;
        this.question = question;
        this.justification = justification;
        this.options = options;
        this.rightAnswer = rightAnswer;
        this.imageFilename = imageFilename;
        this.imageUrl = imageUrl;
        this.justificationImageName = justificationImageName;
        this.justificationImageUrl = justificationImageUrl;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public String getJustificationImageUrl() {
        return justificationImageUrl;
    }

    public void setJustificationImageUrl(String justificationImageUrl) {
        this.justificationImageUrl = justificationImageUrl;
    }

    public String getJustificationImageName() {
        return justificationImageName;
    }

    public void setJustificationImageName(String justificationImageName) {
        this.justificationImageName = justificationImageName;
    }
}
