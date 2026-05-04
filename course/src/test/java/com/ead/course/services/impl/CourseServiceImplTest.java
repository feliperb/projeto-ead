package com.ead.course.services.impl;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.models.LessonModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.CourseRepository;
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

class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private LessonRepository lessonRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    private UUID courseId;
    private CourseModel courseModel;
    private CourseRecordDto courseRecordDto;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        courseId = UUID.randomUUID();
        courseModel = new CourseModel();
        courseModel.setCourseId(courseId);
        courseModel.setName("Test Course");
        courseModel.setDescription("Test Description");
        courseModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        courseModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        courseRecordDto = new CourseRecordDto(
                "Test Course",
                "Test Description",
                CourseStatus.IN_PROGRESS,
                CourseLevel.BEGINNER,
                UUID.randomUUID(),
                "image-url"
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // Tests for deleteById
    @Test
    void deleteById_successfulDeletion_callsDelete() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(moduleRepository.findAllModulesIntoCourse(courseId)).thenReturn(Collections.emptyList());

        courseService.deleteById(courseId);

        verify(courseRepository).findById(courseId);
        verify(courseRepository).delete(courseModel);
    }

    @Test
    void deleteById_courseNotFound_throwsException() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> courseService.deleteById(courseId));
        verify(courseRepository).findById(courseId);
        verify(courseRepository, never()).delete(any());
    }

    @Test
    void deleteById_withModulesAndLessons_deletesAll() {
        ModuleModel module1 = new ModuleModel();
        ModuleModel module2 = new ModuleModel();
        UUID moduleId1 = UUID.randomUUID();
        UUID moduleId2 = UUID.randomUUID();
        module1.setModuleId(moduleId1);
        module2.setModuleId(moduleId2);

        LessonModel lesson1 = new LessonModel();
        LessonModel lesson2 = new LessonModel();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(moduleRepository.findAllModulesIntoCourse(courseId))
                .thenReturn(List.of(module1, module2));
        when(lessonRepository.findAllLessonsIntoModules(anyList()))
                .thenReturn(List.of(lesson1, lesson2));

        courseService.deleteById(courseId);

        verify(courseRepository).findById(courseId);
        verify(moduleRepository).findAllModulesIntoCourse(courseId);
        verify(lessonRepository).findAllLessonsIntoModules(anyList());
        verify(lessonRepository).deleteAll(List.of(lesson1, lesson2));
        verify(moduleRepository).deleteAll(List.of(module1, module2));
        verify(courseRepository).delete(courseModel);
    }

    @Test
    void deleteById_withModulesNoLessons_deletesModulesAndCourse() {
        ModuleModel module = new ModuleModel();
        UUID moduleId = UUID.randomUUID();
        module.setModuleId(moduleId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(moduleRepository.findAllModulesIntoCourse(courseId))
                .thenReturn(List.of(module));
        when(lessonRepository.findAllLessonsIntoModules(anyList()))
                .thenReturn(Collections.emptyList());

        courseService.deleteById(courseId);

        verify(courseRepository).findById(courseId);
        verify(moduleRepository).findAllModulesIntoCourse(courseId);
        verify(lessonRepository, never()).deleteAll(anyList());
        verify(moduleRepository).deleteAll(List.of(module));
        verify(courseRepository).delete(courseModel);
    }

    // Tests for updateById
    @Test
    void updateById_successfulUpdate_returnsUpdatedCourse() {
        CourseRecordDto updateDto = new CourseRecordDto(
                "Updated Course",
                "Updated Description",
                CourseStatus.IN_PROGRESS,
                CourseLevel.BEGINNER,
                UUID.randomUUID(),
                "updated-image-url"
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(courseRepository.save(any(CourseModel.class))).thenReturn(courseModel);

        CourseModel result = courseService.updateById(courseId, updateDto);

        assertNotNull(result);
        assertEquals(courseId, result.getCourseId());
        verify(courseRepository).findById(courseId);
        verify(courseRepository).save(any(CourseModel.class));
    }

    @Test
    void updateById_courseNotFound_throwsException() {
        CourseRecordDto updateDto = new CourseRecordDto(
                "Updated Course",
                "Updated Description",
                CourseStatus.IN_PROGRESS,
                CourseLevel.BEGINNER,
                UUID.randomUUID(),
                "updated-image-url"
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> courseService.updateById(courseId, updateDto));
        verify(courseRepository).findById(courseId);
        verify(courseRepository, never()).save(any());
    }

    @Test
        void updateById_updatesLastUpdateDate() {
            CourseRecordDto updateDto = new CourseRecordDto(
                    "Updated Course",
                    "Updated Description",
                    CourseStatus.IN_PROGRESS,
                    CourseLevel.BEGINNER,
                    UUID.randomUUID(),
                "updated-image-url"
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        LocalDateTime oldUpdateDate = courseModel.getLastUpdateDate();

        when(courseRepository.save(any(CourseModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        courseService.updateById(courseId, updateDto);

        verify(courseRepository).save(argThat(course ->
            course.getLastUpdateDate().isAfter(oldUpdateDate) ||
            course.getLastUpdateDate().equals(oldUpdateDate)
        ));
    }

    // Tests for saveIfNotExists
    @Test
    void saveIfNotExists_success_savesCourse() {
        when(courseRepository.existsByName(courseRecordDto.name())).thenReturn(false);
        when(courseRepository.save(any(CourseModel.class))).thenReturn(courseModel);

        CourseModel result = courseService.create(courseRecordDto);

        assertNotNull(result);
        assertEquals("Test Course", result.getName());
        verify(courseRepository).existsByName(courseRecordDto.name());
        verify(courseRepository).save(any(CourseModel.class));
    }

    @Test
    void saveIfNotExists_nameAlreadyExists_throwsException() {
        when(courseRepository.existsByName(courseRecordDto.name())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> courseService.create(courseRecordDto));
        verify(courseRepository).existsByName(courseRecordDto.name());
        verify(courseRepository, never()).save(any());
    }

    // Tests for getByIdOrThrow
    @Test
    void getByIdOrThrow_courseExists_returnsCourse() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));

        CourseModel result = courseService.getById(courseId);

        assertNotNull(result);
        assertEquals(courseModel, result);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void getByIdOrThrow_courseNotFound_throwsException() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> courseService.getById(courseId));
        verify(courseRepository).findById(courseId);
    }

    // Tests for findAll
    @Test
    void findAll_returnsAllCourses() {
        CourseModel course1 = new CourseModel();
        CourseModel course2 = new CourseModel();

        when(courseRepository.findAll()).thenReturn(List.of(course1, course2));

        List<CourseModel> result = courseService.getAllCourses();

        assertEquals(2, result.size());
        verify(courseRepository).findAll();
    }

    @Test
    void findAll_emptyList_returnsEmptyList() {
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        List<CourseModel> result = courseService.getAllCourses();

        assertTrue(result.isEmpty());
        verify(courseRepository).findAll();
    }
}
