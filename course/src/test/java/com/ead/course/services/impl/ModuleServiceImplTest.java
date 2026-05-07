package com.ead.course.services.impl;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.exceptions.BusinessException;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private UUID courseId;

    private ModuleModel moduleModel;
    private CourseModel courseModel;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        moduleId = UUID.randomUUID();
        courseId = UUID.randomUUID();

        courseModel = new CourseModel();
        courseModel.setCourseId(courseId);
        courseModel.setName("Java Course");

        moduleModel = new ModuleModel();
        moduleModel.setModuleId(moduleId);
        moduleModel.setTitle("Test Module");
        moduleModel.setDescription("Test Description");
        moduleModel.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));
        moduleModel.setCourse(courseModel);
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
    void create_success_shouldSaveModule() {
        ModuleRecordDto dto = new ModuleRecordDto(
                "Module 1",
                "Description"
        );

        when(moduleRepository.existsByTitleAndCourse_CourseId(dto.title(), courseId))
                .thenReturn(false);

        when(moduleRepository.save(any(ModuleModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModuleModel result = moduleService.create(dto, courseModel);

        assertNotNull(result);
        assertEquals(dto.title(), result.getTitle());
        assertEquals(dto.description(), result.getDescription());
        assertEquals(courseModel, result.getCourse());

        verify(moduleRepository)
                .existsByTitleAndCourse_CourseId(dto.title(), courseId);

        verify(moduleRepository).save(any(ModuleModel.class));
    }

    @Test
    void create_duplicateTitle_shouldThrowConflictException() {
        ModuleRecordDto dto = new ModuleRecordDto(
                "Module 1",
                "Description"
        );

        when(moduleRepository.existsByTitleAndCourse_CourseId(dto.title(), courseId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> moduleService.create(dto, courseModel));

        verify(moduleRepository, never()).save(any());
    }

    @Test
    void create_nullCourse_shouldThrowBusinessException() {
        ModuleRecordDto dto = new ModuleRecordDto(
                "Module 1",
                "Description"
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> moduleService.create(dto, null)
        );

        assertEquals("Course cannot be null", exception.getMessage());

        verify(moduleRepository, never()).save(any());
    }

    @Test
    void create_courseWithoutId_shouldThrowBusinessException() {
        ModuleRecordDto dto = new ModuleRecordDto(
                "Module 1",
                "Description"
        );

        CourseModel invalidCourse = new CourseModel();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> moduleService.create(dto, invalidCourse)
        );

        assertEquals("courseId cannot be null", exception.getMessage());

        verify(moduleRepository, never()).save(any());
    }

    // =========================
    // GET ALL
    // =========================

    @Test
    void getAllModules_shouldReturnModules() {
        List<ModuleModel> modules = List.of(
                moduleModel,
                new ModuleModel()
        );

        when(moduleRepository.findAllModulesIntoCourse(courseId))
                .thenReturn(modules);

        List<ModuleModel> result = moduleService.getAllModules(courseId);

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(moduleRepository).findAllModulesIntoCourse(courseId);
    }

    @Test
    void getAllModules_whenRepositoryReturnsNull_shouldReturnEmptyList() {
        when(moduleRepository.findAllModulesIntoCourse(courseId))
                .thenReturn(null);

        List<ModuleModel> result = moduleService.getAllModules(courseId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(moduleRepository).findAllModulesIntoCourse(courseId);
    }

    @Test
    void getAllModules_whenCourseIdIsNull_shouldThrowBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> moduleService.getAllModules(null)
        );

        assertEquals("courseId cannot be null", exception.getMessage());

        verify(moduleRepository, never()).findAllModulesIntoCourse(any());
    }

    // =========================
    // GET BY ID
    // =========================

    @Test
    void getById_shouldReturnModule() {
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(moduleModel));

        ModuleModel result = moduleService.getById(moduleId);

        assertNotNull(result);
        assertEquals(moduleModel, result);

        verify(moduleRepository).findById(moduleId);
    }

    @Test
    void getById_whenModuleNotFound_shouldThrowNotFoundException() {
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> moduleService.getById(moduleId));

        verify(moduleRepository).findById(moduleId);
    }

    @Test
    void getById_whenModuleIdIsNull_shouldThrowBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> moduleService.getById(null)
        );

        assertEquals("moduleId cannot be null", exception.getMessage());

        verify(moduleRepository, never()).findById(any());
    }

    // =========================
    // UPDATE
    // =========================

    @Test
    void updateById_success_shouldUpdateModule() {
        ModuleRecordDto updateDto = new ModuleRecordDto(
                "Updated Title",
                "Updated Description"
        );

        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(moduleModel));

        when(moduleRepository.existsByTitleAndCourse_CourseIdAndModuleIdNot(
                updateDto.title(),
                courseId,
                moduleId
        )).thenReturn(false);

        when(moduleRepository.save(any(ModuleModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModuleModel result = moduleService.updateById(moduleId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());

        verify(moduleRepository).findById(moduleId);

        verify(moduleRepository)
                .existsByTitleAndCourse_CourseIdAndModuleIdNot(
                        updateDto.title(),
                        courseId,
                        moduleId
                );

        verify(moduleRepository).save(moduleModel);
    }

    @Test
    void updateById_sameTitle_shouldNotValidateDuplicateName() {
        ModuleRecordDto dto = new ModuleRecordDto(
                "Test Module",
                "New Description"
        );

        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(moduleModel));

        when(moduleRepository.save(any(ModuleModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        moduleService.updateById(moduleId, dto);

        verify(moduleRepository, never())
                .existsByTitleAndCourse_CourseIdAndModuleIdNot(
                        any(),
                        any(),
                        any()
                );

        verify(moduleRepository).save(moduleModel);
    }

    @Test
    void updateById_duplicateTitle_shouldThrowConflictException() {
        ModuleRecordDto updateDto = new ModuleRecordDto(
                "Updated Title",
                "Updated Description"
        );

        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(moduleModel));

        when(moduleRepository.existsByTitleAndCourse_CourseIdAndModuleIdNot(
                updateDto.title(),
                courseId,
                moduleId
        )).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> moduleService.updateById(moduleId, updateDto));

        verify(moduleRepository, never()).save(any());
    }

    @Test
    void updateById_moduleWithoutCourse_shouldThrowBusinessException() {
        ModuleRecordDto updateDto = new ModuleRecordDto(
                "Updated Title",
                "Updated Description"
        );

        moduleModel.setCourse(null);

        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(moduleModel));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> moduleService.updateById(moduleId, updateDto)
        );

        assertEquals(
                "Module must be associated with a valid course",
                exception.getMessage()
        );

        verify(moduleRepository, never()).save(any());
    }

    @Test
    void updateById_moduleNotFound_shouldThrowNotFoundException() {
        ModuleRecordDto updateDto = new ModuleRecordDto(
                "Updated Title",
                "Updated Description"
        );

        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> moduleService.updateById(moduleId, updateDto));

        verify(moduleRepository, never()).save(any());
    }

    // =========================
    // DELETE
    // =========================

    @Test
    void deleteById_success_shouldDeleteModule() {
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(moduleModel));

        when(lessonRepository.existsByModule_ModuleId(moduleId))
                .thenReturn(false);

        moduleService.deleteById(moduleId);

        verify(moduleRepository).findById(moduleId);
        verify(lessonRepository).existsByModule_ModuleId(moduleId);
        verify(moduleRepository).delete(moduleModel);
    }

    @Test
    void deleteById_withLessons_shouldThrowConflictException() {
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.of(moduleModel));

        when(lessonRepository.existsByModule_ModuleId(moduleId))
                .thenReturn(true);

        assertThrows(ConflictException.class,
                () -> moduleService.deleteById(moduleId));

        verify(moduleRepository, never()).delete(any());
    }

    @Test
    void deleteById_moduleNotFound_shouldThrowNotFoundException() {
        when(moduleRepository.findById(moduleId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> moduleService.deleteById(moduleId));

        verify(moduleRepository, never()).delete(any());
    }

    @Test
    void deleteById_whenModuleIdIsNull_shouldThrowBusinessException() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> moduleService.deleteById(null)
        );

        assertEquals("moduleId cannot be null", exception.getMessage());

        verify(moduleRepository, never()).findById(any());
    }
}