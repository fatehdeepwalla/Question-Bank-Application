package org.example.questionBankApplication.web;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.*;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** View model for displaying a Question without changing the domain model. */
public class QuestionDTO {

    //////////////////////////////////// Fields ////////////////////////////////////////
    // id
    private final int id;
    private final String idLabel;

    // image
    private final String imageUrl;

    // year
    private final String yearLabel;
    private final boolean yearAssigned;

    // subject and topic
    private final String subjectLabel;
    private final boolean subjectAssigned;
    private final String topicLabel;
    private final boolean topicAssigned;

    // type and answer
    private final String typeLabel;
    private final boolean typeAssigned;

    private final String answerLabel;
    private final boolean  answerAssigned;

    // tags
    private final List<String> tagLabels;

    private final boolean classified;

    //////////////////////////////////// Constructor ////////////////////////////////////////
    public QuestionDTO(Question question) {
        this.id = question.getId();
        this.idLabel = String.format(Locale.ROOT, "QB-%04d", question.getId());

        this.imageUrl = buildImageUrl(question.getImagePath());

        int year = question.getYear();
        this.yearAssigned = (year!=-9999);
        this.yearLabel = yearAssigned ? Integer.toString(year) : "Year not set";

        Subject subject = question.getSubject();
        this.subjectAssigned = subject != null;
        this.subjectLabel = subjectAssigned ? subject.name() : "Subject not assigned";

        Topic topic = question.getTopic();
        this.topicAssigned = topic != null;
        this.topicLabel = topicAssigned ? topic.name() : "Topic not assigned"  ;

        Type type = question.getType();
        this.typeAssigned = type != null;
        this.typeLabel = typeAssigned ? type.name() : "Type not set" ;

        Answer answer = question.getAnswer();
        this.answerAssigned = question.getAnswer() != null;
        this.answerLabel = answerAssigned ? buildAnswerLabel(answer) : "Answer not assigned" ;


        List<String> tags = new ArrayList<>();
        if (question.getQuestionTags() != null) {
            for (Tag tag : question.getQuestionTags()) {
                if (tag != null) {tags.add(tag.name());}
            }
        }
        this.tagLabels = List.copyOf(tags);

        this.classified = yearAssigned && subjectAssigned && topicAssigned && typeAssigned && answerAssigned;
    }


    //////////////////////////////////// Getters ////////////////////////////////////////

    public int getId() { return id; }
    public String getIdLabel() { return idLabel; }

    public String getImageUrl() { return imageUrl; }

    public String getYearLabel() { return yearLabel; }
    public boolean isYearAssigned() { return yearAssigned; }

    public String getSubjectLabel() { return subjectLabel; }
    public boolean isSubjectAssigned() { return subjectAssigned; }
    public String getTopicLabel() { return topicLabel; }
    public boolean isTopicAssigned() { return topicAssigned; }

    public String getTypeLabel() { return typeLabel; }
    public boolean isTypeAssigned() {return typeAssigned;}
    public String getAnswerLabel() {return answerLabel;}
    public boolean isAnswerAssigned() {return answerAssigned;}

    public List<String> getTagLabels() { return tagLabels; }
    public boolean isClassified() { return classified; }

    public String getStatusLabel() { return classified ? "Classified" : "Needs details";}
    public String getStatusClass() { return classified ? "status-pill status-ready" : "status-pill status-review";}
    public String getYearClass() { return yearAssigned ? "metadata-value" : "metadata-value value-missing"; }

    //////////////////////////////////// Helper Methods ////////////////////////////////////////
    private static String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {throw new RuntimeException("A valid image path should always be there inside a question");}

        // A URL which can be accepted in HTML
        String normalized = imagePath.replace('\\', '/');

        // Extracting the filename
        int lastSlash = normalized.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        if (fileName.isBlank() || fileName.equals(".") || fileName.equals("..")) {return null;}

        // To replace the white spaces as they will crete problem in HTML
        return "/images-folder/" + UriUtils.encodePathSegment(fileName, StandardCharsets.UTF_8);
    }
    private static String buildAnswerLabel(Answer answer) {

        if (answer instanceof MCQ_Answer mcq) {
            return mcq.getCorrectOption().name();
        }

        if (answer instanceof MSQ_Answer msq) {
            return msq.getCorrectOptions()
                    .stream()
                    .map(AnswerOption::name)
                    .sorted()
                    .collect(Collectors.joining(", "));
        }

        if (answer instanceof NAT_Answer nat) {
            return nat.getMinValue() + " – " + nat.getMaxValue();
        }

        return "Unknown answer type";
    }

}
