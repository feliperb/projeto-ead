package com.ead.course.services.impl;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
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
    private LessonRecordDto dto;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        lessonId = UUID.randomUUID();
        moduleId = UUID.randomUUID();

        module = new ModuleModel();
        module.setModuleId(moduleId);

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
        if (closeable != null) closeable.close();
    }

    // =========================
    // CREATE
    // =========================

    @Test
    void create_success_shouldSave() {
        when(lessonRepository.existsByTitleAndModule_ModuleId(dto.title(), moduleId))
                .thenReturn(false);
        when(lessonRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        LessonModel result = lessonService.create(dto, module);

        assertNotNull(result);
        assertEquals(dto.title(), result.getTitle());

        verify(lessonRepository)
                .existsByTitleAndModule_ModuleId(dto.title(), moduleId);
        verify(lessonRepository).save(any());
    }

    @Test
    void create_duplicateTitleSameModule_shouldThrowConflict() {
        when(lessonRepository.existsByTitleAndModule_ModuleId(dto.title(), moduleId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> lessonService.create(dto, module));

        verify(lessonRepository, never()).save(any());
    }

    @Test
    void create_sameTitleDifferentModule_shouldAllow() {
        UUID moduleId = UUID.randomUUID();

        module.setModuleId(moduleId);

        when(lessonRepository.existsByTitleAndModule_ModuleId(dto.title(), moduleId))
                .thenReturn(false);

        when(lessonRepository.save(any(LessonModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LessonModel result = lessonService.create(dto, module);

        assertNotNull(result);

        verify(lessonRepository).existsByTitleAndModule_ModuleId(dto.title(), moduleId);
        verify(lessonRepository).save(any(LessonModel.class));
    }

    // =========================
    // GET
    // =========================

    @Test
    void getById_success() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));

        LessonModel result = lessonService.getById(lessonId);

        assertEquals(lesson, result);
    }

    @Test
    void getById_notFound() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.getById(lessonId));
    }

    @Test
    void getAllLessons_returnsList() {
        when(lessonRepository.findByModule_ModuleId(moduleId))
                .thenReturn(List.of(lesson));

        List<LessonModel> result = lessonService.getAllLessons(moduleId);

        assertEquals(1, result.size());
    }

    // =========================
    // UPDATE
    // =========================

    @Test
    void update_success_nameChanged() {
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

        verify(lessonRepository)
                .existsByTitleAndModule_ModuleId("New Title", moduleId);
    }

    @Test
    void update_duplicateTitleSameModule_shouldThrowConflict() {
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
    void update_sameName_shouldNotCallExists() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));
        when(lessonRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        lessonService.updateById(lessonId, dto);

        verify(lessonRepository, never())
                .existsByTitleAndModule_ModuleId(any(), any());
    }

    @Test
    void update_notFound() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.updateById(lessonId, dto));
    }

    // =========================
    // DELETE
    // =========================

    @Test
    void delete_success() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lesson));

        lessonService.deleteById(lessonId);

        verify(lessonRepository).delete(lesson);
    }

    @Test
    void delete_notFound() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.deleteById(lessonId));

        verify(lessonRepository, never()).delete(any());
    }
}