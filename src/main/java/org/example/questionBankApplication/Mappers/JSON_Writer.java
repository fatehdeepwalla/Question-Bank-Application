package org.example.questionBankApplication.Mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.questionBankApplication.model.Question;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class JSON_Writer {
    // Prevent instantiation
    private JSON_Writer() {}
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void writeQuestions(List<Question> questions, Path outputDirectory) throws IOException {

        if (questions == null) {throw new IllegalArgumentException("Questions list cannot be null");}
        if (outputDirectory == null) {throw new IllegalArgumentException("Output directory cannot be null");}
        // Check if directory is there or not
        if (!Files.isDirectory(outputDirectory)) {throw new IOException("Output directory does not exist or is not a directory: " + outputDirectory);}

        // Check for duplicate IDs before writing any files.
        Set<Integer> ids = new HashSet<>();
        for (Question question : questions) {
            if (question == null) {throw new IllegalArgumentException("Questions list cannot contain null elements");}
            if (!ids.add(question.getId())) {throw new IllegalArgumentException("Duplicate question ID: " + question.getId());}
        }

        // Write each question to its own JSON file.
        int count = 0;
        for (Question question : questions) {
            Path jsonFile = outputDirectory.resolve("question-" + question.getId() + ".json");
            OBJECT_MAPPER.writeValue(jsonFile.toFile(), question);
            count++;
        }
        System.out.printf("\nNumber of Questions to Write: %d\n", questions.size());
        System.out.printf("Number of JSON Files Written: %d\n",count);
    }

    /**
     * Atomically replaces one existing question JSON file.
     * The complete document is serialized to a temporary file first, so an
     * unsuccessful serialization will not truncate the existing question file.
     */
    public static void writeQuestion(Question question, Path outputDirectory) throws IOException {
        if (question == null) {
            throw new IllegalArgumentException("Question cannot be null");
        }
        if (outputDirectory == null) {
            throw new IllegalArgumentException("Output directory cannot be null");
        }

        Path directory = outputDirectory.toAbsolutePath().normalize();
        if (!Files.isDirectory(directory)) {
            throw new IOException("Output directory does not exist or is not a directory: " + directory);
        }

        Path target = directory.resolve("question-" + question.getId() + ".json").normalize();
        if (!directory.equals(target.getParent())) {
            throw new IOException("The question file path is not inside the JSON directory.");
        }
        if (!Files.isRegularFile(target)) {
            throw new IOException("The existing question file was not found: " + target.getFileName());
        }

        Path temporaryFile = Files.createTempFile(directory,
                ".question-" + question.getId() + "-", ".tmp");
        try {
            OBJECT_MAPPER.writeValue(temporaryFile.toFile(), question);
            try {
                Files.move(temporaryFile, target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}