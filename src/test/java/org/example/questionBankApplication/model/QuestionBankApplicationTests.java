package org.example.questionBankApplication.model;

import org.example.questionBankApplication.model.subject_topic.Subject;
import org.example.questionBankApplication.model.subject_topic.SubjectTopicMap;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionBankApplicationTests {

    @Test
    void getAllSubjectsShouldContainEverySubject() {
        assertEquals(
                EnumSet.allOf(Subject.class),
                SubjectTopicMap.getAllSubjects()
        );
    }

    @Test
    void getAllTopicsShouldContainEveryTopic() {
        assertEquals(
                EnumSet.allOf(Topic.class),
                SubjectTopicMap.getAllTopics()
        );
    }

    @Test
    void everySubjectShouldHaveExpectedTopicCount() {

        Map<Subject, Integer> expectedCounts = Map.ofEntries(
                Map.entry(Subject.Discrete_Mathematics, 11),
                Map.entry(Subject.Theory_of_Computation, 10),
                Map.entry(Subject.Digital_Logic, 4),
                Map.entry(Subject.Computer_Organisation_and_Architecture, 9),
                Map.entry(Subject.Data_Structure, 12),
                Map.entry(Subject.Algorithms, 9),
                Map.entry(Subject.C_Programming, 1),
                Map.entry(Subject.Operating_System, 9),
                Map.entry(Subject.Computer_Networks, 6),
                Map.entry(Subject.Databases, 9),
                Map.entry(Subject.Compiler_Design, 6),
                Map.entry(Subject.Engineering_Mathematics, 3),
                Map.entry(Subject.General_Aptitude, 1)
        );

        expectedCounts.forEach((subject, expectedCount) ->
                assertEquals(
                        expectedCount.intValue(),
                        SubjectTopicMap.getTopics(subject).size(),
                        "Incorrect topic count for subject: " + subject
                )
        );
    }

    @Test
    void getSubjectShouldReturnCorrectParent() {
        assertEquals(
                Subject.Discrete_Mathematics,
                SubjectTopicMap.getSubject(Topic.Graph_Theory)
        );

        assertEquals(
                Subject.Databases,
                SubjectTopicMap.getSubject(Topic.SQL)
        );
    }

    @Test
    void isValidTopicShouldValidateSubjectTopicPairs() {
        assertTrue(
                SubjectTopicMap.isValidTopic(
                        Subject.Discrete_Mathematics,
                        Topic.Graph_Theory
                )
        );

        assertFalse(
                SubjectTopicMap.isValidTopic(
                        Subject.Discrete_Mathematics,
                        Topic.SQL
                )
        );
    }

    @Test
    void everyTopicShouldBelongToItsCorrectSubject() {

        for (Subject subject : SubjectTopicMap.getAllSubjects()) {

            Set<Topic> topics = SubjectTopicMap.getTopics(subject);

            for (Topic topic : topics) {
                assertEquals(
                        subject,
                        SubjectTopicMap.getSubject(topic),
                        "Incorrect parent subject for topic: " + topic
                );

                assertTrue(
                        SubjectTopicMap.isValidTopic(subject, topic),
                        "Invalid subject-topic relationship: "
                                + subject + " -> " + topic
                );
            }
        }
    }

    @Test
    void returnedCollectionsShouldBeUnmodifiable() {

        assertThrows(
                UnsupportedOperationException.class,
                () -> SubjectTopicMap.getAllSubjects().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> SubjectTopicMap.getAllTopics().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> SubjectTopicMap
                        .getTopics(Subject.Discrete_Mathematics)
                        .clear()
        );
    }
}
