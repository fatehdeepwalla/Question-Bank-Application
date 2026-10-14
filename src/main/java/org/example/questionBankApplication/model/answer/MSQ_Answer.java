
package org.example.questionBankApplication.model.answer;

import java.util.Set;

public class MSQ_Answer implements Answer {

    private Set<AnswerOption> correctOptions;

    public MSQ_Answer(Set<AnswerOption> correctOptions) {
        if (correctOptions == null) {throw new RuntimeException("Null Argument");}
        this.correctOptions = correctOptions;
    }

    public Set<AnswerOption> getCorrectOptions() {
        return correctOptions;
    }

    public void setCorrectOptions(Set<AnswerOption> correctOptions) {
        if (correctOptions == null) {throw new RuntimeException("Null Argument");}
        this.correctOptions = correctOptions;
    }
}