
package org.example.questionBankApplication.Mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.*;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

public final class JSON_Reader {
    // Prevent instantiation
    private JSON_Reader() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Set<Tag>> TAG_SET_TYPE = new TypeReference<>() {};
    private static final TypeReference<Set<AnswerOption>> ANSWER_OPTION_SET_TYPE = new TypeReference<>() {};

    public static List<Question> readQuestions(Path inputDirectory) throws IOException {

        if (inputDirectory == null) {throw new IllegalArgumentException("Input directory cannot be null");}
        if (!Files.isDirectory(inputDirectory)) {throw new IOException("Input directory does not exist or is not a directory: " + inputDirectory);}

        List<Path> jsonFiles;
        try (Stream<Path> paths = Files.list(inputDirectory)) {
            jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(JSON_Reader::isJsonFile)
                    .toList();
        }

        List<Question> questions = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();

        int count = 0;
        for (Path jsonFile : jsonFiles) {
            Question question = readQuestionFile(jsonFile);
            count++;
            if (!ids.add(question.getId())) {
                throw new IOException("Duplicate question ID " + question.getId() + " found while reading " + jsonFile);
            }
            questions.add(question);
        }
        System.out.printf("\nNumber of JSON Files Read: %d\n",count);
        System.out.printf("Number of Questions Created After Reading: %d\n",questions.size());

        return questions;
    }

    private static Question readQuestionFile(Path jsonFile) throws IOException {

        try {
            return deserializeQuestion(jsonFile);
        } catch (IOException exception) {
            throw new IOException(
                    "Failed to read question file " + jsonFile + ": " + exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid question data in " + jsonFile + ": " + exception.getMessage(), exception);
        }
    }
    private static Question deserializeQuestion(Path jsonFile) throws IOException {

        JsonNode root = OBJECT_MAPPER.readTree(jsonFile.toFile());
        if (root == null || !root.isObject()) {throw new IOException("JSON must contain a question object");}

        int id = readInt(root, "id");
        String imagePath = readString(root, "imagePath");
        int year = readInt(root, "year");

        // Read topic. A null topic is permitted by the model.
        JsonNode topicNode = requiredNode(root, "topic");
        Topic topic = topicNode.isNull() ? null : OBJECT_MAPPER.treeToValue(topicNode, Topic.class);

        // Read tags. A null tag set is permitted by the model.
        JsonNode tagsNode = requiredNode(root, "questionTags");
        Set<Tag> questionTags;
        if (tagsNode.isNull()) { questionTags = null;}
        else {
            if (!tagsNode.isArray()) {throw new IOException("'questionTags' must be an array or null");}
            questionTags = OBJECT_MAPPER.convertValue(tagsNode, TAG_SET_TYPE);
        }

        // Read question type. It can be null for an incomplete question.
        JsonNode typeNode = requiredNode(root, "type");
        Type type = typeNode.isNull() ? null : OBJECT_MAPPER.treeToValue(typeNode, Type.class);

        JsonNode answerNode = requiredNode(root, "answer");
        if (!answerNode.isNull() && type == null) {throw new IOException("A non-null answer requires a question type");}

        // Construct the question without its answer first.
        Question question = new Question(id, imagePath, year, topic, questionTags, type, null);

        // Verify the serialized subject agrees with the topic mapping.
        JsonNode storedSubject = requiredNode(root, "subject");
        JsonNode expectedSubject = OBJECT_MAPPER.valueToTree(question.getSubject());
        if (!storedSubject.equals(expectedSubject)) {
            throw new IOException("Subject does not match the subject derived from topic");
        }

        // Reconstruct the appropriate concrete answer through Question's setters.
        if (!answerNode.isNull()) {readAnswer(question, type, answerNode);}
        return question;
    }
    private static void readAnswer(Question question, Type type, JsonNode answerNode) throws IOException {
        if (!answerNode.isObject()) {throw new IOException("'answer' must be a JSON object or null");}
        switch (type) {
            case MCQ -> {
                JsonNode optionNode = requiredNode(answerNode, "correctOption");
                AnswerOption correctOption = OBJECT_MAPPER.treeToValue(optionNode, AnswerOption.class);
                question.setMcqAnswer(correctOption);
            }

            case MSQ -> {
                JsonNode optionsNode = requiredNode(answerNode, "correctOptions");
                if (!optionsNode.isArray()) {throw new IOException("'correctOptions' must be an array");}
                Set<AnswerOption> correctOptions = OBJECT_MAPPER.convertValue(optionsNode, ANSWER_OPTION_SET_TYPE);
                question.setMsqAnswer(correctOptions);
            }

            case NAT -> {
                double minValue = readDouble(answerNode, "minValue");
                double maxValue = readDouble(answerNode, "maxValue");
                question.setNatAnswer(minValue, maxValue);
            }
        }
    }
    private static JsonNode requiredNode(JsonNode object, String fieldName) throws IOException {
        if (!object.has(fieldName)) {throw new IOException("Required JSON property is missing: " + fieldName);}
        return object.get(fieldName);
    }
    private static int readInt(JsonNode object, String fieldName) throws IOException {
        JsonNode node = requiredNode(object, fieldName);
        if (!node.isIntegralNumber() || !node.canConvertToInt()) {
            throw new IOException("'" + fieldName + "' must be an integer");
        }
        return node.intValue();
    }
    private static String readString(JsonNode object, String fieldName) throws IOException {
        JsonNode node = requiredNode(object, fieldName);
        if (!node.isTextual()) {throw new IOException("'" + fieldName + "' must be a string");}
        return node.textValue();
    }
    private static double readDouble(JsonNode object, String fieldName) throws IOException {
        JsonNode node = requiredNode(object, fieldName);
        if (!node.isNumber()) {
            throw new IOException("'" + fieldName + "' must be a number");
        }
        return node.doubleValue();
    }
    private static boolean isJsonFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }
}
