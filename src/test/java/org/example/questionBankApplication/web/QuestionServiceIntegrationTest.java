package org.example.questionBankApplication.web;

import org.example.questionBankApplication.Mappers.JSON_Reader;
import org.example.questionBankApplication.Mappers.JSON_Writer;
import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionServiceIntegrationTest {

    @TempDir
    Path jsonDirectory;

    private QuestionService service;

    @BeforeEach
    void setUp() throws IOException {
        Question first = new Question(8, "Question_Images/q8.png", 2021,
                Topic.Graph_Theory, Type.MCQ, new MCQ_Answer(AnswerOption.B), Set.of(Tag.IMPORTANT));
        Question second = new Question(2, "Question_Images/q2.png", 2020,
                Topic.SQL, Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of());
        JSON_Writer.writeQuestions(List.of(first, second), jsonDirectory);

        service = new QuestionService(jsonDirectory.toString());
        service.loadQuestionsAfterStartup();
    }

    @Test
    void loadsQuestionsInIdOrderAndSupportsLookup() {
        assertEquals(List.of(2, 8), service.findAll().stream().map(Question::getId).toList());
        assertTrue(service.findById(8).isPresent());
        assertTrue(service.findById(404).isEmpty());
    }

    @Test
    void updatePersistsToDiskAndPublishesUpdatedCatalog() throws IOException {
        Question updated = new Question(8, "Question_Images/q8.png", 2024,
                Topic.Graph_Theory, Type.MCQ, new MCQ_Answer(AnswerOption.D),
                Set.of(Tag.SOLVED, Tag.REPEATED));

        service.updateQuestion(updated);

        assertEquals(2024, service.findById(8).orElseThrow().getYear());
        assertEquals(List.of(2, 8), service.findAll().stream().map(Question::getId).toList());
        Question persisted = JSON_Reader.readQuestions(jsonDirectory).stream()
                .filter(question -> question.getId() == 8).findFirst().orElseThrow();
        assertEquals(2024, persisted.getYear());
        assertEquals(Set.of(Tag.SOLVED, Tag.REPEATED), persisted.getQuestionTags());
    }

    @Test
    void rejectsUpdatingUnknownQuestionWithoutChangingCatalog() {
        Question unknown = new Question(999, "Question_Images/q999.png", 2024,
                Topic.Graph_Theory, Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of());

        assertThrows(IllegalArgumentException.class, () -> service.updateQuestion(unknown));
        assertEquals(List.of(2, 8), service.findAll().stream().map(Question::getId).toList());
        assertFalse(Files.exists(jsonDirectory.resolve("question-999.json")));
    }
}
