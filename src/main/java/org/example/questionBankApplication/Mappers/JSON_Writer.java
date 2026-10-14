package org.example.questionBankApplication.Mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.questionBankApplication.model.Question;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
}