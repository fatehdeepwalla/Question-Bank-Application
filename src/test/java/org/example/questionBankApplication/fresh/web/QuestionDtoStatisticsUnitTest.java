package org.example.questionBankApplication.fresh.web;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.MSQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.QuestionDTO;
import org.example.questionBankApplication.web.listController.StatisticsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionDtoStatisticsUnitTest {

    @Test
    void buildsSafeLabelsAndEncodedImageUrlForAClassifiedMcq() {
        Question question = new Question(7, "parent\\nested folder\\q 7.png", 2024,
                Topic.Sorting, Type.MCQ, new MCQ_Answer(AnswerOption.C),
                Set.of(Tag.SOLVED, Tag.VERY_IMPORTANT));

        QuestionDTO dto = new QuestionDTO(question);

        assertEquals(7, dto.getId());
        assertEquals("QB-0007", dto.getIdLabel());
        assertEquals("/images-folder/q%207.png", dto.getImageUrl());
        assertEquals("2024", dto.getYearLabel());
        assertTrue(dto.isYearAssigned());
        assertTrue(dto.isSubjectAssigned());
        assertTrue(dto.isTopicAssigned());
        assertEquals("Algorithms", dto.getSubjectLabel());
        assertEquals("Sorting", dto.getTopicLabel());
        assertEquals("MCQ", dto.getTypeLabel());
        assertEquals("C", dto.getAnswerLabel());
        assertTrue(dto.isClassified());
        assertEquals("Classified", dto.getStatusLabel());
        assertTrue(dto.getTagLabels().containsAll(List.of("SOLVED", "VERY_IMPORTANT")));
    }

    @Test
    void buildsDeterministicAnswerLabelsForMsqAndNat() {
        Question msq = new Question(1, "q1.png", 2020, Topic.SQL, Type.MSQ,
                new MSQ_Answer(Set.of(AnswerOption.C, AnswerOption.A)), Set.of());
        Question nat = new Question(2, "q2.png", 2021, Topic.Calculus, Type.NAT,
                new NAT_Answer(1.5, 3.5), Set.of());

        assertEquals("A, C", new QuestionDTO(msq).getAnswerLabel());
        assertEquals("1.5 – 3.5", new QuestionDTO(nat).getAnswerLabel());
    }

    @Test
    void unclassifiedQuestionHasExplicitMissingLabelsAndNoTags() {
        QuestionDTO dto = new QuestionDTO(new Question(5, "q5.png"));

        assertEquals("Year not set", dto.getYearLabel());
        assertEquals("Subject not assigned", dto.getSubjectLabel());
        assertEquals("Topic not assigned", dto.getTopicLabel());
        assertEquals("Type not set", dto.getTypeLabel());
        assertEquals("Answer not assigned", dto.getAnswerLabel());
        assertFalse(dto.isClassified());
        assertEquals("Needs details", dto.getStatusLabel());
        assertTrue(dto.getTagLabels().isEmpty());
        assertFalse(dto.isYearAssigned());
    }

    @Test
    void aBlankImagePathFailsFastDuringDtoConstruction() {
        Question question = new Question(1, "   ");
        assertThrows(RuntimeException.class, () -> new QuestionDTO(question));
    }

    @Test
    void statisticsCountOnlyTheSuppliedFilteredDtos() {
        QuestionDTO taggedClassified = new QuestionDTO(new Question(1, "q1.png", 2020,
                Topic.Sorting, Type.MCQ, new MCQ_Answer(AnswerOption.A),
                Set.of(Tag.IMPORTANT, Tag.SOLVED)));
        QuestionDTO unclassified = new QuestionDTO(new Question(2, "q2.png"));
        QuestionDTO untaggedClassified = new QuestionDTO(new Question(3, "q3.png", 2024,
                Topic.SQL, Type.MSQ, new MSQ_Answer(Set.of(AnswerOption.B, AnswerOption.D)),
                Set.of()));

        var stats = new StatisticsService().calculate(List.of(taggedClassified, unclassified, untaggedClassified));

        assertEquals(3L, stats.get("totalFilteredCount"));
        assertEquals(2L, stats.get("classifiedQuestions"));
        assertEquals(1L, stats.get("needsMetadataCount"));
        assertEquals(1L, stats.get("taggedQuestions"));
        assertEquals(2L, stats.get("yearAssignedCount"));
        assertEquals(1L, stats.get("missingYearCount"));
        assertEquals(1L, stats.get("solvedTagCount"));
        assertEquals(1L, stats.get("importantTagCount"));
        assertEquals(0L, stats.get("veryImportantTagCount"));
    }
}
