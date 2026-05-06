package com.ead.course.services.impl;

import com.ead.course.dtos.ModuleRecordDto;
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
        moduleModel.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // =========================
    // DELETE
    // =========================

    @Test
    void deleteById_success_deletesModule() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(lessonRepository.existsByModule_ModuleId(moduleId)).thenReturn(false);

        moduleService.deleteById(moduleId);

        verify(moduleRepository).findById(moduleId);
        verify(lessonRepository).existsByModule_ModuleId(moduleId);
        verify(moduleRepository).delete(moduleModel);
    }

    @Test
    void deleteById_moduleNotFound_throwsException() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> moduleService.deleteById(moduleId));

        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository, never()).delete(any());
    }

    @Test
    void deleteById_withLessons_throwsConflictException() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(lessonRepository.existsByModule_ModuleId(moduleId)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> moduleService.deleteById(moduleId));

        verify(moduleRepository).findById(moduleId);
        verify(lessonRepository).existsByModule_ModuleId(moduleId);
        verify(moduleRepository, never()).delete(any());
    }

    // =========================
    // CREATE
    // =========================

    @Test
    void create_successfullySavesModule() {
        UUID courseId = UUID.randomUUID();
        CourseModel course = new CourseModel();
        course.setCourseId(courseId);

        ModuleRecordDto dto = new ModuleRecordDto("Test Module", "Test Description");

        when(moduleRepository.existsByTitle(dto.title())).thenReturn(false);
        when(moduleRepository.save(any(ModuleModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModuleModel result = moduleService.create(dto, course);

        assertNotNull(result);
        assertEquals("Test Module", result.getTitle());
        assertEquals(course, result.getCourse());
        assertNotNull(result.getCreationDate());

        verify(moduleRepository).existsByTitle(dto.title());
        verify(moduleRepository).save(any(ModuleModel.class));
    }

    @Test
    void create_nameAlreadyExists_throwsConflictException() {
        ModuleRecordDto dto = new ModuleRecordDto("Test Module", "Test Description");

        when(moduleRepository.existsByTitle(dto.title())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> moduleService.create(dto, new CourseModel()));

        verify(moduleRepository).existsByTitle(dto.title());
        verify(moduleRepository, never()).save(any());
    }

    // =========================
    // GET ALL
    // =========================

    @Test
    void getAllModules_returnsModules() {
        UUID courseId = UUID.randomUUID();

        List<ModuleModel> modules = List.of(new ModuleModel(), new ModuleModel());

        when(moduleRepository.findAllModulesIntoCourse(courseId)).thenReturn(modules);

        List<ModuleModel> result = moduleService.getAllModules(courseId);

        assertEquals(2, result.size());
        verify(moduleRepository).findAllModulesIntoCourse(courseId);
    }

    // ⚠️ REMOVIDO: teste de null (não faz mais sentido no novo padrão)

    // =========================
    // GET BY ID
    // =========================

    @Test
    void getById_success_returnsModule() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));

        ModuleModel result = moduleService.getById(moduleId);

        assertEquals(moduleModel, result);
        verify(moduleRepository).findById(moduleId);
    }

    @Test
    void getById_notFound_throwsException() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> moduleService.getById(moduleId));

        verify(moduleRepository).findById(moduleId);
    }

    // =========================
    // UPDATE
    // =========================

    @Test
    void updateById_success_updatesModule() {
        ModuleRecordDto dto = new ModuleRecordDto("Updated", "Updated Desc");

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(moduleRepository.existsByTitle(dto.title())).thenReturn(false);
        when(moduleRepository.save(any(ModuleModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModuleModel result = moduleService.updateById(moduleId, dto);

        assertEquals("Updated", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());

        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository).save(moduleModel);
    }

    @Test
    void updateById_nameAlreadyExists_throwsConflictException() {
        ModuleRecordDto dto = new ModuleRecordDto("New Name", "Desc");

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(moduleRepository.existsByTitle("New Name")).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> moduleService.updateById(moduleId, dto));

        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository, never()).save(any());
    }

    @Test
    void updateById_notFound_throwsException() {
        ModuleRecordDto dto = new ModuleRecordDto("Updated", "Updated Desc");

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> moduleService.updateById(moduleId, dto));

        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository, never()).save(any());
    }
}