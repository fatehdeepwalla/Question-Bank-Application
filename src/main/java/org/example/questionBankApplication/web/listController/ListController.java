package org.example.questionBankApplication.web.listController;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.QuestionService;
import org.example.questionBankApplication.web.QuestionDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {

    private static final int PAGE_SIZE = 5;

    private final QuestionService questionService;
    private final Query query;
    private final StatisticsService statisticsService;

    public ListController(
            QuestionService questionService,
            Query query,
            StatisticsService statisticsService) {
        this.questionService = questionService;
        this.query = query;
        this.statisticsService = statisticsService;
    }

    @GetMapping({"/", "/questions"})
    public String showQuestions(@RequestParam Map<String, String> parameters, Model model) {

        Filter filter = Filter.fromParameters(parameters);

        // Retrieve the existing JSON-backed collection.
        List<Question> allQuestions = questionService.findAll();

        // Apply the submitted filters and sorting before dividing results into pages.
        List<Question> filteredQuestions = query.filterAndSort(allQuestions, filter);
        List<QuestionDTO> filteredItems = filteredQuestions.stream().map(QuestionDTO::new).toList();

        // Statistics always describe the full filtered collection, not just the current page.
        model.addAllAttributes(statisticsService.calculate(filteredItems));

        int totalFilteredCount = filteredItems.size();
        int totalPages = totalFilteredCount / PAGE_SIZE + (totalFilteredCount % PAGE_SIZE == 0 ? 0 : 1);
        int requestedPage = parseRequestedPage(parameters.get("page"));

        // A return from the edit screen carries _return=1. Keep the requested page
        // even if the edited question changes the result count; silently clamping
        // it would return the user to a different page.
        boolean returningFromEdit = "1".equals(parameters.get("_return"));
        int currentPage = returningFromEdit
                ? requestedPage
                : (totalPages == 0 ? 1 : Math.min(requestedPage, totalPages));
        int fromIndex = Math.min((currentPage - 1) * PAGE_SIZE, totalFilteredCount);
        int toIndex = Math.min(fromIndex + PAGE_SIZE, totalFilteredCount);
        List<QuestionDTO> pageItems = filteredItems.subList(fromIndex, toIndex);

        model.addAttribute("items", pageItems);
        model.addAttribute("totalCount", allQuestions.size());

        // Pagination
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageStart", totalFilteredCount == 0 ? 0 : fromIndex + 1);
        model.addAttribute("pageEnd", toIndex);
        model.addAttribute("hasPreviousPage", currentPage > 1 && totalPages > 0);
        model.addAttribute("hasNextPage", currentPage < totalPages);
        model.addAttribute("previousPageUrl", buildPageUrl(parameters, currentPage - 1));
        model.addAttribute("nextPageUrl", buildPageUrl(parameters, currentPage + 1));
        model.addAttribute("returnUrl", buildReturnUrl(parameters, currentPage));

        // Preserve selected criteria in the filter form.
        model.addAttribute("selectedYear", filter.getYear());
        model.addAttribute("selectedSubject", filter.getSelectedSubject());
        model.addAttribute("selectedTopic", filter.getSelectedTopic());
        model.addAttribute("tagStates", filter.getTagStatesForView());
        model.addAttribute("sortBy", filter.getSortBy());
        model.addAttribute("direction", filter.getDirection());
        model.addAttribute("filterErrors", filter.getValidationErrorsForView());

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

    private int parseRequestedPage(String rawPage) {
        try {
            int page = Integer.parseInt(rawPage);
            return Math.max(page, 1);
        } catch (NumberFormatException exception) {
            return 1;
        }
    }

    /**
     * Builds a pagination URL while retaining the currently submitted filters
     * and sorting options. The existing page value is replaced, not duplicated.
     */
    private String buildPageUrl(Map<String, String> parameters, int page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/questions");

        parameters.forEach((name, value) -> {
            if (!"page".equals(name) && !"_return".equals(name) && value != null) {
                builder.queryParam(name, value);
            }
        });

        return builder.queryParam("page", Math.max(1, page))
                .build()
                .encode()
                .toUriString();
    }

    /**
     * Builds a return link distinct from ordinary pagination links. The marker
     * lets the list retain the requested page if saving changes the match count.
     */
    private String buildReturnUrl(Map<String, String> parameters, int page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/questions");
        parameters.forEach((name, value) -> {
            if (!"page".equals(name) && !"_return".equals(name) && value != null) {
                builder.queryParam(name, value);
            }
        });
        return builder.queryParam("page", Math.max(1, page))
                .queryParam("_return", "1")
                .build()
                .encode()
                .toUriString();
    }
}
