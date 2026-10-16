package org.example.questionBankApplication.web;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ViewDTOTest {

    @Test
    void formatsQuestionIdAndNormalizesWindowsImagePaths() {
        Question question = new Question(12,
                "Question_Images\\Screenshot 2026-10-14 202530.png",
                2021, Topic.Graph_Theory, Type.MCQ, null, Set.of());
        question.setMcqAnswer(AnswerOption.B);

        ViewDTO view = new ViewDTO(question);

        assertEquals("QB-0012", view.getIdLabel());
        assertEquals("/question-images/Screenshot%202026-10-14%20202530.png",
                view.getImageUrl());
    }

    @Test
    void exposesMcqAnswerAndClassifiedStatus() {
        Question question = new Question(1, "q1.png", 2021,
                Topic.Graph_Theory, Type.MCQ, null, Set.of(Tag.SOLVED));
        question.setMcqAnswer(AnswerOption.C);

        ViewDTO view = new ViewDTO(question);

        assertEquals("2021", view.getYearLabel());
        assertEquals("Discrete_Mathematics", view.getSubjectLabel());
        assertEquals("Graph_Theory", view.getTopicLabel());
        assertEquals("MCQ", view.getTypeLabel());
        assertEquals("C", view.getAnswerLabel());
        assertEquals(Set.of("SOLVED"), Set.copyOf(view.getTagLabels()));
        assertTrue(view.isClassified());
        assertEquals("Classified", view.getStatusLabel());
    }

    @Test
    void formatsMsqAndNatAnswers() {
        Question msq = new Question(2, "q2.png", 2021,
                Topic.SQL, Type.MSQ, null, Set.of());
        msq.setMsqAnswer(Set.of(AnswerOption.D, AnswerOption.B));

        Question nat = new Question(3, "q3.png", 2021,
                Topic.SQL, Type.NAT, null, Set.of());
        nat.setNatAnswer(2.5, 3.5);

        assertEquals("B, D", new ViewDTO(msq).getAnswerLabel());
        assertEquals("2.5 – 3.5", new ViewDTO(nat).getAnswerLabel());
    }

    @Test
    void labelsMissingMetadataAndIgnoresNullTagEntries() {
        Set<Tag> tagsWithNull = new HashSet<>();
        tagsWithNull.add(Tag.REPEATED);
        tagsWithNull.add(null);

        Question question = new Question(4, " ", 0,
                null, null, null, tagsWithNull);
        ViewDTO view = new ViewDTO(question);

        assertEquals("Year not set", view.getYearLabel());
        assertEquals("Subject not assigned", view.getSubjectLabel());
        assertEquals("Topic not assigned", view.getTopicLabel());
        assertEquals("Type not set", view.getTypeLabel());
        assertEquals("Answer not assigned", view.getAnswerLabel());
        assertNull(view.getImageUrl());
        assertFalse(view.isClassified());
        assertEquals("Needs details", view.getStatusLabel());
        assertEquals(Set.of("REPEATED"), Set.copyOf(view.getTagLabels()));
    }

    @Test
    void encodesSpecialCharactersInImageFilename() {
        Question question = new Question(5, "Question_Images/question #5 (A).png",
                2020, Topic.Graph_Theory, Type.MCQ, null, Set.of());

        assertEquals("/question-images/question%20%235%20(A).png",
                new ViewDTO(question).getImageUrl());
    }
}
