package org.example.questionBankApplication.web.controllerAndHelpers;

import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QuestionFilterTest {

    @Test
    void defaultsToIdAscendingAndNeutralTags() {
        QuestionFilter filter = QuestionFilter.fromParameters(Map.of());

        assertNull(filter.getYear());
        assertEquals("id", filter.getSortBy());
        assertEquals("asc", filter.getDirection());
        assertEquals(QuestionFilter.TagState.NEUTRAL, filter.getTagState(Tag.SOLVED));
        assertTrue(filter.getValidationErrors().isEmpty());
    }

    @Test
    void parsesValidYearSubjectTopicAndTagStates() {
        QuestionFilter filter = QuestionFilter.fromParameters(Map.of(
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
        assertEquals(QuestionFilter.TagState.INCLUDE, filter.getTagState(Tag.SOLVED));
        assertEquals(QuestionFilter.TagState.EXCLUDE, filter.getTagState(Tag.REPEATED));
        assertTrue(filter.getValidationErrors().isEmpty());
    }

    @Test
    void rejectsInvalidYearAndUsesSafeDefaultsForInvalidSort() {
        QuestionFilter filter = QuestionFilter.fromParameters(Map.of(
                "year", "not-a-year",
                "sortBy", "imagePath",
                "direction", "sideways"
        ));

        assertNull(filter.getYear());
        assertEquals("id", filter.getSortBy());
        assertEquals("asc", filter.getDirection());
        assertEquals(3, filter.getValidationErrors().size());
    }

    @Test
    void rejectsTopicThatDoesNotBelongToSelectedSubject() {
        QuestionFilter filter = QuestionFilter.fromParameters(Map.of(
                "subject", Subject.Discrete_Mathematics.name(),
                "topic", Topic.SQL.name()
        ));

        assertNull(filter.getTopic());
        assertTrue(filter.getValidationErrors().stream()
                .anyMatch(message -> message.contains("does not belong")));
    }

    @Test
    void rejectsTopicWhenSubjectIsMissing() {
        QuestionFilter filter = QuestionFilter.fromParameters(Map.of(
                "topic", Topic.Graph_Theory.name()
        ));

        assertNull(filter.getTopic());
        assertTrue(filter.getValidationErrors().stream()
                .anyMatch(message -> message.contains("Select a valid subject")));
    }

    @Test
    void invalidTagStateIsReportedAndFallsBackToNeutral() {
        QuestionFilter filter = QuestionFilter.fromParameters(Map.of("tag_SOLVED", "maybe"));

        assertEquals(QuestionFilter.TagState.NEUTRAL, filter.getTagState(Tag.SOLVED));
        assertTrue(filter.getValidationErrors().stream()
                .anyMatch(message -> message.contains("Invalid state for tag SOLVED")));
    }
}
