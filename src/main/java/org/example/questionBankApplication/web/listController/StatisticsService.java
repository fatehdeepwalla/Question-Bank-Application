
package org.example.questionBankApplication.web.listController;

import org.example.questionBankApplication.web.QuestionDTO;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    public Map<String, Object> calculate(List<QuestionDTO> items) {

        Map<String, Object> stats = new LinkedHashMap<>();

        long totalFilteredCount = items.size();
        long classified = items.stream().filter(QuestionDTO::isClassified).count();
        long tagged = items.stream().filter(item -> !item.getTagLabels().isEmpty()).count();
        long yearAssigned = items.stream().filter(QuestionDTO::isYearAssigned).count();
        long subjectAssigned = items.stream().filter(QuestionDTO::isSubjectAssigned).count();
        long topicAssigned = items.stream().filter(QuestionDTO::isTopicAssigned).count();
        long typeAssigned = items.stream().filter(QuestionDTO::isTypeAssigned).count();
        long answerAssigned = items.stream().filter(QuestionDTO::isAnswerAssigned).count();

        stats.put("totalFilteredCount", totalFilteredCount);
        stats.put("classifiedQuestions", classified);
        stats.put("needsMetadataCount", totalFilteredCount - classified);
        stats.put("taggedQuestions", tagged);

        stats.put("yearAssignedCount", yearAssigned);
        stats.put("subjectAssignedCount", subjectAssigned);
        stats.put("topicAssignedCount", topicAssigned);
        stats.put("typeAssignedCount", typeAssigned);
        stats.put("answerAssignedCount", answerAssigned);

        stats.put("missingYearCount", totalFilteredCount - yearAssigned);
        stats.put("missingSubjectCount", totalFilteredCount - subjectAssigned);
        stats.put("missingTopicCount", totalFilteredCount - topicAssigned);
        stats.put("missingTypeCount", totalFilteredCount - typeAssigned);
        stats.put("missingAnswerCount", totalFilteredCount - answerAssigned);

        stats.put("solvedTagCount", countTag(items, "SOLVED"));
        stats.put("repeatedTagCount", countTag(items, "REPEATED"));
        stats.put("importantTagCount", countTag(items, "IMPORTANT"));
        stats.put("veryImportantTagCount", countTag(items, "VERY_IMPORTANT"));

        return stats;
    }

    private long countTag(List<QuestionDTO> items, String tagName) {
        return items.stream().filter(item -> item.getTagLabels().contains(tagName)).count();
    }
}
