package org.example.questionBankApplication.web.listController;

import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FilterTest {

    @Test
    void defaultsToIdAscendingAndNeutralTags() {
        Filter filter = Filter.fromParameters(Map.of());

        assertNull(filter.getYear());
        assertEquals("id", filter.getSortBy());
        assertEquals("asc", filter.getDirection());
        assertEquals(Filter.TagState.NEUTRAL, filter.getTagState(Tag.SOLVED));
        assertTrue(filter.getValidationErrorsForView().isEmpty());
    }

    @Test
    void parsesValidYearSubjectTopicAndTagStates() {
        Filter filter = Filter.fromParameters(Map.of(
                "year", "2022",
                "subject", Subject.Discrete_Mathematics.name(),
                "topic", Topic.Graph_Theory.name(),
                "sortBy", "year",
                "direction", "desc",
                "tag_SOLVED", "include",
                "tag_REPEATED", "exclude"
        ));

        assertEquals(2022, filter.getYear());
        assertEquals(Subject.Discrete_Mathematics, filter.getSubject());
        assertEquals(Topic.Graph_Theory, filter.getTopic());
        assertEquals("year", filter.getSortBy());
        assertEquals("desc", filter.getDirection());
        assertEquals(Filter.TagState.INCLUDE, filter.getTagState(Tag.SOLVED));
        assertEquals(Filter.TagState.EXCLUDE, filter.getTagState(Tag.REPEATED));
        assertTrue(filter.getValidationErrorsForView().isEmpty());
    }

    @Test
    void rejectsInvalidYearAndUsesSafeDefaultsForInvalidSort() {
        Filter filter = Filter.fromParameters(Map.of(
                "year", "not-a-year",
                "sortBy", "imagePath",
                "direction", "sideways"
        ));

        assertNull(filter.getYear());
        assertEquals("id", filter.getSortBy());
        assertEquals("asc", filter.getDirection());
        assertEquals(3, filter.getValidationErrorsForView().size());
    }

    @Test
    void rejectsTopicThatDoesNotBelongToSelectedSubject() {
        Filter filter = Filter.fromParameters(Map.of(
                "subject", Subject.Discrete_Mathematics.name(),
                "topic", Topic.SQL.name()
        ));

        assertNull(filter.getTopic());
        assertTrue(filter.getValidationErrorsForView().stream()
                .anyMatch(message -> message.contains("does not belong")));
    }

    @Test
    void rejectsTopicWhenSubjectIsMissing() {
        Filter filter = Filter.fromParameters(Map.of(
                "topic", Topic.Graph_Theory.name()
        ));

        assertNull(filter.getTopic());
        assertTrue(filter.getValidationErrorsForView().stream()
                .anyMatch(message -> message.contains("Select a valid subject")));
    }

    @Test
    void invalidTagStateIsReportedAndFallsBackToNeutral() {
        Filter filter = Filter.fromParameters(Map.of("tag_SOLVED", "maybe"));

        assertEquals(Filter.TagState.NEUTRAL, filter.getTagState(Tag.SOLVED));
        assertTrue(filter.getValidationErrorsForView().stream()
                .anyMatch(message -> message.contains("Invalid state for tag SOLVED")));
    }
}
