package org.example.questionBankApplication.model;

import org.example.questionBankApplication.model.answer.*;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;
import java.util.Set;

public class Question {

    ///////////////////////////////////////////// Instance Variables ///////////////////////////////////////////////////
    private int id;
    private String imagePath;

    private int year;
    private Subject subject;
    private Topic topic;
    private Type type;
    private Answer answer;

    private Set<Tag> questionTags;

    ///////////////////////////////////////////// Constructor //////////////////////////////////////////////////////////
    public Question(int id, String imagePath, int year, Topic topic, Type type, Answer answer, Set<Tag> questionTags) {

        this.id = id;
        if(imagePath==null) {throw new RuntimeException("Null Argument");}
        this.imagePath = imagePath;

        this.year = year;
        this.subject = topic == null?  null: SubjectTopicMap.getSubject(topic);
        this.topic = topic;
        validateTypeAndAnswer(type, answer);
        this.type = type;
        this.answer = answer;

        this.questionTags = questionTags;
    }
    private static void validateTypeAndAnswer(Type type, Answer answer) {
        if (answer == null) {return;} // A question may not have an answer yet
        if (type == null) {throw new IllegalArgumentException("Question type is required when an answer is provided");}
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
    public void setYear(int year) { this.year = year;}
    ///////////////////////////////////////////// {Subject, Topic} /////////////////////////////////////////////////////
    public Topic getTopic() { return topic;}
    public Subject getSubject() {return subject;}
    public void setTopic(Topic topic) {
        if (topic == null) {throw new IllegalArgumentException("topic cannot be null");}
        this.topic = topic;
        this.subject = SubjectTopicMap.getSubject(topic);
    }

    ///////////////////////////////////////////// {Type} ///////////////////////////////////////////////////////////////
    public Type getType() {return type;}
    public void setType(Type type) {
        if (type == null) {throw new IllegalArgumentException("type must not be null");}
        if (this.type != type) {this.answer = null;}
        this.type = type;
    }
    ///////////////////////////////////////////// {Answer} /////////////////////////////////////////////////////////////
    public Answer getAnswer() {return answer;}
    public void setMcqAnswer(AnswerOption correctOption) {
        if (correctOption == null) {throw new IllegalArgumentException("correctOption must not be null");}
        requireType(Type.MCQ);
        this.answer = new MCQ_Answer(correctOption);
    }
    public void setMsqAnswer(Set<AnswerOption> correctOptions) {
        if (correctOptions == null) {throw new IllegalArgumentException("correctOptions must not be null");}
        requireType(Type.MSQ);
        this.answer = new MSQ_Answer(correctOptions);
    }
    public void setNatAnswer(double minValue, double maxValue) {
        requireType(Type.NAT);
        this.answer = new NAT_Answer(minValue, maxValue);
    }
    private void requireType(Type expectedType) {
        if (type == null) {
            throw new IllegalStateException( "Set the question type before entering its answer");
        }
        if (type != expectedType) {
            throw new IllegalStateException("Expected question type " + expectedType + ", but the current type is " + type);
        }
    }

    ///////////////////////////////////////////// {Tags} ///////////////////////////////////////////////////////////////
    public Set<Tag> getQuestionTags() {return questionTags;}
    public void setQuestionTags(Set<Tag> questionTags) {
        if (questionTags == null) {throw new IllegalArgumentException("questionTags cannot be null");}
        this.questionTags = questionTags;
    }
}
