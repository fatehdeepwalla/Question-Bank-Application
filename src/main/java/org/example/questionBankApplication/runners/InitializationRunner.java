
package org.example.questionBankApplication.runners;

import org.example.questionBankApplication.Mappers.JSON_Writer;
import org.example.questionBankApplication.Mappers.Question_Initializer;
import org.example.questionBankApplication.model.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
@ConditionalOnProperty(name = "question-bank.initialize", havingValue = "true")
public class InitializationRunner implements CommandLineRunner {

    @Value("${question-bank.images-directory}")
    private String imagesDirectory;

    @Value("${question-bank.initialization-directory}")
    private String initializationDirectory;

    @Override
    public void run(String... args) throws Exception {

        // Step 1: Create Question objects from the images.
        List<Question> questions = Question_Initializer.initializeFromDirectory(Path.of(imagesDirectory));

        // Step 2: Save those objects as JSON files.
        JSON_Writer.writeQuestions(questions, Path.of(initializationDirectory));

        System.out.println("\nInitialized " + questions.size() + " questions and saved their JSON files.\n");
    }
}
