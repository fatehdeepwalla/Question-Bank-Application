package org.example.questionBankApplication.fresh.web;

import org.example.questionBankApplication.Mappers.JSON_Reader;
import org.example.questionBankApplication.Mappers.JSON_Writer;
import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionServiceFreshIntegrationTest {

    @TempDir
    Path directory;

    private QuestionService service;

    private static Question unclassified(int id) {
        return new Question(id, "Question_Images/q" + id + ".png");
    }

    private static Question classified(int id, int year, AnswerOption option, Tag... tags) {
        return new Question(id, "Question_Images/q" + id + ".png", year,
                Topic.Sorting, Type.MCQ, new MCQ_Answer(option), Set.of(tags));
    }

    @BeforeEach
    void loadInitialCatalog() throws IOException {
        JSON_Writer.writeQuestions(List.of(unclassified(3), classified(1, 2020, AnswerOption.A)), directory);
        service = new QuestionService(directory.toString());
        service.loadQuestionsAfterStartup();
    }

    @Test
    void loadsCatalogSortedByIdAndSupportsOptionalLookup() {
        assertEquals(List.of(1, 3), service.findAll().stream().map(Question::getId).toList());
        assertEquals(1, service.findById(1).orElseThrow().getId());
        assertTrue(service.findById(99).isEmpty());
    }

    @Test
    void catalogListIsImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> service.findAll().add(unclassified(7)));
    }

    @Test
    void updatePersistsToDiskAndPublishesTheReplacementObject() throws Exception {
        List<Question> oldSnapshot = service.findAll();
        Question updated = classified(1, 2025, AnswerOption.D, Tag.IMPORTANT, Tag.SOLVED);

        service.updateQuestion(updated);

        assertSame(updated, service.findById(1).orElseThrow());
        assertEquals(2025, service.findById(1).orElseThrow().getYear());
        assertEquals(2, oldSnapshot.size());
        assertEquals(2020, oldSnapshot.get(0).getYear());
        Question reloaded = JSON_Reader.readQuestions(directory).stream()
                .filter(question -> question.getId() == 1)
                .findFirst().orElseThrow();
        assertEquals(2025, reloaded.getYear());
        assertEquals(AnswerOption.D, ((MCQ_Answer) reloaded.getAnswer()).getCorrectOption());
        assertEquals(Set.of(Tag.IMPORTANT, Tag.SOLVED), reloaded.getQuestionTags());
    }

    @Test
    void rejectsNullOrUnknownUpdatesWithoutChangingTheCatalog() {
        List<Question> before = service.findAll();

        assertThrows(IllegalArgumentException.class, () -> service.updateQuestion(null));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateQuestion(classified(99, 2025, AnswerOption.B)));

        assertSame(before, service.findAll());
        assertEquals(List.of(1, 3), service.findAll().stream().map(Question::getId).toList());
        assertFalse(Files.exists(directory.resolve("question-99.json")));
    }

    @Test
    void failedDiskWriteLeavesTheInMemoryCatalogUnchanged() throws Exception {
        Question original = service.findById(1).orElseThrow();
        Path target = directory.resolve("question-1.json");
        Files.delete(target);
        Files.createDirectory(target);

        assertThrows(IOException.class,
                () -> service.updateQuestion(classified(1, 2026, AnswerOption.C)));

        assertSame(original, service.findById(1).orElseThrow());
        assertEquals(2020, service.findById(1).orElseThrow().getYear());
    }
}
