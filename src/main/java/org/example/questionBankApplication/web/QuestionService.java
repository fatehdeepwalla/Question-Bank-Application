package org.example.questionBankApplication.web;

import org.example.questionBankApplication.Mappers.JSON_Reader;
import org.example.questionBankApplication.model.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Loads the existing per-question JSON files once the application's startup
 * runners have completed. The web UI reads from this shared in-memory catalog.
 */

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
            throw new IllegalStateException("Could not load questions from JSON directory: " + jsonDirectory, exception);
        }
    }

    public List<Question> findAll() {
        return questions;
    }
}

