package org.example.questionBankApplication.web.editControler;

import java.util.ArrayList;
import java.util.List;

/** Form backing bean for editing the editable metadata of an existing question. */
public class EditForm {

    private String year = "";
    private String subject = "";
    private String topic = "";
    private String type = "";

    private String mcqCorrectOption = "";
    private List<String> msqCorrectOptions = new ArrayList<>();
    private String natMinValue = "";
    private String natMaxValue = "";

    private List<String> selectedTags = new ArrayList<>();

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year == null ? "" : year.trim(); }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject == null ? "" : subject.trim(); }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic == null ? "" : topic.trim(); }

    public String getType() { return type; }
    public void setType(String type) { this.type = type == null ? "" : type.trim(); }

    public String getMcqCorrectOption() { return mcqCorrectOption; }
    public void setMcqCorrectOption(String mcqCorrectOption) {
        this.mcqCorrectOption = mcqCorrectOption == null ? "" : mcqCorrectOption.trim();
    }

    public List<String> getMsqCorrectOptions() { return msqCorrectOptions; }
    public void setMsqCorrectOptions(List<String> msqCorrectOptions) {
        this.msqCorrectOptions = msqCorrectOptions == null
                ? new ArrayList<>()
                : new ArrayList<>(msqCorrectOptions);
    }

    public String getNatMinValue() { return natMinValue; }
    public void setNatMinValue(String natMinValue) {
        this.natMinValue = natMinValue == null ? "" : natMinValue.trim();
    }

    public String getNatMaxValue() { return natMaxValue; }
    public void setNatMaxValue(String natMaxValue) {
        this.natMaxValue = natMaxValue == null ? "" : natMaxValue.trim();
    }

    public List<String> getSelectedTags() { return selectedTags; }
    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags = selectedTags == null
                ? new ArrayList<>()
                : new ArrayList<>(selectedTags);
    }
}
