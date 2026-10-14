package org.example.questionBankApplication.model;

import java.util.HashSet;
import java.util.Set;

public class Question {
    private String id;
    private int year;
    private String Topic;
    private String subTopic;
    private Type type;
    private Answer answer;
    private String image;
    private Set<Tag> questionTags = new HashSet<>();

}
