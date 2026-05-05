package com.ead.course.services.impl;

import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.LessonModel;
import com.ead.course.repositories.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        lessonId = UUID.randomUUID();
        lessonModel = new LessonModel();
        lessonModel.setLessonId(lessonId);
        lessonModel.setTitle("Test Lesson");
        lessonModel.setDescription("Test Description");
        lessonModel.setVideoUrl("video-url");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // Tests for deleteById
    @Test
    void deleteById_success_deletesLesson() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lessonModel));
        doNothing().when(lessonRepository).delete(lessonModel);

        lessonService.deleteById(lessonId);

        verify(lessonRepository, times(1)).findById(lessonId);
        verify(lessonRepository, times(1)).delete(lessonModel);
    }

    @Test
    void deleteById_lessonNotFound_throwsException() {
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> lessonService.deleteById(lessonId));
        verify(lessonRepository, times(1)).findById(lessonId);
        verify(lessonRepository, never()).delete(any());
    }
}


