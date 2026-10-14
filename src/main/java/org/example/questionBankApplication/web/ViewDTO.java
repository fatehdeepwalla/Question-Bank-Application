package org.example.questionBankApplication.web;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** View model for displaying a Question without changing the domain model. */
public class ViewDTO {

    private final int id;
    private final String idLabel;
    private final String imageUrl;
    private final String yearLabel;
    private final boolean yearAssigned;
    private final String subjectLabel;
    private final boolean subjectAssigned;
    private final String topicLabel;
    private final boolean topicAssigned;
    private final String typeLabel;
    private final List<String> tagLabels;
    private final boolean classified;
    private final String statusLabel;
    private final String statusClass;

    // Constructor
    public ViewDTO(Question question) {
        this.id = question.getId();
        this.idLabel = String.format(Locale.ROOT, "QB-%04d", question.getId());
        this.imageUrl = buildImageUrl(question.getImagePath());

        int year = question.getYear();
        this.yearAssigned = year >= 1990 && year <= 2030;
        this.yearLabel = yearAssigned ? Integer.toString(year) : "Year not set";

        Subject subject = question.getSubject();
        Topic topic = question.getTopic();
        Type type = question.getType();
        this.subjectAssigned = subject != null;
        this.topicAssigned = topic != null;
        this.subjectLabel = subject == null ? "Subject not assigned" : subject.name();
        this.topicLabel = topic == null ? "Topic not assigned" : topic.name();
        this.typeLabel = type == null ? "Type not set" : type.name();

        List<String> tags = new ArrayList<>();
        if (question.getQuestionTags() != null) {
            for (Tag tag : question.getQuestionTags()) {
                if (tag != null) {tags.add(tag.name());}
            }
        }
        this.tagLabels = List.copyOf(tags);
        this.classified = yearAssigned && subjectAssigned && topicAssigned;
        this.statusLabel = classified ? "Classified" : "Needs details";
        this.statusClass = classified ? "status-pill status-ready" : "status-pill status-review";
    }

    private static String buildImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {return null;}

        // JSON paths may have been written on Windows, so normalize both slash styles.
        String normalized = imagePath.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1) : normalized;
        if (fileName.isBlank() || fileName.equals(".") || fileName.equals("..")) {return null;}

        // Images are served from the configured Question_Images directory.
        return "/question-images/" + UriUtils.encodePathSegment(fileName, StandardCharsets.UTF_8);
    }


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
    public List<String> getTagLabels() { return tagLabels; }
    public boolean isClassified() { return classified; }
    public String getStatusLabel() { return statusLabel; }
    public String getStatusClass() { return statusClass; }
    public String getYearClass() { return yearAssigned ? "metadata-value" : "metadata-value value-missing"; }
}
