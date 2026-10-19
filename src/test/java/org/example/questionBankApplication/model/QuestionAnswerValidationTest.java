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
    void constructorRejectsMismatchedOrMissingTypeAndAnswer() {
        assertThrows(IllegalArgumentException.class, () ->
                new Question(1, "q.png", 2021, Topic.Graph_Theory,
                        Type.MSQ, new MCQ_Answer(AnswerOption.A), Set.of()));

        assertThrows(RuntimeException.class, () ->
                new Question(2, "q.png", 2021, Topic.Graph_Theory,
                        null, new MCQ_Answer(AnswerOption.A), Set.of()));

        assertThrows(RuntimeException.class, () ->
                new Question(3, "q.png", 2021, Topic.Graph_Theory,
                        Type.MCQ, null, Set.of()));
    }

    @Test
    void setTypeAndAnswerChangesTypeAndAnswerTogether() {
        Question question = new Question(4, "q.png", 2021, Topic.Graph_Theory,
                Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of());

        question.setTypeAndAnswer(Type.NAT, new NAT_Answer(1, 2));

        assertEquals(Type.NAT, question.getType());
        NAT_Answer answer = assertInstanceOf(NAT_Answer.class, question.getAnswer());
        assertEquals(1, answer.getMinValue());
        assertEquals(2, answer.getMaxValue());
    }

    @Test
    void setTypeAndAnswerRejectsMismatchedPairWithoutCorruptingQuestion() {
        Question question = new Question(5, "q.png", 2021, Topic.Graph_Theory,
                Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of());

        assertThrows(IllegalArgumentException.class,
                () -> question.setTypeAndAnswer(Type.NAT, new MCQ_Answer(AnswerOption.B)));

        assertEquals(Type.MCQ, question.getType());
        assertEquals(AnswerOption.A,
                ((MCQ_Answer) question.getAnswer()).getCorrectOption());
    }

    @Test
    void setTypeAndAnswerRejectsNullValuesWithoutChangingExistingState() {
        Question question = new Question(6, "q.png", 2021, Topic.Graph_Theory,
                Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of());

        assertThrows(IllegalArgumentException.class,
                () -> question.setTypeAndAnswer(null, new MCQ_Answer(AnswerOption.B)));
        assertThrows(IllegalArgumentException.class,
                () -> question.setTypeAndAnswer(Type.MCQ, null));

        assertEquals(Type.MCQ, question.getType());
        assertEquals(AnswerOption.A,
                ((MCQ_Answer) question.getAnswer()).getCorrectOption());
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
