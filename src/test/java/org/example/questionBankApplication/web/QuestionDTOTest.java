package org.example.questionBankApplication.web;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.MSQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class QuestionDTOTest {

    @Test
    void formatsQuestionIdAndNormalizesWindowsImagePaths() {
        Question question = new Question(12,
                "Question_Images\\Screenshot 2026-10-14 202530.png",
                2021, Topic.Graph_Theory, Type.MCQ, new MCQ_Answer(AnswerOption.B), Set.of());

        QuestionDTO view = new QuestionDTO(question);

        assertEquals("QB-0012", view.getIdLabel());
        assertEquals("/images-folder/Screenshot%202026-10-14%20202530.png",
                view.getImageUrl());
    }

    @Test
    void exposesMcqAnswerAndClassifiedStatus() {
        Question question = new Question(1, "q1.png", 2021,
                Topic.Graph_Theory, Type.MCQ, new MCQ_Answer(AnswerOption.C), Set.of(Tag.SOLVED));

        QuestionDTO view = new QuestionDTO(question);

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
                Topic.SQL, Type.MSQ, new MSQ_Answer(Set.of(AnswerOption.D, AnswerOption.B)), Set.of());

        Question nat = new Question(3, "q3.png", 2021,
                Topic.SQL, Type.NAT, new NAT_Answer(2.5, 3.5), Set.of());

        assertEquals("B, D", new QuestionDTO(msq).getAnswerLabel());
        assertEquals("2.5 – 3.5", new QuestionDTO(nat).getAnswerLabel());
    }

    @Test
    void labelsMissingMetadataAndIgnoresNullTagEntries() {
        Set<Tag> tagsWithNull = new HashSet<>();
        tagsWithNull.add(Tag.REPEATED);
        tagsWithNull.add(null);

        Question question = new Question(4, "Question_Images/q4.png");
        question.setQuestionTags(tagsWithNull);
        QuestionDTO view = new QuestionDTO(question);

        assertEquals("Year not set", view.getYearLabel());
        assertEquals("Subject not assigned", view.getSubjectLabel());
        assertEquals("Topic not assigned", view.getTopicLabel());
        assertEquals("Type not set", view.getTypeLabel());
        assertEquals("Answer not assigned", view.getAnswerLabel());
        assertEquals("/images-folder/q4.png", view.getImageUrl());
        assertFalse(view.isClassified());
        assertEquals("Needs details", view.getStatusLabel());
        assertEquals(Set.of("REPEATED"), Set.copyOf(view.getTagLabels()));
    }

    @Test
    void encodesSpecialCharactersInImageFilename() {
        Question question = new Question(5, "Question_Images/question #5 (A).png",
                2020, Topic.Graph_Theory, Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of());

        assertEquals("/images-folder/question%20%235%20(A).png",
                new QuestionDTO(question).getImageUrl());
    }
}
