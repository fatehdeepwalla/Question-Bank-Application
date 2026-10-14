package org.example.questionBankApplication.runners;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.questionBankApplication.Mappers.JSON_Reader;
import org.example.questionBankApplication.model.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@ConditionalOnProperty(name = "question-bank.read", havingValue = "true")
public class ReaderRunner implements CommandLineRunner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Value("${question-bank.JSON-directory}")
    private String readDirectory;

    @Override
    public void run(String... args) throws Exception {

        // Step 1: Read JSON files and reconstruct Question objects.
        List<Question> questions = JSON_Reader.readQuestions(Path.of(readDirectory));

        // Step 2: Print the reconstructed objects as JSON.
        String json = OBJECT_MAPPER.writeValueAsString(questions);

        System.out.println("\n========== LOADED QUESTIONS (JSON) ==========");
        System.out.println(json);
        System.out.println("==============================================\n");
    }
}