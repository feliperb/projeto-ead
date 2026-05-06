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
    private LessonModel lessonModel;
    private ModuleModel moduleModel;
    private LessonRecordDto lessonDto;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        lessonId = UUID.randomUUID();

        moduleModel = new ModuleModel();
        moduleModel.setModuleId(UUID.randomUUID());

        lessonModel = new LessonModel();
        lessonModel.setLessonId(lessonId);
        lessonModel.setTitle("Test Lesson");
        lessonModel.setDescription("Test Description");
        lessonModel.setVideoUrl("video-url");
        lessonModel.setModule(moduleModel);

        lessonDto = new LessonRecordDto(
                "Test Lesson",
                "Test Description",
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
    void create_success_savesLesson() {
        when(lessonRepository.existsByTitle(lessonDto.title())).thenReturn(false);
        when(lessonRepository.save(any(LessonModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LessonModel result = lessonService.create(lessonDto, moduleModel);

        assertNotNull(result);
        assertEquals("Test Lesson", result.getTitle());
        assertEquals(moduleModel, result.getModule());

        verify(lessonRepository).existsByTitle(lessonDto.title());
        verify(lessonRepository).save(any(LessonModel.class));
    }

    @Test
    void create_titleAlreadyExists_throwsConflict() {
        when(lessonRepository.existsByTitle(lessonDto.title())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> lessonService.create(lessonDto, moduleModel));

        verify(lessonRepository).existsByTitle(lessonDto.title());
        verify(lessonRepository, never()).save(any());
    }

    // =========================
    // GET ALL
    // =========================
    @Test
    void getAllLessons_success_returnsList() {
        List<LessonModel> lessons = List.of(new LessonModel(), new LessonModel());

        when(lessonRepository.findByModule_ModuleId(moduleModel.getModuleId()))
                .thenReturn(lessons);

        List<LessonModel> result = lessonService.getAllLessons(moduleModel.getModuleId());

        assertEquals(2, result.size());
        verify(lessonRepository).findByModule_ModuleId(moduleModel.getModuleId());
    }

    @Test
    void getAllLessons_empty_returnsEmptyList() {
        when(lessonRepository.findByModule_ModuleId(moduleModel.getModuleId()))
                .thenReturn(List.of());

        List<LessonModel> result = lessonService.getAllLessons(moduleModel.getModuleId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================
    // GET BY ID
    // =========================
    @Test
    void getById_success_returnsLesson() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lessonModel));

        LessonModel result = lessonService.getById(lessonId);

        assertNotNull(result);
        assertEquals(lessonModel, result);

        verify(lessonRepository).findById(lessonId);
    }

    @Test
    void getById_notFound_throwsException() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.getById(lessonId));
    }

    // =========================
    // UPDATE
    // =========================
    @Test
    void updateById_success_updatesLesson() {
        LessonRecordDto updateDto = new LessonRecordDto(
                "Updated",
                "Updated Desc",
                "new-url"
        );

        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lessonModel));

        when(lessonRepository.save(any(LessonModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LessonModel result = lessonService.updateById(lessonId, updateDto);

        assertEquals("Updated", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());
        assertEquals("new-url", result.getVideoUrl());

        verify(lessonRepository).save(lessonModel);
    }

    @Test
    void updateById_notFound_throwsException() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.updateById(lessonId, lessonDto));

        verify(lessonRepository, never()).save(any());
    }

    // =========================
    // DELETE
    // =========================
    @Test
    void deleteById_success_deletesLesson() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.of(lessonModel));

        lessonService.deleteById(lessonId);

        verify(lessonRepository).findById(lessonId);
        verify(lessonRepository).delete(lessonModel);
    }

    @Test
    void deleteById_notFound_throwsException() {
        when(lessonRepository.findById(lessonId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> lessonService.deleteById(lessonId));

        verify(lessonRepository, never()).delete(any());
    }
}