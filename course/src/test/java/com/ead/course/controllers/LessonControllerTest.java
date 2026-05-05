package com.ead.course.controllers;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.LessonService;
import com.ead.course.services.ModuleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LessonControllerTest {

    @Mock
    private ModuleService moduleService;

    @Mock
    private LessonService lessonService;

    @InjectMocks
    private LessonController lessonController;

    private UUID moduleId;
    private UUID lessonId;
    private ModuleModel moduleModel;
    private LessonModel lessonModel;
    private LessonRecordDto lessonRecordDto;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        moduleId = UUID.randomUUID();
        lessonId = UUID.randomUUID();

        moduleModel = new ModuleModel();
        moduleModel.setModuleId(moduleId);
        moduleModel.setTitle("Test Module");

        lessonModel = new LessonModel();
        lessonModel.setLessonId(lessonId);
        lessonModel.setTitle("Test Lesson");
        lessonModel.setDescription("Test Description");
        lessonModel.setVideoUrl("video-url");
        lessonModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        lessonModel.setModule(moduleModel);

        lessonRecordDto = new LessonRecordDto("Test Lesson", "Test Description", "video-url");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // Tests for saveLesson
    @Test
    void saveLesson_success_returns201() {
        when(moduleService.getById(moduleId)).thenReturn(moduleModel);
        when(lessonService.create(lessonRecordDto, moduleModel)).thenReturn(lessonModel);

        ResponseEntity<LessonModel> response = lessonController.createLesson(moduleId, lessonRecordDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(lessonModel, response.getBody());
        verify(moduleService).getById(moduleId);
        verify(lessonService).create(lessonRecordDto, moduleModel);
    }

    @Test
    void saveLesson_moduleNotFound_throwsException() {
        when(moduleService.getById(moduleId))
                .thenThrow(new NotFoundException("Module not found"));
        assertThrows(NotFoundException.class, () -> lessonController.createLesson(moduleId, lessonRecordDto));
        verify(moduleService).getById(moduleId);
        verify(lessonService, never()).create(any(), any());
    }

    // Tests for getAllLessons
    @Test
    void getAllLessons_success_returns200() {
        LessonModel lesson1 = new LessonModel();
        LessonModel lesson2 = new LessonModel();
        List<LessonModel> lessons = List.of(lesson1, lesson2);

        when(lessonService.getAllLessons(moduleId)).thenReturn(lessons);

        ResponseEntity<List<LessonModel>> response = lessonController.getAllLessons(moduleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(2, response.getBody().size());
        verify(lessonService).getAllLessons(moduleId);
    }

    @Test
    void getAllLessons_emptyList_returns200() {
        when(lessonService.getAllLessons(moduleId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<LessonModel>> response = lessonController.getAllLessons(moduleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertTrue(response.getBody().isEmpty());
        verify(lessonService).getAllLessons(moduleId);
    }

    // Tests for getLessonById
    @Test
    void getLessonById_success_returns200() {
        when(lessonService.getById(lessonId)).thenReturn(lessonModel);

        ResponseEntity<LessonModel> response = lessonController.getLessonById(lessonId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(lessonModel, response.getBody());
        verify(lessonService).getById(lessonId);
    }

    @Test
    void getLessonById_notFound_throwsException() {
        when(lessonService.getById(lessonId))
                .thenThrow(new NotFoundException("Lesson not found"));
        assertThrows(NotFoundException.class, () -> lessonController.getLessonById(lessonId));
        verify(lessonService).getById(lessonId);
    }

    // Tests for updateLessonById
    @Test
    void updateLessonById_success_returns200() {
        LessonModel updatedLesson = new LessonModel();
        updatedLesson.setLessonId(lessonId);
        updatedLesson.setTitle("Updated Title");
        updatedLesson.setDescription("Updated Description");
        updatedLesson.setVideoUrl("updated-video-url");

        when(lessonService.updateById(lessonId, lessonRecordDto)).thenReturn(updatedLesson);

        ResponseEntity<LessonModel> response = lessonController.updateLesson(lessonId, lessonRecordDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(updatedLesson, response.getBody());
        verify(lessonService).updateById(lessonId, lessonRecordDto);
    }

    @Test
    void updateLessonById_notFound_throwsException() {
        when(lessonService.updateById(lessonId, lessonRecordDto))
                .thenThrow(new NotFoundException("Lesson not found"));
        assertThrows(NotFoundException.class, () -> lessonController.updateLesson(lessonId, lessonRecordDto));
        verify(lessonService).updateById(lessonId, lessonRecordDto);
    }

    @Test
    void updateLessonById_returnsUpdatedLessonWithNewValues() {
        LessonModel updatedLesson = new LessonModel();
        updatedLesson.setLessonId(lessonId);
        updatedLesson.setTitle("New Title");
        updatedLesson.setDescription("New Description");
        updatedLesson.setVideoUrl("new-video-url");

        when(lessonService.updateById(lessonId, lessonRecordDto)).thenReturn(updatedLesson);

        ResponseEntity<LessonModel> response = lessonController.updateLesson(lessonId, lessonRecordDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        LessonModel result = response.getBody();
        assertEquals("New Title", result.getTitle());
        assertEquals("New Description", result.getDescription());
        assertEquals("new-video-url", result.getVideoUrl());
        verify(lessonService).updateById(lessonId, lessonRecordDto);
    }

    // Tests for deleteLessonById
    @Test
    void deleteLessonById_success_returns204() {
        doNothing().when(lessonService).deleteById(lessonId);

        ResponseEntity<Void> response = lessonController.deleteLesson(lessonId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(lessonService).deleteById(lessonId);
    }

    @Test
    void deleteLessonById_notFound_throwsException() {
        doThrow(new NotFoundException("Lesson not found"))
                .when(lessonService).deleteById(lessonId);
        assertThrows(NotFoundException.class, () -> lessonController.deleteLesson(lessonId));
        verify(lessonService).deleteById(lessonId);
    }
}