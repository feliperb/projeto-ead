 package com.ead.course.services.impl;

 import com.ead.course.dtos.CourseRecordDto;
 import com.ead.course.enums.CourseLevel;
 import com.ead.course.enums.CourseStatus;
 import com.ead.course.exceptions.BusinessException;
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
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.jpa.domain.Specification;

 import java.time.LocalDateTime;
 import java.time.ZoneOffset;
 import java.util.List;
 import java.util.Optional;
 import java.util.UUID;

 import static org.junit.jupiter.api.Assertions.*;
 import static org.mockito.ArgumentMatchers.*;
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
        courseModel.setCourseStatus(CourseStatus.IN_PROGRESS);
        courseModel.setCourseLevel(CourseLevel.BEGINNER);
        courseModel.setUserInstructor(UUID.randomUUID());
        courseModel.setImageUrl("image-url");
        courseModel.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));
        courseModel.setLastUpdateDate(LocalDateTime.now(ZoneOffset.UTC));

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
    void create_shouldSaveCourse_whenNameIsAvailable() {
        when(courseRepository.existsByName(courseRecordDto.name()))
                .thenReturn(false);

        when(courseRepository.save(any(CourseModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseModel result = courseService.create(courseRecordDto);

        assertNotNull(result);
        assertEquals(courseRecordDto.name(), result.getName());

        verify(courseRepository).existsByName(courseRecordDto.name());
        verify(courseRepository).save(any(CourseModel.class));
    }

    @Test
    void create_shouldThrowConflictException_whenNameAlreadyExists() {
        when(courseRepository.existsByName(courseRecordDto.name()))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> courseService.create(courseRecordDto));

        verify(courseRepository, never()).save(any());
    }

    // =========================
    // GET BY ID
    // =========================

    @Test
    void getById_shouldReturnCourse_whenExists() {
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(courseModel));

        CourseModel result = courseService.getById(courseId);

        assertEquals(courseModel, result);

        verify(courseRepository).findById(courseId);
    }

    @Test
    void getById_shouldThrowNotFound_whenCourseDoesNotExist() {
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> courseService.getById(courseId));

        verify(courseRepository).findById(courseId);
    }

    @Test
    void getById_shouldThrowBusinessException_whenCourseIdIsNull() {
        assertThrows(BusinessException.class,
                () -> courseService.getById(null));

        verify(courseRepository, never()).findById(any());
    }

    // =========================
    // GET ALL
    // =========================

    @Test
    void getAllCourses_shouldReturnPageWithCourses() {
        var pageable = PageRequest.of(0, 10);

        var page = new PageImpl<>(
                List.of(courseModel),
                pageable,
                1
        );

        when(courseRepository.findAll(
                nullable(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        var result = courseService.getAllCourses(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());

        verify(courseRepository).findAll(
                nullable(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void getAllCourses_shouldReturnEmptyPage_whenNoCourses() {
        var pageable = PageRequest.of(0, 10);

        var page = new PageImpl<CourseModel>(
                List.of(),
                pageable,
                0
        );

        when(courseRepository.findAll(
                nullable(Specification.class),
                eq(pageable)
        )).thenReturn(page);

        var result = courseService.getAllCourses(null, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    // =========================
    // UPDATE
    // =========================

    @Test
    void updateById_shouldUpdateCourse_whenValid() {
        var updateDto = new CourseRecordDto(
                "Updated Course",
                "Updated Description",
                CourseStatus.IN_PROGRESS,
                CourseLevel.BEGINNER,
                UUID.randomUUID(),
                "updated-image-url"
        );

        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(courseModel));

        when(courseRepository.existsByName(updateDto.name()))
                .thenReturn(false);

        when(courseRepository.save(any(CourseModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseModel result = courseService.updateById(courseId, updateDto);

        assertEquals("Updated Course", result.getName());
        assertEquals("Updated Description", result.getDescription());

        verify(courseRepository).existsByName(updateDto.name());
        verify(courseRepository).save(any(CourseModel.class));
    }

    @Test
    void updateById_shouldNotCheckName_whenNameDidNotChange() {
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(courseModel));

        when(courseRepository.save(any(CourseModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        courseService.updateById(courseId, courseRecordDto);

        verify(courseRepository, never()).existsByName(any());
        verify(courseRepository).save(any(CourseModel.class));
    }

    @Test
    void updateById_shouldThrowConflict_whenNameAlreadyExists() {
        var updateDto = new CourseRecordDto(
                "New Name",
                "Desc",
                CourseStatus.IN_PROGRESS,
                CourseLevel.BEGINNER,
                UUID.randomUUID(),
                "img"
        );

        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(courseModel));

        when(courseRepository.existsByName("New Name"))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> courseService.updateById(courseId, updateDto));

        verify(courseRepository, never()).save(any());
    }

    @Test
    void updateById_shouldUpdateLastUpdateDate() {
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(courseModel));

        when(courseRepository.save(any(CourseModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime oldDate = courseModel.getLastUpdateDate();

        CourseModel result = courseService.updateById(courseId, courseRecordDto);

        assertTrue(result.getLastUpdateDate().isAfter(oldDate));
    }

    // =========================
    // DELETE
    // =========================

    @Test
    void deleteById_shouldDeleteCourse_whenNoModules() {
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(courseModel));

        when(moduleRepository.existsByCourse_CourseId(courseId))
                .thenReturn(false);

        courseService.deleteById(courseId);

        verify(courseRepository).delete(courseModel);
    }

    @Test
    void deleteById_shouldThrowConflict_whenCourseHasModules() {
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.of(courseModel));

        when(moduleRepository.existsByCourse_CourseId(courseId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> courseService.deleteById(courseId));

        verify(courseRepository, never()).delete(any(CourseModel.class));
    }

    @Test
    void deleteById_shouldThrowNotFound_whenCourseDoesNotExist() {
        when(courseRepository.findById(courseId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> courseService.deleteById(courseId));

        verify(courseRepository, never()).delete(any(CourseModel.class));
    }

    @Test
    void deleteById_shouldThrowBusinessException_whenCourseIdIsNull() {
        assertThrows(BusinessException.class,
                () -> courseService.deleteById(null));

        verify(courseRepository, never()).findById(any());
    }
}