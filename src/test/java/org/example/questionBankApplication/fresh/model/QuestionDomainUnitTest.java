package org.example.questionBankApplication.fresh.model;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.MSQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionDomainUnitTest {

    private static Question classified(int id, int year, Topic topic, Type type,
                                       org.example.questionBankApplication.model.answer.Answer answer,
                                       Set<Tag> tags) {
        return new Question(id, "Question_Images/q" + id + ".png", year, topic, type, answer, tags);
    }

    @Test
    void unclassifiedConstructorCreatesOnlyTheUnclassifiedState() {
        Question question = new Question(17, "Question_Images/q17.png");

        assertEquals(17, question.getId());
        assertEquals("Question_Images/q17.png", question.getImagePath());
        assertEquals(-9999, question.getYear());
        assertNull(question.getSubject());
        assertNull(question.getTopic());
        assertNull(question.getType());
        assertNull(question.getAnswer());
        assertNull(question.getQuestionTags());
    }

    @Test
    void classifiedConstructorDerivesSubjectAndAcceptsEmptyTags() {
        Question question = classified(3, 1995, Topic.Sorting, Type.MCQ,
                new MCQ_Answer(AnswerOption.A), Set.of());

        assertEquals(1995, question.getYear());
        assertEquals(Topic.Sorting, question.getTopic());
        assertEquals(Subject.Algorithms, question.getSubject());
        assertEquals(Type.MCQ, question.getType());
        assertEquals(AnswerOption.A, ((MCQ_Answer) question.getAnswer()).getCorrectOption());
        assertNotNull(question.getQuestionTags());
        assertTrue(question.getQuestionTags().isEmpty());
    }

    @Test
    void acceptsBothEndsOfTheSupportedYearRange() {
        assertDoesNotThrow(() -> classified(1, 1995, Topic.Sorting, Type.MCQ,
                new MCQ_Answer(AnswerOption.A), Set.of()));
        assertDoesNotThrow(() -> classified(2, 2030, Topic.Sorting, Type.MCQ,
                new MCQ_Answer(AnswerOption.A), Set.of()));
    }

    @Test
    void rejectsNullImagePaths() {
        assertThrows(RuntimeException.class, () -> new Question(1, null));
        assertThrows(RuntimeException.class, () -> new Question(1, null, 2020, Topic.Sorting,
                Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of()));
    }

    @Test
    void rejectsYearsOutsideTheSupportedRange() {
        for (int invalidYear : new int[]{1994, 2031, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            assertThrows(RuntimeException.class, () -> classified(1, invalidYear, Topic.Sorting,
                    Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of()), "year " + invalidYear);
        }
    }

    @Test
    void rejectsMissingRequiredClassifiedFields() {
        assertThrows(RuntimeException.class, () -> new Question(1, "q.png", 2020, null,
                Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of()));
        assertThrows(RuntimeException.class, () -> new Question(1, "q.png", 2020, Topic.Sorting,
                null, new MCQ_Answer(AnswerOption.A), Set.of()));
        assertThrows(RuntimeException.class, () -> new Question(1, "q.png", 2020, Topic.Sorting,
                Type.MCQ, null, Set.of()));
        assertThrows(RuntimeException.class, () -> new Question(1, "q.png", 2020, Topic.Sorting,
                Type.MCQ, new MCQ_Answer(AnswerOption.A), null));
    }

    @Test
    void rejectsAnAnswerImplementationThatDoesNotMatchTheQuestionType() {
        assertThrows(IllegalArgumentException.class, () -> classified(1, 2020, Topic.Sorting,
                Type.MCQ, new MSQ_Answer(Set.of(AnswerOption.A, AnswerOption.B)), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> classified(2, 2020, Topic.Sorting,
                Type.MSQ, new MCQ_Answer(AnswerOption.A), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> classified(3, 2020, Topic.Sorting,
                Type.NAT, new MCQ_Answer(AnswerOption.A), Set.of()));
    }

    @Test
    void setterValidationDoesNotReplaceExistingAnswerWhenRejected() {
        Question question = classified(1, 2020, Topic.Sorting, Type.MCQ,
                new MCQ_Answer(AnswerOption.A), Set.of());

        assertThrows(IllegalArgumentException.class, () ->
                question.setTypeAndAnswer(Type.MSQ, new MCQ_Answer(AnswerOption.B)));

        assertEquals(Type.MCQ, question.getType());
        assertEquals(AnswerOption.A, ((MCQ_Answer) question.getAnswer()).getCorrectOption());
    }

    @Test
    void topicAndSubjectAreAlwaysDerivedFromTheSameMapping() {
        for (Topic topic : Topic.values()) {
            Subject owner = SubjectTopicMap.getSubject(topic);
            assertTrue(SubjectTopicMap.getTopics(owner).contains(topic), topic.name());
            long ownerCount = SubjectTopicMap.getAllSubjects().stream()
                    .filter(subject -> SubjectTopicMap.getTopics(subject).contains(topic))
                    .count();
            assertEquals(1, ownerCount, topic.name());
        }
        assertFalse(SubjectTopicMap.isValidTopic(Subject.Algorithms, Topic.SQL));
    }

    @Test
    void topicMappingRejectsNullAndExposesImmutableTopicSets() {
        assertThrows(IllegalStateException.class, () -> SubjectTopicMap.getSubject(null));
        assertThrows(IllegalStateException.class, () -> SubjectTopicMap.getTopics(null));
        assertThrows(UnsupportedOperationException.class,
                () -> SubjectTopicMap.getTopics(Subject.Algorithms).add(Topic.SQL));
    }

    @Test
    void naturalAnswerAcceptsEqualAndFiniteDecimalBounds() {
        NAT_Answer answer = new NAT_Answer(2.5, 2.5);
        assertEquals(2.5, answer.getMinValue());
        assertEquals(2.5, answer.getMaxValue());

        answer.setRange(-3.25, 8.75);
        assertEquals(-3.25, answer.getMinValue());
        assertEquals(8.75, answer.getMaxValue());
    }

    @Test
    void naturalAnswerRejectsReversedOrNonFiniteBoundsWithoutChangingPriorRange() {
        NAT_Answer answer = new NAT_Answer(1.0, 2.0);

        assertThrows(IllegalArgumentException.class, () -> answer.setRange(3.0, 2.0));
        assertThrows(IllegalArgumentException.class, () -> new NAT_Answer(Double.NaN, 1.0));
        assertThrows(IllegalArgumentException.class, () -> new NAT_Answer(0.0, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new NAT_Answer(Double.NEGATIVE_INFINITY, 0.0));

        assertEquals(1.0, answer.getMinValue());
        assertEquals(2.0, answer.getMaxValue());
    }

    @Test
    void answerObjectsRejectNullRequiredValues() {
        assertThrows(RuntimeException.class, () -> new MCQ_Answer(null));
        assertThrows(RuntimeException.class, () -> new MSQ_Answer(null));
        assertThrows(RuntimeException.class, () -> new MCQ_Answer(AnswerOption.No_Valid_Answer_Stored));
    }
}
