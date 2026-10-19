package org.example.questionBankApplication.fresh.web;

import org.example.questionBankApplication.Mappers.JSON_Writer;
import org.example.questionBankApplication.model.Question;
import org.example.questionBankApplication.model.Tag;
import org.example.questionBankApplication.model.answer.AnswerOption;
import org.example.questionBankApplication.model.answer.MCQ_Answer;
import org.example.questionBankApplication.model.answer.MSQ_Answer;
import org.example.questionBankApplication.model.answer.NAT_Answer;
import org.example.questionBankApplication.model.answer.Type;
import org.example.questionBankApplication.model.subject_topic.Topic;
import org.example.questionBankApplication.web.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class QuestionWebWorkflowIntegrationTest {

    private static final Path JSON_DIRECTORY = createInitialDirectory();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private QuestionService questionService;

    private MockMvc mvc;

    @DynamicPropertySource
    static void applicationProperties(DynamicPropertyRegistry registry) {
        registry.add("question-bank.JSON-directory", () -> JSON_DIRECTORY.toString());
        registry.add("question-bank.images-directory", () -> JSON_DIRECTORY.toString());
        registry.add("question-bank.initialize", () -> "false");
        registry.add("question-bank.read", () -> "false");
    }

    private static Path createInitialDirectory() {
        try {
            Path path = Files.createTempDirectory("question-bank-mvc-tests-");
            JSON_Writer.writeQuestions(fixtures(), path);
            return path;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static List<Question> fixtures() {
        return List.of(
                new Question(1, "Question_Images/q1.png"),
                new Question(2, "Question_Images/q2.png", 2020, Topic.Sorting,
                        Type.MCQ, new MCQ_Answer(AnswerOption.A), Set.of(Tag.SOLVED)),
                new Question(3, "Question_Images/q3.png", 2022, Topic.SQL,
                        Type.MSQ, new MSQ_Answer(Set.of(AnswerOption.A, AnswerOption.C)), Set.of(Tag.REPEATED)),
                new Question(4, "Question_Images/q4.png", 2030, Topic.Calculus,
                        Type.NAT, new NAT_Answer(-1.0, 1.0), Set.of(Tag.IMPORTANT)),
                new Question(5, "Question_Images/q5.png", 1995, Topic.Propositional_Logic,
                        Type.MCQ, new MCQ_Answer(AnswerOption.B), Set.of()),
                new Question(6, "Question_Images/q6.png", 2024, Topic.Process,
                        Type.MCQ, new MCQ_Answer(AnswerOption.D), Set.of(Tag.VERY_IMPORTANT)),
                new Question(7, "Question_Images/q7.png", 2024, Topic.Network_Layer_Protocol,
                        Type.MSQ, new MSQ_Answer(Set.of(AnswerOption.B, AnswerOption.E)),
                        Set.of(Tag.SOLVED, Tag.IMPORTANT))
        );
    }

    @BeforeEach
    void resetPersistentFixtureAndBuildMvc() throws IOException {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        try (Stream<Path> files = Files.list(JSON_DIRECTORY)) {
            for (Path file : files.toList()) {
                Files.delete(file);
            }
        }
        JSON_Writer.writeQuestions(fixtures(), JSON_DIRECTORY);
        questionService.loadQuestionsAfterStartup();
    }

    @Test
    void listRouteRendersTheRealTemplateAndProvidesPaginationMetadata() throws Exception {
        MvcResult result = mvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"))
                .andReturn();

        var model = result.getModelAndView().getModel();
        assertEquals(7, model.get("totalCount"));
        assertEquals(2, model.get("totalPages"));
        assertEquals(1, model.get("currentPage"));
        assertEquals(5, ((List<?>) model.get("items")).size());
        assertEquals("/questions?page=1", model.get("returnUrl"));
    }

    @Test
    void secondPageRetainsCurrentSortParametersAndContainsTheRemainingItems() throws Exception {
        MvcResult result = mvc.perform(get("/questions")
                        .param("page", "2")
                        .param("sortBy", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"))
                .andReturn();

        var model = result.getModelAndView().getModel();
        assertEquals(2, model.get("currentPage"));
        assertEquals(2, model.get("totalPages"));
        assertEquals(2, ((List<?>) model.get("items")).size());
        assertEquals("/questions?sortBy=id&direction=asc&page=2", model.get("returnUrl"));
    }

    @Test
    void listRouteAppliesCombinedFiltersAndStatisticsToTheFilteredCollection() throws Exception {
        MvcResult result = mvc.perform(get("/questions")
                        .param("year", "2022")
                        .param("subject", "Databases")
                        .param("topic", "SQL")
                        .param("tag_REPEATED", "include"))
                .andExpect(status().isOk())
                .andExpect(view().name("list"))
                .andReturn();

        var model = result.getModelAndView().getModel();
        assertEquals(1L, model.get("totalFilteredCount"));
        assertEquals(1L, model.get("classifiedQuestions"));
        List<?> items = (List<?>) model.get("items");
        assertEquals(1, items.size());
        assertEquals(3, ((org.example.questionBankApplication.web.QuestionDTO) items.get(0)).getId());
    }

    @Test
    void editGetRetainsAnInternalFilteredReturnUrl() throws Exception {
        String returnUrl = "/questions?year=2022&subject=Databases&sortBy=year&direction=desc&page=1";

        mvc.perform(get("/questions/1/edit").param("returnUrl", returnUrl))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attribute("returnUrl", returnUrl))
                .andExpect(model().attributeExists("form", "editErrors", "subjectOptions", "topicOptions"));
    }

    @Test
    void successfulEditPersistsAllMetadataAndRedirectsBackToTheFilteredList() throws Exception {
        String returnUrl = "/questions?year=2024&subject=Algorithms&sortBy=year&direction=desc&page=2";

        mvc.perform(post("/questions/1/edit")
                        .param("returnUrl", returnUrl)
                        .param("year", "2024")
                        .param("subject", "Algorithms")
                        .param("topic", "Sorting")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "D")
                        .param("selectedTags", "IMPORTANT", "SOLVED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(returnUrl));

        Question saved = questionService.findById(1).orElseThrow();
        assertEquals(2024, saved.getYear());
        assertEquals(Topic.Sorting, saved.getTopic());
        assertEquals(Type.MCQ, saved.getType());
        assertEquals(AnswerOption.D, ((MCQ_Answer) saved.getAnswer()).getCorrectOption());
        assertEquals(Set.of(Tag.IMPORTANT, Tag.SOLVED), saved.getQuestionTags());
        assertTrue(Files.isRegularFile(JSON_DIRECTORY.resolve("question-1.json")));
    }

    @Test
    void invalidSubjectTopicPairReturnsErrorsWithoutPersistingTheEdit() throws Exception {
        Question before = questionService.findById(1).orElseThrow();

        MvcResult result = mvc.perform(post("/questions/1/edit")
                        .param("year", "2024")
                        .param("subject", "Algorithms")
                        .param("topic", "SQL")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "B"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andReturn();

        assertFalse(((List<?>) result.getModelAndView().getModel().get("editErrors")).isEmpty());
        assertSame(before, questionService.findById(1).orElseThrow());
        assertEquals(-9999, questionService.findById(1).orElseThrow().getYear());
    }

    @Test
    void anMsqWithNoSelectedAnswersIsNotSaved() throws Exception {
        Question before = questionService.findById(2).orElseThrow();

        MvcResult result = mvc.perform(post("/questions/2/edit")
                        .param("year", "2024")
                        .param("subject", "Algorithms")
                        .param("topic", "Sorting")
                        .param("type", "MSQ"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andReturn();

        assertFalse(((List<?>) result.getModelAndView().getModel().get("editErrors")).isEmpty());
        assertSame(before, questionService.findById(2).orElseThrow());
        assertEquals(Type.MCQ, questionService.findById(2).orElseThrow().getType());
    }

    @Test
    void reversedNatRangeIsRejectedWithoutChangingStoredData() throws Exception {
        Question before = questionService.findById(1).orElseThrow();

        MvcResult result = mvc.perform(post("/questions/1/edit")
                        .param("year", "2025")
                        .param("subject", "Engineering_Mathematics")
                        .param("topic", "Calculus")
                        .param("type", "NAT")
                        .param("natMinValue", "8.5")
                        .param("natMaxValue", "2.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andReturn();

        assertFalse(((List<?>) result.getModelAndView().getModel().get("editErrors")).isEmpty());
        assertSame(before, questionService.findById(1).orElseThrow());
    }

    @Test
    void returnUrlIsConstrainedToTheLocalQuestionsRoute() throws Exception {
        mvc.perform(get("/questions/1/edit").param("returnUrl", "https://example.com/leave"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andExpect(model().attribute("returnUrl", "/questions"));

        mvc.perform(get("/questions/999/edit").param("returnUrl", "//example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/questions"));
    }

    @Test
    void submittingAllFourAvailableTagsIsRejectedByTheCurrentBackendLimit() throws Exception {
        Question before = questionService.findById(1).orElseThrow();

        MvcResult result = mvc.perform(post("/questions/1/edit")
                        .param("year", "2024")
                        .param("subject", "Algorithms")
                        .param("topic", "Sorting")
                        .param("type", "MCQ")
                        .param("mcqCorrectOption", "B")
                        .param("selectedTags", "IMPORTANT", "VERY_IMPORTANT", "REPEATED", "SOLVED"))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-question"))
                .andReturn();

        assertFalse(((List<?>) result.getModelAndView().getModel().get("editErrors")).isEmpty());
        assertSame(before, questionService.findById(1).orElseThrow());
        assertEquals(-9999, questionService.findById(1).orElseThrow().getYear());
    }
}
