package org.example.questionBankApplication.Mappers;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MSQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JsonAnswerFormatsIntegrationTest {

    @TempDir
    Path directory;

    @Test
    void roundTripsMsqAnswerAndTags() throws IOException {
        Question question = new Question(41, "Question_Images/msq.png", 2022,
                Topic.SQL, Type.MSQ, null, Set.of(Tag.IMPORTANT));
        question.setMsqAnswer(Set.of(AnswerOption.B, AnswerOption.D));

        JSON_Writer.writeQuestions(List.of(question), directory);
        Question restored = JSON_Reader.readQuestions(directory).get(0);

        assertEquals(Type.MSQ, restored.getType());
        assertEquals(Set.of(AnswerOption.B, AnswerOption.D),
                ((MSQ_Answer) restored.getAnswer()).getCorrectOptions());
        assertEquals(Set.of(Tag.IMPORTANT), restored.getQuestionTags());
    }

    @Test
    void roundTripsNatRange() throws IOException {
        Question question = new Question(42, "Question_Images/nat.png", 2023,
                Topic.SQL, Type.NAT, null, Set.of(Tag.REPEATED));
        question.setNatAnswer(-0.25, 10.75);

        JSON_Writer.writeQuestions(List.of(question), directory);
        Question restored = JSON_Reader.readQuestions(directory).get(0);

        assertEquals(Type.NAT, restored.getType());
        NAT_Answer answer = assertInstanceOf(NAT_Answer.class, restored.getAnswer());
        assertEquals(-0.25, answer.getMinValue());
        assertEquals(10.75, answer.getMaxValue());
        assertEquals(Set.of(Tag.REPEATED), restored.getQuestionTags());
    }

    @Test
    void roundTripsAQuestionWithIncompleteMetadata() throws IOException {
        Question question = new Question(43, "Question_Images/incomplete.png", 0,
                null, null, null, null);

        JSON_Writer.writeQuestions(List.of(question), directory);
        Question restored = JSON_Reader.readQuestions(directory).get(0);

        assertEquals(43, restored.getId());
        assertNull(restored.getSubject());
        assertNull(restored.getTopic());
        assertNull(restored.getType());
        assertNull(restored.getAnswer());
        assertNull(restored.getQuestionTags());
    }
}
