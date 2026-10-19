package org.example.questionBankApplication.model;

import org.example.questionBankApplication.model.answer.*;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;
import java.util.Set;

public class Question {

    ///////////////////////////////////////////// Instance Variables ///////////////////////////////////////////////////
    private final int id;
    private final String imagePath;

    private int year;
    private Subject subject;
    private Topic topic;
    private Type type;
    private Answer answer;

    private Set<Tag> questionTags;

    ///////////////////////////////////////////// Constructor //////////////////////////////////////////////////////////
    // Initial state of Question
    public Question(int id, String imagePath) {
        this.id = id;
        if(imagePath==null) {throw new RuntimeException("Null Argument");}
        this.imagePath = imagePath;

        this.year = -9999;
        this.subject = null;
        this.topic = null;
        this.type = null;
        this.answer = null;
        this.questionTags = null;
    }

    // Classified Question
    public Question(int id, String imagePath, int year, Topic topic, Type type, Answer answer, Set<Tag> questionTags) {
        this.id = id;

        if(imagePath==null) {throw new RuntimeException("Image Path is Null");}
        this.imagePath = imagePath;

        if (year<1995 || year >2030) {throw new RuntimeException("Invalid Year");}
        this.year = year;

        if(topic==null) {throw new RuntimeException("Topic is Null");}
        this.subject = SubjectTopicMap.getSubject(topic);
        this.topic = topic;

        if(type==null || answer==null) {throw new RuntimeException("Type or answer is Null");}
        validateTypeAndAnswer(type, answer);
        this.type = type;
        this.answer = answer;

        if(questionTags==null) {throw new RuntimeException("Question Tags is Null");}
        this.questionTags = questionTags;
    }
    private static void validateTypeAndAnswer(Type type, Answer answer) {
        boolean compatible = switch (type) {
            case MCQ -> answer instanceof MCQ_Answer;
            case MSQ -> answer instanceof MSQ_Answer;
            case NAT -> answer instanceof NAT_Answer;
        };
        if (!compatible) {throw new IllegalArgumentException("Answer type does not match question type: " + type);}
    }

    ///////////////////////////////////////////// {id, imagePath} //////////////////////////////////////////////////////
    // No Setters, as they should be unique.
    public int getId() { return id;}
    public String getImagePath() {return imagePath;}

    ///////////////////////////////////////////// {year} ///////////////////////////////////////////////////////////////
    public int getYear() { return year;}
    public void setYear(int year) {
        if (year<1995 || year >2030) {throw new RuntimeException("Invalid Year");}
        this.year = year;
    }

    ///////////////////////////////////////////// {Subject, Topic} /////////////////////////////////////////////////////
    public Topic getTopic() { return topic;}
    public Subject getSubject() {return subject;}
    public void setTopicAndSubject(Topic topic) {
        if (topic == null) {throw new IllegalArgumentException("topic cannot be null");}
        this.topic = topic;
        this.subject = SubjectTopicMap.getSubject(topic);
    }

    ///////////////////////////////////////////// {Type, Answer} ///////////////////////////////////////////////////////
    public Type getType() {return type;}
    public Answer getAnswer() {return answer;}
    public void setTypeAndAnswer(Type type, Answer answer) {
        if (type == null || answer==null) {throw new IllegalArgumentException("type and answer must not be null");}
        validateTypeAndAnswer(type, answer);
        this.type = type;
        this.answer = answer;
    }

    ///////////////////////////////////////////// {Tags} ///////////////////////////////////////////////////////////////
    public Set<Tag> getQuestionTags() {return questionTags;}
    public void setQuestionTags(Set<Tag> questionTags) {
        if (questionTags == null) {throw new IllegalArgumentException("questionTags cannot be null");}
        this.questionTags = questionTags;
    }
}
