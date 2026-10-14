package org.example.questionBankApplication.Mappers;
import org.example.questionBankApplication.model.Question;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public final class Question_Initializer {

    // Prevent instantiation
    private Question_Initializer() {}

    public static List<Question> initializeFromDirectory(Path imageDirectory) throws IOException {

        if (imageDirectory == null || !Files.isDirectory(imageDirectory)) {throw new IllegalArgumentException("Invalid image directory: " + imageDirectory);}
        List<Question> questions = new ArrayList<>();
        try (Stream<Path> paths = Files.list(imageDirectory)) {
            List<Path> imageFiles = paths.filter(Files::isRegularFile)
                                .filter(Question_Initializer::isImageFile)
                                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                                .toList();

            int id = 1;
            for (Path imageFile : imageFiles) {
                String imagePath = imageFile.toString();
                Question question = new Question(id, imagePath, -9999, null, null, null, null);
                questions.add(question);
                id++;
            }
        }
        System.out.printf("\n\n\n\nNumber of Questions Object Created from Images: %d\n\n\n\n ", questions.size());
        return questions;
    }

    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");
    private static boolean isImageFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0) {return false;}
        String extension = fileName.substring(dotIndex + 1);
        return IMAGE_EXTENSIONS.contains(extension);
    }
}
