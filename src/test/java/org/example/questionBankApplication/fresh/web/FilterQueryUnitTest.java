package org.example.questionBankApplication.fresh.web;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.listController.Filter;
import org.example.questionBankApplication.web.listController.Query;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilterQueryUnitTest {

    private static Question question(int id, int year, Topic topic, Tag... tags) {
        if (year == -9999) {
            return new Question(id, "q" + id + ".png");
        }
        return new Question(id, "q" + id + ".png", year, topic, Type.MCQ,
                new MCQ_Answer(AnswerOption.A), Set.of(tags));
    }

    private static List<Integer> ids(List<Question> questions) {
        return questions.stream().map(Question::getId).toList();
    }

    @Test
    void blankOrNullParametersChooseSafeDefaults() {
        Filter blank = Filter.fromParameters(Map.of());
        Filter nullMap = Filter.fromParameters(null);

        assertNull(blank.getYear());
        assertNull(blank.getSubject());
        assertNull(blank.getTopic());
        assertEquals("id", blank.getSortBy());
        assertEquals("asc", blank.getDirection());
        assertTrue(blank.getValidationErrorsForView().isEmpty());
        assertEquals(Filter.TagState.NEUTRAL, blank.getTagState(Tag.SOLVED));

        assertEquals("id", nullMap.getSortBy());
        assertEquals("asc", nullMap.getDirection());
    }

    @Test
    void acceptsSupportedYearEndpointsAndRejectsInvalidYearText() {
        assertEquals(1995, Filter.fromParameters(Map.of("year", "1995")).getYear());
        assertEquals(2030, Filter.fromParameters(Map.of("year", "2030")).getYear());

        Filter tooEarly = Filter.fromParameters(Map.of("year", "1994"));
        assertNull(tooEarly.getYear());
        assertTrue(tooEarly.getValidationErrorsForView().stream().anyMatch(e -> e.contains("1995 and 2030")));

        Filter malformed = Filter.fromParameters(Map.of("year", "20xx"));
        assertNull(malformed.getYear());
        assertTrue(malformed.getValidationErrorsForView().stream().anyMatch(e -> e.contains("whole-number")));
    }

    @Test
    void validatesSubjectTopicRelationshipInsteadOfAcceptingAnImpossiblePair() {
        Filter valid = Filter.fromParameters(Map.of("subject", "Algorithms", "topic", "Sorting"));
        assertEquals(Subject.Algorithms, valid.getSubject());
        assertEquals(Topic.Sorting, valid.getTopic());
        assertTrue(valid.getValidationErrorsForView().isEmpty());

        Filter invalid = Filter.fromParameters(Map.of("subject", "Algorithms", "topic", "SQL"));
        assertEquals(Subject.Algorithms, invalid.getSubject());
        assertNull(invalid.getTopic());
        assertTrue(invalid.getValidationErrorsForView().stream().anyMatch(e -> e.contains("does not belong")));
    }

    @Test
    void invalidEnumSortDirectionAndTagInputsFallBackToSafeDefaults() {
        Filter filter = Filter.fromParameters(Map.of(
                "subject", "Not_A_Subject",
                "sortBy", "imagePath",
                "direction", "sideways",
                "tag_SOLVED", "maybe"
        ));

        assertNull(filter.getSubject());
        assertEquals("id", filter.getSortBy());
        assertEquals("asc", filter.getDirection());
        assertEquals(Filter.TagState.NEUTRAL, filter.getTagState(Tag.SOLVED));
        assertTrue(filter.getValidationErrorsForView().size() >= 4);
    }

    @Test
    void appliesYearSubjectTopicAndIncludeExcludeTagCriteriaTogether() {
        Question expected = question(1, 2024, Topic.Sorting, Tag.IMPORTANT);
        Question excludedByTag = question(2, 2024, Topic.Sorting, Tag.IMPORTANT, Tag.SOLVED);
        Question wrongSubject = question(3, 2024, Topic.SQL, Tag.IMPORTANT);
        Question wrongYear = question(4, 2023, Topic.Sorting, Tag.IMPORTANT);

        Filter filter = Filter.fromParameters(Map.of(
                "year", "2024",
                "subject", "Algorithms",
                "topic", "Sorting",
                "tag_IMPORTANT", "include",
                "tag_SOLVED", "exclude"
        ));

        List<Question> result = new Query().filterAndSort(
                List.of(excludedByTag, wrongSubject, wrongYear, expected), filter);

        assertEquals(List.of(1), ids(result));
    }

    @Test
    void includeRequiresTagAndExcludeRejectsTagButNeutralDoesNotRestrict() {
        Question tagged = question(1, 2020, Topic.Sorting, Tag.SOLVED);
        Question untagged = question(2, -9999, Topic.Sorting);

        Query query = new Query();
        Filter includeSolved = Filter.fromParameters(Map.of("tag_SOLVED", "include"));
        Filter excludeSolved = Filter.fromParameters(Map.of("tag_SOLVED", "exclude"));
        Filter neutral = Filter.fromParameters(Map.of("tag_SOLVED", "neutral"));

        assertEquals(List.of(1), ids(query.filterAndSort(List.of(tagged, untagged), includeSolved)));
        assertEquals(List.of(2), ids(query.filterAndSort(List.of(tagged, untagged), excludeSolved)));
        assertEquals(List.of(1, 2), ids(query.filterAndSort(List.of(tagged, untagged), neutral)));
    }

    @Test
    void sortsByIdInBothDirections() {
        List<Question> questions = List.of(
                question(4, 2020, Topic.Sorting),
                question(1, 2020, Topic.Sorting),
                question(3, 2020, Topic.Sorting),
                question(2, 2020, Topic.Sorting));

        assertEquals(List.of(1, 2, 3, 4), ids(new Query().filterAndSort(questions,
                Filter.fromParameters(Map.of("sortBy", "id", "direction", "asc")))));
        assertEquals(List.of(4, 3, 2, 1), ids(new Query().filterAndSort(questions,
                Filter.fromParameters(Map.of("sortBy", "id", "direction", "desc")))));
    }

    @Test
    void sortsByYearWithIdTieBreakAndKeepsUnassignedYearsLastInBothDirections() {
        List<Question> questions = List.of(
                question(2, 2000, Topic.Sorting),
                question(4, -9999, Topic.Sorting),
                question(1, 2000, Topic.Sorting),
                question(3, 1995, Topic.Propositional_Logic),
                question(5, 2030, Topic.Sorting));

        Query query = new Query();
        assertEquals(List.of(3, 1, 2, 5, 4), ids(query.filterAndSort(questions,
                Filter.fromParameters(Map.of("sortBy", "year", "direction", "asc")))));
        assertEquals(List.of(5, 1, 2, 3, 4), ids(query.filterAndSort(questions,
                Filter.fromParameters(Map.of("sortBy", "year", "direction", "desc")))));
    }

    @Test
    void yearCriterionMatchesExactlyRatherThanTreatingItAsARange() {
        List<Question> questions = List.of(
                question(1, 2020, Topic.Sorting),
                question(2, 2021, Topic.Sorting),
                question(3, -9999, Topic.Sorting));

        assertEquals(List.of(2), ids(new Query().filterAndSort(questions,
                Filter.fromParameters(Map.of("year", "2021")))));
    }
}
