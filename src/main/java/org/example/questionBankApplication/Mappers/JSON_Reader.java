
package org.example.questionBankApplication.Mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.*;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

        if (inputDirectory == null || !Files.isDirectory(inputDirectory)) {
            throw new IllegalArgumentException("Invalid image directory: " + inputDirectory);
        }

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
            throw new IOException("Failed to read question file " + jsonFile + ": " + exception.getMessage(), exception);
        } catch (RuntimeException exception) {
            throw new IOException("Invalid question data in " + jsonFile + ": " + exception.getMessage(), exception);
        }
    }
    private static Question deserializeQuestion(Path jsonFile) throws IOException {

        JsonNode root = OBJECT_MAPPER.readTree(jsonFile.toFile());
        if (root == null || !root.isObject()) {throw new IOException("There is no JSON object in " + jsonFile);}

        // Reading from JSON file
        int id = readInt(root, "id");
        String imagePath = readImagePath(root);
        int year = readInt(root, "year");
        Topic topic = readTopic(root);
        Subject subject = readSubject(root);
        Type type = readType(root);
        Answer answer = readAnswer(root, type);
        Set<Tag> questionTags = readTags(root);

        // Validation
        checkFilenameMatchesID(jsonFile, id);
        checkTopicMatchesSubject(topic, subject);
        checkTypeMatchesAnswer(type, answer);


        boolean notClassified=
                year==-9999 &&
                topic == null &&
                subject ==null &&
                type == null &&
                answer == null &&
                questionTags == null;
        boolean classified=
                !(year<1995 || year >2030) &&
                topic != null &&
                subject != null &&
                type != null &&
                answer!= null &&
                questionTags != null;

        if(notClassified) {
            return new Question(id, imagePath);
        }
        else if(classified) {
            return new Question(id, imagePath, year, topic, type, answer, questionTags);
        }
        else{
            throw new IOException("Invalid question data in " + jsonFile);
        }
    }



    private static void checkFilenameMatchesID (Path jsonFile, int id) throws IOException {
        String actualFileName = jsonFile.getFileName().toString();
        String expectedFileName = "question-" + id + ".json";
        if (!expectedFileName.equals(actualFileName)) {
            throw new IOException("Filename '" + actualFileName
                    + "' does not match embedded question ID " + id
                    + " (expected '" + expectedFileName + "')");
        }
    }
    private static void checkTopicMatchesSubject(Topic topic, Subject subject) throws IOException {
        if (topic == null && subject == null) {return;}
        if (!subject.equals(SubjectTopicMap.getSubject(topic))) {
            throw new IOException("Subject does not match the subject derived from topic");
        }
    }
    private static void checkTypeMatchesAnswer(Type type, Answer answer) throws IOException {
        if(answer == null && type!=null) {throw new IOException("Answer is null but type is not");}
        if(answer != null && type==null) {throw new IOException("Answer is not null but type is null");}
        if (type == null && answer == null) {return;}
        boolean compatible = switch (type) {
            case MCQ -> answer instanceof MCQ_Answer;
            case MSQ -> answer instanceof MSQ_Answer;
            case NAT -> answer instanceof NAT_Answer;
        };
        if (!compatible) {throw new IllegalArgumentException("Answer type does not match question type: " + type);}
    }

    private static int readInt(JsonNode object, String fieldName) throws IOException {
        JsonNode node = requiredNode(object, fieldName);
        if (!node.isIntegralNumber() || !node.canConvertToInt()) {
            throw new IOException("'" + fieldName + "' must be an integer");
        }
        return node.intValue();
    }
    private static double readDouble(JsonNode object, String fieldName) throws IOException {
        JsonNode node = requiredNode(object, fieldName);
        if (!node.isNumber()) {
            throw new IOException("'" + fieldName + "' must be a number");
        }
        return node.doubleValue();
    }
    private static String readImagePath(JsonNode object) throws IOException {
        JsonNode node = requiredNode(object, "imagePath");
        if (!node.isTextual()) {throw new IOException("'" + "imagePath" + "' must be a string");}
        return node.textValue();
    }
    private static Topic readTopic(JsonNode object) throws IOException {
        JsonNode topicNode = requiredNode(object, "topic");
        return topicNode.isNull() ? null : OBJECT_MAPPER.treeToValue(topicNode, Topic.class);
    }
    private static Subject readSubject(JsonNode object) throws IOException {
        JsonNode subjectNode = requiredNode(object, "subject");
        return subjectNode.isNull() ? null : OBJECT_MAPPER.treeToValue(subjectNode, Subject.class);
    }
    private static Type readType(JsonNode object) throws IOException {
        JsonNode typeNode = requiredNode(object, "type");
        return typeNode.isNull() ? null : OBJECT_MAPPER.treeToValue(typeNode, Type.class);
    }
    private static Set<Tag> readTags(JsonNode object) throws IOException {
        JsonNode tagsNode = requiredNode(object, "questionTags");
        Set<Tag> questionTags;
        if (tagsNode.isNull()) { questionTags = null;}
        else {
            if (!tagsNode.isArray()) {throw new IOException("'questionTags' must be an array or null");}
            questionTags = OBJECT_MAPPER.convertValue(tagsNode, TAG_SET_TYPE);
        }
        return questionTags;
    }
    private static Answer readAnswer(JsonNode object, Type type) throws IOException {
        JsonNode answerNode = requiredNode(object, "answer");
        if (answerNode.isNull()) {return null;}
        else {
            if (!answerNode.isObject()) {throw new IOException("'answer' must be a JSON object or null");}
            switch (type) {
                case MCQ -> {
                    JsonNode optionNode = requiredNode(answerNode, "correctOption");
                    AnswerOption correctOption = OBJECT_MAPPER.treeToValue(optionNode, AnswerOption.class);
                    return new MCQ_Answer(correctOption);
                }

                case MSQ -> {
                    JsonNode optionsNode = requiredNode(answerNode, "correctOptions");
                    if (!optionsNode.isArray()) {throw new IOException("'correctOptions' must be an array");}
                    Set<AnswerOption> correctOptions = OBJECT_MAPPER.convertValue(optionsNode, ANSWER_OPTION_SET_TYPE);
                    return new MSQ_Answer(correctOptions);
                }

                case NAT -> {
                    double minValue = readDouble(answerNode, "minValue");
                    double maxValue = readDouble(answerNode, "maxValue");
                    return new NAT_Answer(minValue, maxValue);
                }
            }
        }
        throw new IOException("Invalid answer type " + type);
    }


    private static JsonNode requiredNode(JsonNode object, String fieldName) throws IOException {
        if (!object.has(fieldName)) {throw new IOException("Required JSON property is missing: " + fieldName);}
        return object.get(fieldName);
    }
    private static boolean isJsonFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json");
    }
}
