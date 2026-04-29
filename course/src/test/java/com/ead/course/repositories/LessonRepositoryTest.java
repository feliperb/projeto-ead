package com.ead.course.repositories;

import com.ead.course.models.LessonModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LessonRepositoryTest {

    @Mock
    private LessonRepository lessonRepository;

    @Test
    void findAllLessonsIntoModule_returnsLessons() {
        UUID moduleId = UUID.randomUUID();
        LessonModel lesson = mock(LessonModel.class);
        when(lessonRepository.findAllLessonsIntoModule(moduleId)).thenReturn(List.of(lesson));

        List<LessonModel> result = lessonRepository.findAllLessonsIntoModule(moduleId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(lesson, result.getFirst());
    }

    @Test
    void findAllLessonsIntoModules_returnsLessons() {
        UUID moduleId1 = UUID.randomUUID();
        UUID moduleId2 = UUID.randomUUID();
        LessonModel lesson1 = mock(LessonModel.class);
        LessonModel lesson2 = mock(LessonModel.class);
        List<UUID> moduleIds = List.of(moduleId1, moduleId2);
        List<LessonModel> lessons = List.of(lesson1, lesson2);
        when(lessonRepository.findAllLessonsIntoModules(moduleIds)).thenReturn(lessons);

        List<LessonModel> result = lessonRepository.findAllLessonsIntoModules(moduleIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(lesson1));
        assertTrue(result.contains(lesson2));
    }

    @Test
    void findAllLessonsIntoModule_returnsEmptyList() {
        UUID moduleId = UUID.randomUUID();
        when(lessonRepository.findAllLessonsIntoModule(moduleId)).thenReturn(Collections.emptyList());

        List<LessonModel> result = lessonRepository.findAllLessonsIntoModule(moduleId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

