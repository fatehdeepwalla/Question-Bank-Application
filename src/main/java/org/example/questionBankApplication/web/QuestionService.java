package org.example.questionBankApplication.web;

import org.example.questionBankApplication.Mappers.JSON_Reader;
import org.example.questionBankApplication.Mappers.JSON_Writer;
import org.example.questionBankApplication.model.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Loads and maintains the JSON-backed question catalog used by the web UI. */
@Service
public class QuestionService {

    private final Path jsonDirectory;
    private volatile List<Question> questions = List.of();

    public QuestionService(@Value("${question-bank.JSON-directory}") String jsonDirectory) {
        this.jsonDirectory = Path.of(jsonDirectory);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadQuestionsAfterStartup() {
        try {
            this.questions = JSON_Reader.readQuestions(jsonDirectory)
                    .stream()
                    .sorted(Comparator.comparingInt(Question::getId))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load questions from JSON directory: " + jsonDirectory, exception);
        }
    }

    public List<Question> findAll() {
        return questions;
    }

    public Optional<Question> findById(int id) {
        return questions.stream()
                .filter(question -> question.getId() == id)
                .findFirst();
    }

    /**
     * Persists an edit to the existing per-question JSON file before publishing
     * the replacement object to the in-memory catalog.
     */

    public synchronized void updateQuestion(Question updatedQuestion) throws IOException {
        if (updatedQuestion == null) {
            throw new IllegalArgumentException("Updated question cannot be null");
        }

        List<Question> currentQuestions = this.questions;
        int existingIndex = -1;
        for (int index = 0; index < currentQuestions.size(); index++) {
            if (currentQuestions.get(index).getId() == updatedQuestion.getId()) {
                existingIndex = index;
                break;
            }
        }
        if (existingIndex < 0) {
            throw new IllegalArgumentException("Question " + updatedQuestion.getId() + " does not exist.");
        }

        // Write first. If disk persistence fails, the in-memory question stays unchanged.
        JSON_Writer.writeQuestion(updatedQuestion, jsonDirectory);

        List<Question> replacement = new ArrayList<>(currentQuestions);
        replacement.set(existingIndex, updatedQuestion);
        this.questions = List.copyOf(replacement);
    }
}
