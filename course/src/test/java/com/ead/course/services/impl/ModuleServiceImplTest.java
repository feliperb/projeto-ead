package com.ead.course.services.impl;

import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ModuleServiceImplTest {

    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private LessonRepository lessonRepository;
    @InjectMocks
    private ModuleServiceImpl moduleService;

    private UUID moduleId;
    private ModuleModel moduleModel;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        moduleId = UUID.randomUUID();
        moduleModel = new ModuleModel();
        moduleModel.setModuleId(moduleId);
        moduleModel.setTitle("Test Module");
        moduleModel.setDescription("Test Description");
        moduleModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // Tests for delete
    @Test
    void delete_nullModuleModel_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> moduleService.delete(null));
        verify(lessonRepository, never()).deleteAll(anyList());
        verify(moduleRepository, never()).delete(any());
    }

    @Test
    void delete_noLessons_deletesModuleOnly() {
        when(lessonRepository.findAllLessonsIntoModule(moduleId)).thenReturn(Collections.emptyList());

        moduleService.delete(moduleModel);

        verify(lessonRepository).findAllLessonsIntoModule(moduleId);
        verify(lessonRepository, never()).deleteAll(anyList());
        verify(moduleRepository).delete(moduleModel);
    }

    @Test
    void delete_withLessons_deletesLessonsAndModule() {
        LessonModel lesson1 = new LessonModel();
        LessonModel lesson2 = new LessonModel();
        List<LessonModel> lessons = List.of(lesson1, lesson2);
        when(lessonRepository.findAllLessonsIntoModule(moduleId)).thenReturn(lessons);

        moduleService.delete(moduleModel);

        verify(lessonRepository).findAllLessonsIntoModule(moduleId);
        verify(lessonRepository).deleteAll(lessons);
        verify(moduleRepository).delete(moduleModel);
    }

    // Tests for deleteById
    @Test
    void deleteById_success_deletesModule() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(lessonRepository.findAllLessonsIntoModule(moduleId)).thenReturn(Collections.emptyList());

        moduleService.deleteById(moduleId);

        verify(moduleRepository).findById(moduleId);
        verify(lessonRepository).findAllLessonsIntoModule(moduleId);
        verify(moduleRepository).delete(moduleModel);
    }

    @Test
    void deleteById_moduleNotFound_throwsException() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> moduleService.deleteById(moduleId));
        verify(moduleRepository).findById(moduleId);
        verify(lessonRepository, never()).deleteAll(anyList());
        verify(moduleRepository, never()).delete(any());
    }

    @Test
    void deleteById_withLessons_deletesAllInOrder() {
        LessonModel lesson1 = new LessonModel();
        List<LessonModel> lessons = List.of(lesson1);
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(lessonRepository.findAllLessonsIntoModule(moduleId)).thenReturn(lessons);

        moduleService.deleteById(moduleId);

        verify(moduleRepository).findById(moduleId);
        verify(lessonRepository).findAllLessonsIntoModule(moduleId);
        verify(lessonRepository).deleteAll(lessons);
        verify(moduleRepository).delete(moduleModel);
    }
}



