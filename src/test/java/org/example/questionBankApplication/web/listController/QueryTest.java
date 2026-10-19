package org.example.questionBankApplication.web.listController;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    private final Query service = new Query();

    private Question question(int id, int year, Topic topic, Set<Tag> tags) {
        return new Question(id, "Question_Images/q" + id + ".png", year, topic,
                Type.MCQ, null, tags);
    }

    @Test
    void filtersByYearSubjectAndTopic() {
        List<Question> questions = List.of(
                question(1, 2020, Topic.Graph_Theory, Set.of()),
                question(2, 2021, Topic.SQL, Set.of()),
                question(3, 2021, Topic.Graph_Theory, Set.of())
        );
        Filter filter = Filter.fromParameters(Map.of(
                "year", "2021",
                "subject", "Discrete_Mathematics",
                "topic", "Graph_Theory"
        ));

        List<Question> result = service.filterAndSort(questions, filter);

        assertEquals(List.of(3), result.stream().map(Question::getId).toList());
    }

    @Test
    void includeAndExcludeTagFiltersCanBeCombined() {
        List<Question> questions = List.of(
                question(1, 2020, Topic.Graph_Theory, Set.of(Tag.SOLVED, Tag.IMPORTANT)),
                question(2, 2020, Topic.Graph_Theory, Set.of(Tag.SOLVED)),
                question(3, 2020, Topic.Graph_Theory, Set.of(Tag.IMPORTANT))
        );
        Filter filter = Filter.fromParameters(Map.of(
                "tag_SOLVED", "include",
                "tag_IMPORTANT", "exclude"
        ));

        List<Question> result = service.filterAndSort(questions, filter);

        assertEquals(List.of(2), result.stream().map(Question::getId).toList());
    }

    @Test
    void sortsByIdAscendingAndDescending() {
        List<Question> questions = List.of(
                question(8, 2020, Topic.Graph_Theory, Set.of()),
                question(2, 2020, Topic.Graph_Theory, Set.of()),
                question(5, 2020, Topic.Graph_Theory, Set.of())
        );

        assertEquals(List.of(2, 5, 8), service.filterAndSort(questions,
                Filter.fromParameters(Map.of())).stream().map(Question::getId).toList());
        assertEquals(List.of(8, 5, 2), service.filterAndSort(questions,
                Filter.fromParameters(Map.of("direction", "desc"))).stream()
                .map(Question::getId).toList());
    }

    @Test
    void sortsByYearAndKeepsUnassignedYearsLast() {
        List<Question> questions = List.of(
                question(1, 0, Topic.Graph_Theory, Set.of()),
                question(4, 2022, Topic.Graph_Theory, Set.of()),
                question(3, 2020, Topic.Graph_Theory, Set.of()),
                question(2, 2022, Topic.Graph_Theory, Set.of())
        );

        List<Integer> ascending = service.filterAndSort(questions,
                Filter.fromParameters(Map.of("sortBy", "year"))).stream()
                .map(Question::getId).toList();
        List<Integer> descending = service.filterAndSort(questions,
                Filter.fromParameters(Map.of("sortBy", "year", "direction", "desc"))).stream()
                .map(Question::getId).toList();

        assertEquals(List.of(3, 2, 4, 1), ascending);
        assertEquals(List.of(2, 4, 3, 1), descending);
    }

    @Test
    void nullTagsDoNotBreakUnfilteredQueries() {
        List<Question> questions = List.of(question(1, 2020, Topic.Graph_Theory, null));

        assertEquals(1, service.filterAndSort(questions,
                Filter.fromParameters(Map.of())).size());
        assertTrue(service.filterAndSort(questions,
                Filter.fromParameters(Map.of("tag_SOLVED", "include"))).isEmpty());
    }
}
