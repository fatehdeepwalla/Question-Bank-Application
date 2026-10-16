package org.example.questionBankApplication.Mappers;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JsonPersistenceIntegrationTest {

    @TempDir
    Path directory;

    @Test
    void writesAndReadsMcqQuestionWithMetadata() throws IOException {
        Question original = new Question(17, "Question_Images/q17.png", 2022,
                Topic.Graph_Theory, Type.MCQ, null, Set.of(Tag.IMPORTANT, Tag.SOLVED));
        original.setMcqAnswer(AnswerOption.C);

        JSON_Writer.writeQuestions(List.of(original), directory);
        List<Question> loaded = JSON_Reader.readQuestions(directory);

        assertEquals(1, loaded.size());
        Question actual = loaded.get(0);
        assertEquals(original.getId(), actual.getId());
        assertEquals(original.getImagePath(), actual.getImagePath());
        assertEquals(2022, actual.getYear());
        assertEquals(Topic.Graph_Theory, actual.getTopic());
        assertEquals(original.getSubject(), actual.getSubject());
        assertEquals(Type.MCQ, actual.getType());
        assertEquals(AnswerOption.C, ((org.example.questionBankApplication.model.answer.MCQ_Answer)
                actual.getAnswer()).getCorrectOption());
        assertEquals(Set.of(Tag.IMPORTANT, Tag.SOLVED), actual.getQuestionTags());
    }

    @Test
    void updatingQuestionReplacesExistingJsonRecord() throws IOException {
        Question original = new Question(4, "Question_Images/q4.png", 2020,
                Topic.SQL, Type.MCQ, null, Set.of());
        original.setMcqAnswer(AnswerOption.A);
        JSON_Writer.writeQuestions(List.of(original), directory);

        Question updated = new Question(4, "Question_Images/q4.png", 2023,
                Topic.SQL, Type.MCQ, null, Set.of(Tag.REPEATED));
        updated.setMcqAnswer(AnswerOption.D);
        JSON_Writer.writeQuestion(updated, directory);

        Question persisted = JSON_Reader.readQuestions(directory).get(0);
        assertEquals(2023, persisted.getYear());
        assertEquals(AnswerOption.D,
                ((org.example.questionBankApplication.model.answer.MCQ_Answer) persisted.getAnswer())
                        .getCorrectOption());
        assertEquals(Set.of(Tag.REPEATED), persisted.getQuestionTags());
    }

    @Test
    void rejectsDuplicateIdsBeforeWriting() {
        Question first = new Question(1, "Question_Images/1.png", 2020,
                Topic.Graph_Theory, Type.MCQ, null, Set.of());
        Question second = new Question(1, "Question_Images/2.png", 2021,
                Topic.SQL, Type.MCQ, null, Set.of());

        assertThrows(IllegalArgumentException.class,
                () -> JSON_Writer.writeQuestions(List.of(first, second), directory));
        assertFalse(Files.exists(directory.resolve("question-1.json")));
    }

    @Test
    void readerRejectsFileWhoseNameDoesNotMatchEmbeddedId() throws IOException {
        Files.writeString(directory.resolve("wrong-name.json"),
                """
                {
                  "id": 3,
                  "imagePath": "Question_Images/q3.png",
                  "year": 2020,
                  "subject": "Discrete_Mathematics",
                  "topic": "Graph_Theory",
                  "type": null,
                  "answer": null,
                  "questionTags": []
                }
                """);

        IOException exception = assertThrows(IOException.class,
                () -> JSON_Reader.readQuestions(directory));
        assertTrue(exception.getMessage().contains("Failed to read question file"));
    }
}
