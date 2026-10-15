
package org.example.questionBankApplication.web.controllerAndHelpers;

import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class QuestionFilter {

    public enum TagState {INCLUDE("include"), NEUTRAL("neutral"), EXCLUDE("exclude");
        private final String value;
        TagState(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    private final Integer year;
    private final String selectedYear;
    private final Subject subject;
    private final Topic topic;
    private final String sortBy;
    private final String direction;

    private final EnumMap<Tag, TagState> tagStates = new EnumMap<>(Tag.class);

    private final List<String> validationErrors;

    private QuestionFilter(Map<String, String> parameters) {
        Map<String, String> params = parameters == null ? Map.of() : parameters;
        List<String> errors = new ArrayList<>();

        selectedYear = valueOf(params, "year");
        year = parseYear(selectedYear, errors);
        subject = parseEnum(Subject.class, valueOf(params, "subject"), "subject", errors);

        String topicText = valueOf(params, "topic");
        Topic parsedTopic = parseEnum(Topic.class, topicText, "topic", errors);

        Topic validTopic = null;

        if (!topicText.isBlank() && parsedTopic != null) {
            if (subject == null) {
                errors.add("Select a valid subject before selecting a topic.");
            } else if (!SubjectTopicMap.isValidTopic(subject, parsedTopic)) {
                errors.add("The selected topic does not belong to the selected subject.");
            } else {
                validTopic = parsedTopic;
            }
        }

        topic = validTopic;

        String requestedSort = valueOf(params, "sortBy");

        if (requestedSort.isBlank() || requestedSort.equals("id")
                || requestedSort.equals("year")) {
            sortBy = requestedSort.isBlank() ? "id" : requestedSort;
        } else {
            errors.add("Invalid sorting field. Defaulting to Question ID.");
            sortBy = "id";
        }

        String requestedDirection = valueOf(params, "direction");

        if (requestedDirection.isBlank()
                || requestedDirection.equals("asc")
                || requestedDirection.equals("desc")) {
            direction = requestedDirection.isBlank()
                    ? "asc"
                    : requestedDirection;
        } else {
            errors.add("Invalid sorting order. Defaulting to ascending.");
            direction = "asc";
        }

        for (Tag tag : Tag.values()) {
            String requestedState = params.get("tag_" + tag.name());

            tagStates.put(
                    tag,
                    parseTagState(requestedState, tag, errors)
            );
        }

        validationErrors = List.copyOf(errors);
    }

    public static QuestionFilter fromParameters(Map<String, String> parameters) {
        return new QuestionFilter(parameters);
    }

    private static String valueOf(Map<String, String> parameters, String key) {
        String value = parameters.get(key);
        return value == null ? "" : value.trim();
    }

    private static Integer parseYear(String value, List<String> errors) {
        if (value.isBlank()) {return null;}
        try {
            int year = Integer.parseInt(value);
            if (year < 1990 || year > 2030) {
                errors.add("GATE year must be between 1990 and 2030.");
                return null;
            }
            return year;
        } catch (NumberFormatException exception) {
            errors.add("Enter a valid whole-number GATE year.");
            return null;
        }
    }

    private static <E extends Enum<E>> E parseEnum(Class<E> enumType, String value, String fieldName, List<String> errors) {
        if (value.isBlank()) {return null;}

        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException exception) {
            errors.add("Invalid " + fieldName + " selection.");
            return null;
        }
    }

    private static TagState parseTagState(String value, Tag tag, List<String> errors) {

        if (value == null || value.isBlank() || value.equals("neutral")) {
            return TagState.NEUTRAL;
        }

        return switch (value) {
            case "include" -> TagState.INCLUDE;
            case "exclude" -> TagState.EXCLUDE;
            default -> {
                errors.add("Invalid state for tag " + tag.name() + ".");
                yield TagState.NEUTRAL;
            }
        };
    }

    public Integer getYear() {
        return year;
    }
    public String getSelectedYear() {
        return selectedYear;
    }
    public Subject getSubject() {
        return subject;
    }
    public Topic getTopic() {
        return topic;
    }
    public String getSelectedSubject() {
        return subject == null ? "" : subject.name();
    }
    public String getSelectedTopic() {
        return topic == null ? "" : topic.name();
    }
    public String getSortBy() {
        return sortBy;
    }
    public String getDirection() {
        return direction;
    }
    public TagState getTagState(Tag tag) {
        return tagStates.getOrDefault(tag, TagState.NEUTRAL);
    }
    public Map<String, String> getTagStatesForView() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Tag tag : Tag.values()) {
            result.put(tag.name(), getTagState(tag).getValue());
        }
        return result;
    }
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}
