package com.ead.course.services.impl;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.exceptions.BusinessException;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.models.CourseModel;
import com.ead.course.repositories.LessonRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LessonServiceImplTest {

    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private LessonServiceImpl lessonService;

    private UUID lessonId;
    private UUID moduleId;

    private LessonModel lesson;
    private ModuleModel module;
    private CourseModel course;
    private LessonRecordDto dto;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        lessonId = UUID.randomUUID();
        moduleId = UUID.randomUUID();

        course = new CourseModel();
        course.setCourseId(UUID.randomUUID());

        module = new ModuleModel();
        module.setModuleId(moduleId);
        module.setCourse(course);

        lesson = new LessonModel();
        lesson.setLessonId(lessonId);
        lesson.setTitle("Lesson 1");
        lesson.setModule(module);

        dto = new LessonRecordDto(
                "Lesson 1",
                "Desc",
                "video-url"
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // =========================
    // CREATE
    // =========================

    @Test
    void create_shouldSave_whenValid() {
        when(lessonRepository.existsByTitleAndModule_ModuleId(dto.title(), moduleId))
                .thenReturn(false);

        when(lessonRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        LessonModel result = lessonService.create(dto, module);

        assertNotNull(result);
        assertEquals(dto.title(), result.getTitle());

        verify(lessonRepository).save(any());
    }

    @Test
    void create_shouldThrowConflict_whenTitleExists() {
        when(lessonRepository.existsByTitleAndModule_ModuleId(dto.title(), moduleId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> lessonService.create(dto, module));

        verify(lessonRepository, never()).save(any());
    }

    // =========================
    // VALIDATION MODULE (NOVO)
    // =========================

    @Test
    void create_shouldThrowBusinessException_whenModuleIsNull() {
        assertThrows(BusinessException.class,
                () -> lessonService.create(dto, null));
    }

    @Test
    void create_shouldThrowBusinessException_whenModuleHasNoId() {
        module.setModuleId(null);

        assertThrows(BusinessException.class,
                () -> lessonService.create(dto, module));
    }

    @Test
    void create_shouldThrowBusinessException_whenModuleHasNoCourse() {
        module.setCourse(null);

        assertThrows(BusinessException.class,
                () -> lessonService.create(dto, module));
    }

    // =========================
    // GET BY ID
    // =========================

    @Test
    void getById_shouldReturnLesson() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));

        LessonModel result = lessonService.getById(lessonId);

        assertEquals(lesson, result);
    }

    @Test
    void getById_shouldThrowNotFound() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.getById(lessonId));
    }

    // =========================
    // GET ALL
    // =========================

    @Test
    void getAll_shouldReturnList() {
        when(lessonRepository.findByModule_ModuleId(moduleId))
                .thenReturn(List.of(lesson));

        List<LessonModel> result = lessonService.getAllLessons(moduleId);

        assertEquals(1, result.size());
    }

    @Test
    void getAll_shouldThrowBusinessException_whenModuleIdNull() {
        assertThrows(BusinessException.class,
                () -> lessonService.getAllLessons(null));
    }

    // =========================
    // UPDATE
    // =========================

    @Test
    void update_shouldChangeTitle_whenValid() {
        LessonRecordDto updateDto = new LessonRecordDto(
                "New Title",
                "Desc",
                "url"
        );

        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));

        when(lessonRepository.existsByTitleAndModule_ModuleId("New Title", moduleId))
                .thenReturn(false);

        when(lessonRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        LessonModel result = lessonService.updateById(lessonId, updateDto);

        assertEquals("New Title", result.getTitle());

        verify(lessonRepository).save(any());
    }

    @Test
    void update_shouldThrowConflict_whenTitleExists() {
        LessonRecordDto updateDto = new LessonRecordDto(
                "New Title",
                "Desc",
                "url"
        );

        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));

        when(lessonRepository.existsByTitleAndModule_ModuleId("New Title", moduleId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> lessonService.updateById(lessonId, updateDto));

        verify(lessonRepository, never()).save(any());
    }

    @Test
    void update_shouldNotCheckExists_whenTitleUnchanged() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));

        when(lessonRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        lessonService.updateById(lessonId, dto);

        verify(lessonRepository, never())
                .existsByTitleAndModule_ModuleId(any(), any());
    }

    @Test
    void update_shouldThrowNotFound() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.updateById(lessonId, dto));
    }

    // =========================
    // DELETE
    // =========================

    @Test
    void delete_shouldRemoveLesson() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));

        lessonService.deleteById(lessonId);

        verify(lessonRepository).delete(lesson);
    }

    @Test
    void delete_shouldThrowNotFound() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.deleteById(lessonId));
    }
}