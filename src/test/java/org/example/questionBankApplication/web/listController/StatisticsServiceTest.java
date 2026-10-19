package org.example.questionBankApplication.web.listController;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.QuestionDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StatisticsServiceTest {

    private final StatisticsService service = new StatisticsService();

    private QuestionDTO classified(int id, Set<Tag> tags) {
        Question question = new Question(id, "q" + id + ".png", 2021,
                Topic.Graph_Theory, Type.MCQ, new MCQ_Answer(AnswerOption.A), tags);
        return new QuestionDTO(question);
    }

    @Test
    void calculatesCountsForClassificationAndRevisionTags() {
        List<QuestionDTO> items = List.of(
                classified(1, Set.of(Tag.SOLVED, Tag.IMPORTANT)),
                classified(2, Set.of(Tag.REPEATED)),
                new QuestionDTO(new Question(3, "q3.png", 0, null, null, null, Set.of()))
        );

        Map<String, Object> stats = service.calculate(items);

        assertEquals(3L, stats.get("totalFilteredCount"));
        assertEquals(2L, stats.get("classifiedQuestions"));
        assertEquals(1L, stats.get("needsMetadataCount"));
        assertEquals(2L, stats.get("taggedQuestions"));
        assertEquals(2L, stats.get("yearAssignedCount"));
        assertEquals(2L, stats.get("subjectAssignedCount"));
        assertEquals(2L, stats.get("topicAssignedCount"));
        assertEquals(2L, stats.get("typeAssignedCount"));
        assertEquals(2L, stats.get("answerAssignedCount"));
        assertEquals(1L, stats.get("missingYearCount"));
        assertEquals(1L, stats.get("missingAnswerCount"));
        assertEquals(1L, stats.get("solvedTagCount"));
        assertEquals(1L, stats.get("repeatedTagCount"));
        assertEquals(1L, stats.get("importantTagCount"));
        assertEquals(0L, stats.get("veryImportantTagCount"));
    }

    @Test
    void returnsZeroCountsForEmptyList() {
        Map<String, Object> stats = service.calculate(List.of());

        assertEquals(0L, stats.get("totalFilteredCount"));
        assertEquals(0L, stats.get("classifiedQuestions"));
        assertEquals(0L, stats.get("needsMetadataCount"));
        assertEquals(0L, stats.get("taggedQuestions"));
        assertEquals(0L, stats.get("missingYearCount"));
        assertEquals(0L, stats.get("solvedTagCount"));
    }
}
