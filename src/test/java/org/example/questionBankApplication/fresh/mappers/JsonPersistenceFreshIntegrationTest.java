package org.example.questionBankApplication.fresh.mappers;

import org.example.questionBankApplication.Mappers.JSON_Reader;
import org.example.questionBankApplication.Mappers.JSON_Writer;
import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.MSQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JsonPersistenceFreshIntegrationTest {

    @TempDir
    Path directory;

    private static Question unclassified(int id) {
        return new Question(id, "Question_Images/q" + id + ".png");
    }

    private static Question mcq(int id) {
        return new Question(id, "Question_Images/q" + id + ".png", 2024,
                Topic.Sorting, Type.MCQ, new MCQ_Answer(AnswerOption.B),
                Set.of(Tag.IMPORTANT, Tag.REPEATED));
    }

    private static Question msq(int id) {
        return new Question(id, "Question_Images/q" + id + ".png", 2022,
                Topic.SQL, Type.MSQ, new MSQ_Answer(Set.of(AnswerOption.A, AnswerOption.C)),
                Set.of(Tag.SOLVED));
    }

    private static Question nat(int id) {
        return new Question(id, "Question_Images/q" + id + ".png", 1995,
                Topic.Calculus, Type.NAT, new NAT_Answer(-1.25, 3.5), Set.of());
    }

    @Test
    void writerAndReaderRoundTripUnclassifiedAndEveryAnswerType() throws Exception {
        JSON_Writer.writeQuestions(List.of(unclassified(1), mcq(2), msq(3), nat(4)), directory);

        Map<Integer, Question> loaded = JSON_Reader.readQuestions(directory).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        assertEquals(Set.of(1, 2, 3, 4), loaded.keySet());

        Question unclassified = loaded.get(1);
        assertEquals(-9999, unclassified.getYear());
        assertNull(unclassified.getSubject());
        assertNull(unclassified.getTopic());
        assertNull(unclassified.getType());
        assertNull(unclassified.getAnswer());
        assertNull(unclassified.getQuestionTags());

        Question loadedMcq = loaded.get(2);
        assertEquals(2024, loadedMcq.getYear());
        assertEquals(Topic.Sorting, loadedMcq.getTopic());
        assertEquals(Type.MCQ, loadedMcq.getType());
        assertEquals(AnswerOption.B, ((MCQ_Answer) loadedMcq.getAnswer()).getCorrectOption());
        assertEquals(Set.of(Tag.IMPORTANT, Tag.REPEATED), loadedMcq.getQuestionTags());

        Question loadedMsq = loaded.get(3);
        assertEquals(Set.of(AnswerOption.A, AnswerOption.C),
                ((MSQ_Answer) loadedMsq.getAnswer()).getCorrectOptions());
        assertEquals(Set.of(Tag.SOLVED), loadedMsq.getQuestionTags());

        Question loadedNat = loaded.get(4);
        NAT_Answer natAnswer = (NAT_Answer) loadedNat.getAnswer();
        assertEquals(-1.25, natAnswer.getMinValue());
        assertEquals(3.5, natAnswer.getMaxValue());
        assertTrue(loadedNat.getQuestionTags().isEmpty());
    }

    @Test
    void readerIgnoresNonJsonFilesAndJsonNamedDirectories() throws Exception {
        JSON_Writer.writeQuestions(List.of(unclassified(1)), directory);
        Files.writeString(directory.resolve("notes.txt"), "not a question");
        Files.createDirectory(directory.resolve("ignored.json"));
        Files.writeString(directory.resolve("data.JSON.bak"), "{}");

        List<Question> questions = JSON_Reader.readQuestions(directory);
        assertEquals(1, questions.size());
        assertEquals(1, questions.getFirst().getId());
    }

    @Test
    void readerRejectsPartiallyClassifiedRecords() throws Exception {
        writeJson("question-1.json", """
                {
                  "id": 1,
                  "imagePath": "q1.png",
                  "year": 2020,
                  "subject": null,
                  "topic": null,
                  "type": null,
                  "answer": null,
                  "questionTags": null
                }
                """);

        assertThrows(IOException.class, () -> JSON_Reader.readQuestions(directory));
    }

    @Test
    void readerRejectsSubjectThatDoesNotOwnTheStoredTopic() throws Exception {
        writeJson("question-1.json", """
                {
                  "id": 1,
                  "imagePath": "q1.png",
                  "year": 2020,
                  "subject": "Digital_Logic",
                  "topic": "Sorting",
                  "type": "MCQ",
                  "answer": {"correctOption": "A"},
                  "questionTags": []
                }
                """);

        assertThrows(IOException.class, () -> JSON_Reader.readQuestions(directory));
    }

    @Test
    void readerRejectsAnAnswerObjectThatDoesNotMatchItsDeclaredType() throws Exception {
        writeJson("question-1.json", """
                {
                  "id": 1,
                  "imagePath": "q1.png",
                  "year": 2020,
                  "subject": "Algorithms",
                  "topic": "Sorting",
                  "type": "MCQ",
                  "answer": {"correctOptions": ["A", "B"]},
                  "questionTags": []
                }
                """);

        assertThrows(IOException.class, () -> JSON_Reader.readQuestions(directory));
    }

    @Test
    void readerRejectsMissingRequiredFieldsAndMismatchedFilenames() throws Exception {
        writeJson("question-1.json", """
                {
                  "id": 1,
                  "imagePath": "q1.png",
                  "year": -9999,
                  "subject": null,
                  "topic": null,
                  "type": null,
                  "answer": null
                }
                """);
        assertThrows(IOException.class, () -> JSON_Reader.readQuestions(directory));

        Files.delete(directory.resolve("question-1.json"));
        writeJson("another-name.json", """
                {
                  "id": 1,
                  "imagePath": "q1.png",
                  "year": -9999,
                  "subject": null,
                  "topic": null,
                  "type": null,
                  "answer": null,
                  "questionTags": null
                }
                """);
        assertThrows(IOException.class, () -> JSON_Reader.readQuestions(directory));
    }

    @Test
    void readerRejectsInvalidNaturalAnswerRanges() throws Exception {
        writeJson("question-1.json", """
                {
                  "id": 1,
                  "imagePath": "q1.png",
                  "year": 2024,
                  "subject": "Engineering_Mathematics",
                  "topic": "Calculus",
                  "type": "NAT",
                  "answer": {"minValue": 9.0, "maxValue": 3.0},
                  "questionTags": []
                }
                """);

        assertThrows(IOException.class, () -> JSON_Reader.readQuestions(directory));
    }

    @Test
    void writerValidatesDuplicateIdsBeforeCreatingAnyFiles() {
        Question first = unclassified(9);
        Question second = new Question(9, "Question_Images/other.png");

        assertThrows(IllegalArgumentException.class,
                () -> JSON_Writer.writeQuestions(List.of(first, second), directory));

        try (var children = Files.list(directory)) {
            assertEquals(0, children.count());
        } catch (IOException exception) {
            fail(exception);
        }
    }

    @Test
    void writerRejectsNullArgumentsAndAPathThatIsNotAnExistingDirectory() {
        assertThrows(IllegalArgumentException.class, () -> JSON_Writer.writeQuestions(null, directory));
        assertThrows(IllegalArgumentException.class, () -> JSON_Writer.writeQuestions(List.of(), null));
        assertThrows(IOException.class,
                () -> JSON_Writer.writeQuestions(List.of(unclassified(1)), directory.resolve("missing")));
    }

    private void writeJson(String filename, String content) throws IOException {
        Files.writeString(directory.resolve(filename), content);
    }
}
