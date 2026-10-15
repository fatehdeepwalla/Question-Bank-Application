
package org.example.questionBankApplication.web.controllerAndHelpers;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class QuestionQueryService {

    public List<Question> filterAndSort(List<Question> questions, QuestionFilter filter) {
        return questions.stream()
                .filter(question -> matches(question, filter))
                .sorted(comparatorFor(filter)).toList();
    }

    private boolean matches(Question question, QuestionFilter filter) {

        // Year
        if (filter.getYear() != null && question.getYear() != filter.getYear().intValue()) {
            return false;
        }

        // Subject
        if (filter.getSubject() != null && question.getSubject() != filter.getSubject()) {
            return false;
        }

        // Topic
        if (filter.getTopic() != null && question.getTopic() != filter.getTopic()) {
            return false;
        }

        // Tag rules
        Set<Tag> questionTags = question.getQuestionTags();

        for (Tag tag : Tag.values()) {

            boolean hasTag =
                    questionTags != null && questionTags.contains(tag);

            switch (filter.getTagState(tag)) {
                case INCLUDE -> {
                    if (!hasTag) {
                        return false;
                    }
                }
                case EXCLUDE -> {
                    if (hasTag) {
                        return false;
                    }
                }
                case NEUTRAL -> {
                    // This tag does not restrict the result.
                }
            }
        }

        return true;
    }

    private Comparator<Question> comparatorFor(QuestionFilter filter) {
        if ("year".equals(filter.getSortBy())) {
            return (left, right) -> compareByYear(left, right, filter.getDirection());
        }

        Comparator<Question> byId = Comparator.comparingInt(Question::getId);
        return "desc".equals(filter.getDirection()) ? byId.reversed() : byId;
    }

    private int compareByYear(Question left, Question right, String direction) {

        boolean leftAssigned = isYearAssigned(left.getYear());
        boolean rightAssigned = isYearAssigned(right.getYear());

        // Keep questions with unassigned years at the end
        // in both ascending and descending order.
        if (leftAssigned != rightAssigned) {
            return leftAssigned ? -1 : 1;
        }

        // If both years are unassigned, order by ID ascending.
        if (!leftAssigned) {return Integer.compare(left.getId(), right.getId());}

        int comparison = "desc".equals(direction)
                ? Integer.compare(right.getYear(), left.getYear())
                : Integer.compare(left.getYear(), right.getYear());

        // Equal years are ordered by ID ascending.
        return comparison != 0 ? comparison : Integer.compare(left.getId(), right.getId());
    }

    private boolean isYearAssigned(int year) {
        return year >= 1990 && year <= 2030;
    }
}
