package org.example.questionBankApplication.web.editControler;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.Answer;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.MSQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.QuestionService;
import org.example.questionBankApplication.web.QuestionDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
public class EditController {

    private static final int UNASSIGNED_YEAR = -9999;
    private static final int MAX_TAGS = 3;

    private final QuestionService questionService;

    public EditController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/questions/{id}/edit")
    public String showEditPage(@PathVariable int id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Question> found = questionService.findById(id);
        if (found.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Question " + id + " was not found.");
            return "redirect:/questions";
        }

        Question question = found.get();
        EditForm form = toForm(question);
        addEditPageAttributes(model, question, form, List.of());
        return "edit-question";
    }

    @PostMapping("/questions/{id}/edit")
    public String saveEdit(@PathVariable int id, @ModelAttribute("form") EditForm form, Model model, RedirectAttributes redirectAttributes) {
        Optional<Question> found = questionService.findById(id);
        if (found.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Question " + id + " was not found.");
            return "redirect:/questions";
        }

        Question original = found.get();
        List<String> errors = new ArrayList<>();

        int year = parseYear(form.getYear(), errors);
        Subject subject = parseOptionalEnum(Subject.class, form.getSubject(), "subject", errors);
        Topic topic = parseOptionalEnum(Topic.class, form.getTopic(), "topic", errors);
        Type type = parseOptionalEnum(Type.class, form.getType(), "question type", errors);

        boolean subjectProvided = form.getSubject() != null && !form.getSubject().isBlank();
        boolean topicProvided = form.getTopic() != null && !form.getTopic().isBlank();
        if (subjectProvided && !topicProvided) {
            errors.add("Choose a topic for the selected subject, or leave both subject and topic unassigned.");
        } else if (!subjectProvided && topicProvided) {
            errors.add("Choose the subject that contains the selected topic.");
        } else if (subject != null && topic != null && !SubjectTopicMap.isValidTopic(subject, topic)) {
            errors.add("The selected topic does not belong to the selected subject.");
        }

        Set<Tag> tags = parseTags(form.getSelectedTags(), errors);
        Answer answer = parseAnswer(form, type, errors);

        boolean fullyClassified =
                year != UNASSIGNED_YEAR
                && subject != null
                && topic != null
                && type != null
                && answer != null
                && tags != null;

        if (errors.isEmpty() && !fullyClassified) {
            errors.add(
                    "Complete the year, subject, topic, question type, and answer before saving. "
                    + "Partially classified questions are not allowed."
            );
        }

        if (!errors.isEmpty()) {
            addEditPageAttributes(model, original, form, errors);
            return "edit-question";
        }

        Question updated;
        try {
            // ID and image path deliberately come from the stored record, not from the form.
            updated = new Question(original.getId(), original.getImagePath(), year,
                    topic, type, answer, tags);
        } catch (IllegalArgumentException exception) {
            errors.add(exception.getMessage() == null
                    ? "The submitted question details are not valid."
                    : exception.getMessage());
            addEditPageAttributes(model, original, form, errors);
            return "edit-question";
        }

        try {
            questionService.updateQuestion(updated);
        } catch (IOException | IllegalArgumentException exception) {
            errors.add("The question could not be saved. The original JSON record has been kept. "
                    + safeMessage(exception));
            addEditPageAttributes(model, original, form, errors);
            return "edit-question";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Question " + id + " was updated successfully.");
        return "redirect:/questions";
    }

    private void addEditPageAttributes(Model model, Question question, EditForm form, List<String> errors) {
        QuestionDTO view = new QuestionDTO(question);
        model.addAttribute("questionId", question.getId());
        model.addAttribute("questionIdLabel", view.getIdLabel());
        model.addAttribute("questionImageUrl", view.getImageUrl());
        model.addAttribute("form", form);
        model.addAttribute("editErrors", errors);
        model.addAttribute("subjectOptions", Subject.values());
        model.addAttribute("topicOptions", Topic.values());
        model.addAttribute("typeOptions", Type.values());
        model.addAttribute("answerOptions", AnswerOption.values());
        model.addAttribute("tagOptions", Tag.values());

        Map<String, List<String>> subjectTopicOptions = new LinkedHashMap<>();
        for (Subject subject : Subject.values()) {
            Set<Topic> validTopics = SubjectTopicMap.getTopics(subject);
            List<String> topicNames = Arrays.stream(Topic.values())
                    .filter(validTopics::contains)
                    .map(Topic::name)
                    .toList();
            subjectTopicOptions.put(subject.name(), topicNames);
        }
        model.addAttribute("subjectTopicOptions", subjectTopicOptions);
    }

    private EditForm toForm(Question question) {
        EditForm form = new EditForm();

        int year = question.getYear();
        form.setYear(year >= 1995 && year <= 2030 ? Integer.toString(year) : "");
        form.setSubject(question.getSubject() == null ? "" : question.getSubject().name());
        form.setTopic(question.getTopic() == null ? "" : question.getTopic().name());
        form.setType(question.getType() == null ? "" : question.getType().name());

        Answer answer = question.getAnswer();
        if (answer instanceof MCQ_Answer mcq) {
            form.setMcqCorrectOption(mcq.getCorrectOption().name());
        } else if (answer instanceof MSQ_Answer msq) {
            form.setMsqCorrectOptions(msq.getCorrectOptions().stream()
                    .map(AnswerOption::name)
                    .sorted()
                    .toList());
        } else if (answer instanceof NAT_Answer nat) {
            form.setNatMinValue(Double.toString(nat.getMinValue()));
            form.setNatMaxValue(Double.toString(nat.getMaxValue()));
        }

        if (question.getQuestionTags() != null) {
            form.setSelectedTags(question.getQuestionTags().stream()
                    .filter(tag -> tag != null)
                    .map(Tag::name)
                    .sorted()
                    .toList());
        }
        return form;
    }

    private int parseYear(String rawYear, List<String> errors) {
        if (rawYear == null || rawYear.isBlank()) {
            return UNASSIGNED_YEAR;
        }
        try {
            int year = Integer.parseInt(rawYear.trim());
            if (year < 1995 || year > 2030) {
                errors.add("GATE year must be between 1995 and 2030, or left blank if it is unknown.");
                return UNASSIGNED_YEAR;
            }
            return year;
        } catch (NumberFormatException exception) {
            errors.add("Enter a valid whole-number GATE year, or leave it blank if it is unknown.");
            return UNASSIGNED_YEAR;
        }
    }

    private <E extends Enum<E>> E parseOptionalEnum(Class<E> enumType, String rawValue, String fieldLabel, List<String> errors) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumType, rawValue.trim());
        } catch (IllegalArgumentException exception) {
            errors.add("Select a valid " + fieldLabel + ".");
            return null;
        }
    }

    private Set<Tag> parseTags(List<String> submittedTags, List<String> errors) {
        Set<Tag> tags = EnumSet.noneOf(Tag.class);
        if (submittedTags != null) {
            for (String rawTag : submittedTags) {
                Tag tag = parseOptionalEnum(Tag.class, rawTag, "tag", errors);
                if (tag != null) {
                    tags.add(tag);
                }
            }
        }
        if (tags.size() > MAX_TAGS) {
            errors.add("Choose no more than three tags.");
        }
        return tags;
    }

    private Answer parseAnswer(EditForm form, Type type, List<String> errors) {
        if (type == null) {
            return null;
        }

        switch (type) {
            case MCQ -> {
                String rawOption = form.getMcqCorrectOption();
                if (rawOption == null || rawOption.isBlank()) {
                    return null;
                }
                AnswerOption option = parseOptionalEnum(AnswerOption.class, rawOption,
                        "MCQ correct option", errors);
                return option == null ? null : new MCQ_Answer(option);
            }
            case MSQ -> {
                Set<AnswerOption> options = new LinkedHashSet<>();
                if (form.getMsqCorrectOptions() != null) {
                    for (String rawOption : form.getMsqCorrectOptions()) {
                        AnswerOption option = parseOptionalEnum(AnswerOption.class, rawOption,
                                "MSQ correct option", errors);
                        if (option != null) {
                            options.add(option);
                        }
                    }
                }
                return options.isEmpty() ? null : new MSQ_Answer(options);
            }
            case NAT -> {
                String rawMin = form.getNatMinValue();
                String rawMax = form.getNatMaxValue();
                boolean minBlank = rawMin == null || rawMin.isBlank();
                boolean maxBlank = rawMax == null || rawMax.isBlank();
                if (minBlank && maxBlank) {
                    return null;
                }
                if (minBlank || maxBlank) {
                    errors.add("For an NAT answer, enter both the minimum and maximum values, or leave both blank.");
                    return null;
                }
                try {
                    double min = Double.parseDouble(rawMin);
                    double max = Double.parseDouble(rawMax);
                    if (!Double.isFinite(min) || !Double.isFinite(max)) {
                        errors.add("NAT answer values must be finite numbers.");
                        return null;
                    }
                    if (min > max) {
                        errors.add("NAT minimum value cannot be greater than the maximum value.");
                        return null;
                    }
                    return new NAT_Answer(min, max);
                } catch (NumberFormatException exception) {
                    errors.add("Enter valid numeric values for the NAT minimum and maximum.");
                    return null;
                }
            }
        }
        return null;
    }

    private String safeMessage(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "Please check the data directory and try again." : message;
    }
}
