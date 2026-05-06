package com.ead.course.services.impl;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.repositories.CourseRepository;
import com.ead.course.repositories.ModuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    // =========================
    // CREATE
    // =========================

    @Test
    void create_success_savesCourse() {
        when(courseRepository.existsByName(courseRecordDto.name())).thenReturn(false);
        when(courseRepository.save(any())).thenReturn(courseModel);

        CourseModel result = courseService.create(courseRecordDto);

        assertNotNull(result);
        assertEquals("Test Course", result.getName());

        verify(courseRepository).existsByName(courseRecordDto.name());
        verify(courseRepository).save(any());
    }

    @Test
    void create_nameAlreadyExists_throwsConflictException() {
        when(courseRepository.existsByName(courseRecordDto.name())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> courseService.create(courseRecordDto));

        verify(courseRepository).existsByName(courseRecordDto.name());
        verify(courseRepository, never()).save(any());
    }

    // =========================
    // GET BY ID
    // =========================

    @Test
    void getById_courseExists_returnsCourse() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));

        CourseModel result = courseService.getById(courseId);

        assertEquals(courseModel, result);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void getById_courseNotFound_throwsException() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> courseService.getById(courseId));

        verify(courseRepository).findById(courseId);
    }

    // =========================
    // GET ALL
    // =========================

    @Test
    void getAllCourses_returnsAllCourses() {
        when(courseRepository.findAll()).thenReturn(List.of(new CourseModel(), new CourseModel()));

        List<CourseModel> result = courseService.getAllCourses();

        assertEquals(2, result.size());
        verify(courseRepository).findAll();
    }

    @Test
    void getAllCourses_emptyList_returnsEmptyList() {
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        List<CourseModel> result = courseService.getAllCourses();

        assertTrue(result.isEmpty());
        verify(courseRepository).findAll();
    }

    // =========================
    // UPDATE
    // =========================

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
        when(courseRepository.existsByName(updateDto.name())).thenReturn(false);
        when(courseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseModel result = courseService.updateById(courseId, updateDto);

        assertEquals("Updated Course", result.getName());
        assertEquals("Updated Description", result.getDescription());

        verify(courseRepository).existsByName(updateDto.name());
        verify(courseRepository).save(any());
    }

    @Test
    void updateById_nameDidNotChange_shouldNotCallExistsByName() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(courseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        courseService.updateById(courseId, courseRecordDto);

        verify(courseRepository, never()).existsByName(any());
        verify(courseRepository).save(any());
    }

    @Test
    void updateById_nameAlreadyExists_throwsConflictException() {
        CourseRecordDto updateDto = new CourseRecordDto(
                "New Name",
                "Desc",
                CourseStatus.IN_PROGRESS,
                CourseLevel.BEGINNER,
                UUID.randomUUID(),
                "img"
        );

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(courseRepository.existsByName("New Name")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> courseService.updateById(courseId, updateDto));

        verify(courseRepository).existsByName("New Name");
        verify(courseRepository, never()).save(any());
    }

    @Test
    void updateById_updatesLastUpdateDate() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(courseRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime oldDate = courseModel.getLastUpdateDate();

        CourseModel result = courseService.updateById(courseId, courseRecordDto);

        assertTrue(result.getLastUpdateDate().isAfter(oldDate));
    }

    // =========================
    // DELETE
    // =========================

    @Test
    void deleteById_successfulDeletion_callsDelete() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(moduleRepository.existsByCourse_CourseId(courseId)).thenReturn(false);

        courseService.deleteById(courseId);

        verify(courseRepository).delete(courseModel);
    }

    @Test
    void deleteById_courseNotFound_shouldNotCheckModules() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> courseService.deleteById(courseId));

        verify(courseRepository, never()).existsByCourseId(any());
    }

    @Test
    void deleteById_withModules_throwsConflictException() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseModel));
        when(moduleRepository.existsByCourse_CourseId(courseId)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> courseService.deleteById(courseId));

        verify(courseRepository, never()).delete(any());
    }
}