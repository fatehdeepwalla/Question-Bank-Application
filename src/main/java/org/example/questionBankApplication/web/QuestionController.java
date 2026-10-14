package org.example.questionBankApplication.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping({"/", "/questions"})
    public String showQuestions(Model model) {
        List<ViewDTO> items = questionService.findAll()
                                            .stream()
                                            .map(ViewDTO::new)
                                            .toList();

        long classifiedCount = items.stream().filter(ViewDTO::isClassified).count();
        long needsMetadataCount = items.size() - classifiedCount;
        long taggedCount = items.stream().filter(item -> !item.getTagLabels().isEmpty()).count();

        model.addAttribute("items", items);
        model.addAttribute("totalQuestions", items.size());
        model.addAttribute("classifiedQuestions", classifiedCount);
        model.addAttribute("needsMetadataCount", needsMetadataCount);
        model.addAttribute("taggedQuestions", taggedCount);
        return "list";
    }
}
