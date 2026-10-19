package org.example.questionBankApplication.model;

import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionAnswerValidationTest {

    @Test
    void questionTypeMustMatchProvidedAnswerImplementation() {
        assertThrows(IllegalArgumentException.class, () ->
                new Question(1, "q.png", 2021, Topic.Graph_Theory,
                        Type.MSQ, new MCQ_Answer(AnswerOption.A), Set.of()));

        assertThrows(IllegalArgumentException.class, () ->
                new Question(2, "q.png", 2021, Topic.Graph_Theory,
                        null, new MCQ_Answer(AnswerOption.A), Set.of()));
    }

    @Test
    void changingQuestionTypeClearsPreviouslyStoredAnswer() {
        Question question = new Question(3, "q.png", 2021, Topic.Graph_Theory,
                Type.MCQ, null, Set.of());
        question.setMcqAnswer(AnswerOption.A);

        question.setTypeAndAnswer(Type.NAT);

        assertNull(question.getAnswer());
        assertEquals(Type.NAT, question.getType());
    }

    @Test
    void answerSettersRejectWrongQuestionType() {
        Question question = new Question(4, "q.png", 2021, Topic.Graph_Theory,
                Type.MCQ, null, Set.of());

        assertThrows(IllegalStateException.class,
                () -> question.setMsqAnswer(Set.of(AnswerOption.A, AnswerOption.B)));
        assertThrows(IllegalStateException.class,
                () -> question.setNatAnswer(1, 2));
    }

    @Test
    void natAnswerRejectsInvalidRangesAndNonFiniteValues() {
        assertThrows(IllegalArgumentException.class, () -> new NAT_Answer(5, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new NAT_Answer(Double.NEGATIVE_INFINITY, 4));
        assertThrows(IllegalArgumentException.class,
                () -> new NAT_Answer(0, Double.NaN));

        NAT_Answer answer = new NAT_Answer(1, 2);
        assertThrows(IllegalArgumentException.class, () -> answer.setRange(3, 2));
        assertEquals(1, answer.getMinValue());
        assertEquals(2, answer.getMaxValue());
    }

    @Test
    void validNatRangeCanBeUpdated() {
        NAT_Answer answer = new NAT_Answer(-1.5, 2.5);

        answer.setRange(0, 3.25);

        assertEquals(0, answer.getMinValue());
        assertEquals(3.25, answer.getMaxValue());
    }
}
