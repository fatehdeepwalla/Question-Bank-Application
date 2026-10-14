package org.example.questionBankApplication.model.answer;

public class MCQ_Answer implements Answer {

    private AnswerOption correctOption;

    public MCQ_Answer(AnswerOption correctOption) {
        if (correctOption == null) {throw new RuntimeException("Null Argument");}
        this.correctOption = correctOption;
    }

    public AnswerOption getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(AnswerOption correctOption) {
        if (correctOption == null) {throw new RuntimeException("Null Argument");}
        this.correctOption = correctOption;
    }
}
