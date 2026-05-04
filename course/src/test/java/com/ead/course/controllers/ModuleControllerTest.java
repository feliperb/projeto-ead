package com.ead.course.controllers;

import com.ead.course.dtos.ModuleRecordeDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
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

class ModuleControllerTest {

    @Mock
    private ModuleService moduleService;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private ModuleController moduleController;

    private UUID courseId;
    private UUID moduleId;
    private CourseModel courseModel;
    private ModuleModel moduleModel;
    private ModuleRecordeDto moduleRecordeDto;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        courseId = UUID.randomUUID();
        moduleId = UUID.randomUUID();

        courseModel = new CourseModel();
        courseModel.setCourseId(courseId);
        courseModel.setName("Test Course");

        moduleModel = new ModuleModel();
        moduleModel.setModuleId(moduleId);
        moduleModel.setTitle("Test Module");
        moduleModel.setDescription("Test Description");
        moduleModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        moduleModel.setCourse(courseModel);

        moduleRecordeDto = new ModuleRecordeDto("Test Module", "Test Description");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    // Tests for saveModule
    @Test
    void saveModule_success_returns201() {
        when(courseService.getByIdOrThrow(courseId)).thenReturn(courseModel);
        when(moduleService.save(moduleRecordeDto, courseModel)).thenReturn(moduleModel);

        ResponseEntity<Object> response = moduleController.saveModule(courseId, moduleRecordeDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(moduleModel, response.getBody());
        verify(courseService).getByIdOrThrow(courseId);
        verify(moduleService).save(moduleRecordeDto, courseModel);
    }

    @Test
    void saveModule_courseNotFound_returns409() {
        when(courseService.getByIdOrThrow(courseId))
                .thenThrow(new IllegalArgumentException("Course not found with id: " + courseId));

        ResponseEntity<Object> response = moduleController.saveModule(courseId, moduleRecordeDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertInstanceOf(String.class, response.getBody());
        verify(courseService).getByIdOrThrow(courseId);
        verify(moduleService, never()).save(any(), any());
    }

    // Tests for getAllModules
    @Test
    void getAllModules_success_returns200() {
        ModuleModel module1 = new ModuleModel();
        ModuleModel module2 = new ModuleModel();
        List<ModuleModel> modules = List.of(module1, module2);

        when(moduleService.findAllModulesIntoCourse(courseId)).thenReturn(modules);

        ResponseEntity<List<ModuleModel>> response = moduleController.getAllModules(courseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(2, response.getBody().size());
        assertEquals(modules, response.getBody());
        verify(moduleService).findAllModulesIntoCourse(courseId);
    }

    @Test
    void getAllModules_noModulesFound_returnsEmptyList() {
        when(moduleService.findAllModulesIntoCourse(courseId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ModuleModel>> response = moduleController.getAllModules(courseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertTrue(response.getBody().isEmpty());
        verify(moduleService).findAllModulesIntoCourse(courseId);
    }

    @Test
    void getAllModules_returnsEmptyListWhenNull() {
        when(moduleService.findAllModulesIntoCourse(courseId)).thenReturn(null);

        ResponseEntity<List<ModuleModel>> response = moduleController.getAllModules(courseId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertTrue(response.getBody().isEmpty());
        verify(moduleService).findAllModulesIntoCourse(courseId);
    }

    // Tests for getModuleById
    @Test
    void getModuleById_success_returns200() {
        when(moduleService.getById(moduleId)).thenReturn(moduleModel);

        ResponseEntity<Object> response = moduleController.getModuleById(moduleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(moduleModel, response.getBody());
        verify(moduleService).getById(moduleId);
    }

    @Test
    void getModuleById_notFound_returns404() {
        when(moduleService.getById(moduleId))
                .thenThrow(new IllegalArgumentException("Module not found with id: " + moduleId));

        ResponseEntity<Object> response = moduleController.getModuleById(moduleId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(String.class, response.getBody());
        assertTrue(((String) response.getBody()).contains("Module not found"));
        verify(moduleService).getById(moduleId);
    }

    // Tests for deleteModuleById
    @Test
    void deleteModuleById_success_returns200() {
        doNothing().when(moduleService).deleteById(moduleId);

        ResponseEntity<Object> response = moduleController.deleteModuleById(moduleId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(moduleService).deleteById(moduleId);
    }

    @Test
    void deleteModuleById_notFound_returns404() {
        doThrow(new IllegalArgumentException("Module not found with id: " + moduleId))
                .when(moduleService).deleteById(moduleId);

        ResponseEntity<Object> response = moduleController.deleteModuleById(moduleId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(String.class, response.getBody());
        assertTrue(((String) response.getBody()).contains("Module not found"));
        verify(moduleService).deleteById(moduleId);
    }

    // Tests for updateModuleById
    @Test
    void updateModuleById_success_returns200() {
        ModuleModel updatedModule = new ModuleModel();
        updatedModule.setModuleId(moduleId);
        updatedModule.setTitle("Updated Title");
        updatedModule.setDescription("Updated Description");
        updatedModule.setCourse(courseModel);

        when(moduleService.updateById(moduleId, moduleRecordeDto)).thenReturn(updatedModule);

        ResponseEntity<Object> response = moduleController.updateModuleById(moduleId, moduleRecordeDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedModule, response.getBody());
        verify(moduleService).updateById(moduleId, moduleRecordeDto);
    }

    @Test
    void updateModuleById_notFound_returns404() {
        when(moduleService.updateById(moduleId, moduleRecordeDto))
                .thenThrow(new IllegalArgumentException("Module not found with id: " + moduleId));

        ResponseEntity<Object> response = moduleController.updateModuleById(moduleId, moduleRecordeDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(String.class, response.getBody());
        assertTrue(((String) response.getBody()).contains("Module not found"));
        verify(moduleService).updateById(moduleId, moduleRecordeDto);
    }

    @Test
    void updateModuleById_returnsUpdatedModule() {
        ModuleModel updatedModule = new ModuleModel();
        updatedModule.setModuleId(moduleId);
        updatedModule.setTitle("New Title");
        updatedModule.setDescription("New Description");

        when(moduleService.updateById(moduleId, moduleRecordeDto)).thenReturn(updatedModule);

        ResponseEntity<Object> response = moduleController.updateModuleById(moduleId, moduleRecordeDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        ModuleModel result = (ModuleModel) response.getBody();
        assertEquals("New Title", result.getTitle());
        assertEquals("New Description", result.getDescription());
        verify(moduleService).updateById(moduleId, moduleRecordeDto);
    }
}

