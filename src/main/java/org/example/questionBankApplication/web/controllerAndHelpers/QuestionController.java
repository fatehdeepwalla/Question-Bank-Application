
package org.example.questionBankApplication.web.controllerAndHelpers;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;

import org.example.questionBankApplication.web.QuestionService;
import org.example.questionBankApplication.web.ViewDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class QuestionController {

    private final QuestionService questionService;
    private final QuestionQueryService questionQueryService;
    private final QuestionStatisticsService statisticsService;

    public QuestionController(QuestionService questionService, QuestionQueryService questionQueryService, QuestionStatisticsService statisticsService) {
        this.questionService = questionService;
        this.questionQueryService = questionQueryService;
        this.statisticsService = statisticsService;
    }

    @GetMapping({"/", "/questions"})
    public String showQuestions(@RequestParam Map<String, String> parameters, Model model) {

        QuestionFilter filter = QuestionFilter.fromParameters(parameters);

        // Retrieve the existing JSON-backed collection.
        List<Question> allQuestions = questionService.findAll();

        // Apply the submitted filters and sorting criteria.
        List<Question> filteredQuestions = questionQueryService.filterAndSort(allQuestions, filter);

        // Statistics always describe the filtered collection.
        List<ViewDTO> filteredItems = filteredQuestions.stream().map(ViewDTO::new).toList();
        model.addAllAttributes(statisticsService.calculate(filteredItems));


        model.addAttribute("items", filteredItems);
        model.addAttribute("totalCount", allQuestions.size());

        // Preserve selected criteria in the form.
        model.addAttribute("selectedYear", filter.getSelectedYear());
        model.addAttribute("selectedSubject", filter.getSelectedSubject());
        model.addAttribute("selectedTopic", filter.getSelectedTopic());
        model.addAttribute("sortBy", filter.getSortBy());
        model.addAttribute("direction", filter.getDirection());
        model.addAttribute("tagStates", filter.getTagStatesForView());
        model.addAttribute("filterErrors", filter.getValidationErrors());

        // Subject and tag dropdown options.
        model.addAttribute("tagOptions", Tag.values());
        model.addAttribute("subjectOptions", SubjectTopicMap.getAllSubjects());
        model.addAttribute("topicOptions", SubjectTopicMap.getAllTopics());

        Map<String, List<String>> subjectTopicOptions = new LinkedHashMap<>();
        for (Subject subject : SubjectTopicMap.getAllSubjects()) {
            List<String> topics = SubjectTopicMap.getTopics(subject).stream().map(Topic::name).toList();
            subjectTopicOptions.put(subject.name(), topics);
        }
        model.addAttribute("subjectTopicOptions", subjectTopicOptions);
        return "list";
    }
}
