package org.example.questionBankApplication.web.controllerAndHelpers;

import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QuestionControllersTest {

    private QuestionService service;
    private MockMvc mvc;
    private List<Question> questions;

    @BeforeEach
    void setUp() {
        service = mock(QuestionService.class);
        questions = new ArrayList<>();
        for (int id = 1; id <= 11; id++) {
            Question question = new Question(id, "Question_Images/q" + id + ".png",
                    2020 + id % 4, Topic.Graph_Theory, Type.MCQ, null,
                    id % 2 == 0 ? Set.of(Tag.SOLVED) : Set.of());
            question.setMcqAnswer(AnswerOption.A);
            questions.add(question);
        }
        when(service.findAll()).thenReturn(questions);
        when(service.findById(anyInt())).thenAnswer(invocation -> questions.stream()
                .filter(q -> q.getId() == (Integer) invocation.getArgument(0))
                .findFirst());

        QuestionController listController = new QuestionController(service,
                new QuestionQueryService(), new QuestionStatisticsService());
        QuestionEditController editController = new QuestionEditController(service);
        mvc = MockMvcBuilders.standaloneSetup(listController, editController).build();
    }

    @Test
    void listEndpointRendersFirstPageAndCorrectPaginationMetadata() throws Exception {
        mvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"))
                .andExpect(model().attribute("totalCount", 11))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 3))
                .andExpect(model().attribute("pageStart", 1))
                .andExpect(model().attribute("pageEnd", 5))
                .andExpect(model().attribute("hasPreviousPage", false))
                .andExpect(model().attribute("hasNextPage", true))
                .andExpect(model().attributeExists("filterErrors", "subjectTopicOptions", "tagOptions"));
    }

    @Test
    void paginationClampsOutOfRangeAndRetainsFiltersInNavigationUrls() throws Exception {
        MvcResult result = mvc.perform(get("/questions")
                        .param("page", "99")
                        .param("sortBy", "id")
                        .param("direction", "desc")
                        .param("tag_SOLVED", "neutral"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 3))
                .andExpect(model().attribute("pageStart", 11))
                .andExpect(model().attribute("pageEnd", 11))
                .andReturn();

        String nextUrl = (String) result.getModelAndView().getModel().get("nextPageUrl");
        String previousUrl = (String) result.getModelAndView().getModel().get("previousPageUrl");
        assertTrue(nextUrl.contains("sortBy=id"));
        assertTrue(nextUrl.contains("direction=desc"));
        assertTrue(nextUrl.contains("tag_SOLVED=neutral"));
        assertTrue(nextUrl.contains("page=4"));
        assertTrue(previousUrl.contains("page=2"));
        assertFalse(previousUrl.contains("page=99"));
    }

    @Test
    void invalidPageFallsBackToFirstPageAndEmptyFilterHasNoResults() throws Exception {
        mvc.perform(get("/questions").param("page", "-2").param("tag_VERY_IMPORTANT", "include"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 0))
                .andExpect(model().attribute("pageStart", 0))
                .andExpect(model().attribute("pageEnd", 0));
    }

    @Test
    void editPageShowsFormForExistingQuestion() throws Exception {
        mvc.perform(get("/questions/2/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attribute("questionId", 2))
                .andExpect(model().attributeExists("form", "editErrors", "subjectOptions",
                        "topicOptions", "typeOptions", "answerOptions", "tagOptions"));
    }

    @Test
    void editPageRedirectsWhenQuestionDoesNotExist() throws Exception {
        mvc.perform(get("/questions/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void invalidEditDoesNotCallPersistenceService() throws Exception {
        mvc.perform(post("/questions/2/edit")
                        .param("year", "1800")
                        .param("subject", "Discrete_Mathematics")
                        .param("topic", "Graph_Theory")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "A")
                        .param("selectedTags", "SOLVED"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attributeExists("editErrors"));

        verify(service, never()).updateQuestion(any(Question.class));
    }

    @Test
    void invalidSubjectTopicPairDoesNotPersist() throws Exception {
        mvc.perform(post("/questions/2/edit")
                        .param("year", "2024")
                        .param("subject", "Discrete_Mathematics")
                        .param("topic", "SQL")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "B"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attributeExists("editErrors"));

        verify(service, never()).updateQuestion(any(Question.class));
    }

    @Test
    void successfulEditPersistsEditableFieldsButKeepsStoredIdAndImagePath() throws Exception {
        mvc.perform(post("/questions/2/edit")
                        .param("year", "2024")
                        .param("subject", "Discrete_Mathematics")
                        .param("topic", "Graph_Theory")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "D")
                        .param("selectedTags", "IMPORTANT", "REPEATED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions"))
                .andExpect(flash().attributeExists("successMessage"));

        org.mockito.ArgumentCaptor<Question> captor =
                org.mockito.ArgumentCaptor.forClass(Question.class);
        verify(service).updateQuestion(captor.capture());
        Question updated = captor.getValue();
        assertEquals(2, updated.getId());
        assertEquals("Question_Images/q2.png", updated.getImagePath());
        assertEquals(2024, updated.getYear());
        assertEquals(AnswerOption.D,
                ((org.example.questionBankApplication.model.answer.MCQ_Answer) updated.getAnswer())
                        .getCorrectOption());
        assertEquals(Set.of(Tag.IMPORTANT, Tag.REPEATED), updated.getQuestionTags());
    }

    @Test
    void failedPersistenceReturnsEditPageAndDoesNotReportSuccess() throws Exception {
        doThrow(new IOException("test-only simulated write failure"))
                .when(service).updateQuestion(any(Question.class));

        MvcResult result = mvc.perform(post("/questions/2/edit")
                        .param("year", "2024")
                        .param("subject", "Discrete_Mathematics")
                        .param("topic", "Graph_Theory")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "C"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attributeExists("editErrors"))
                .andReturn();

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.getModelAndView().getModel().get("editErrors");
        assertTrue(errors.stream().anyMatch(error -> error.contains("original JSON record has been kept")));
    }

    @Test
    void editForUnknownIdNeverAttemptsToPersist() throws Exception {
        mvc.perform(post("/questions/999/edit")
                        .param("year", "2024")
                        .param("subject", "Discrete_Mathematics")
                        .param("topic", "Graph_Theory")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "A"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions"))
                .andExpect(flash().attributeExists("errorMessage"));

        verify(service, never()).updateQuestion(any(Question.class));
    }
}
