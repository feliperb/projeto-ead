package com.ead.course.services.impl;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.models.LessonModel;
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


    // Tests for deleteById
    @Test
    void deleteById_success_deletesModule() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(lessonRepository.findAllLessonsIntoModule(moduleId)).thenReturn(Collections.emptyList());

        moduleService.deleteById(moduleId);

        verify(moduleRepository).findById(moduleId);
        //verify(lessonRepository).findAllLessonsIntoModule(moduleId);
        verify(moduleRepository).delete(moduleModel);
    }

    @Test
    void deleteById_moduleNotFound_throwsException() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> moduleService.deleteById(moduleId));
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
        //verify(lessonRepository).findAllLessonsIntoModule(moduleId);
        //verify(lessonRepository).deleteAll(lessons);
        verify(moduleRepository).delete(moduleModel);
    }

    // Tests for save
    @Test
    void save_successfullySavesModule() {
        UUID courseId = UUID.randomUUID();
        CourseModel courseModel = new CourseModel();
        courseModel.setCourseId(courseId);
        courseModel.setName("Test Course");

        ModuleRecordDto moduleRecordeDto = new ModuleRecordDto("Test Module", "Test Description");

        when(moduleRepository.save(any(ModuleModel.class))).thenAnswer(invocation -> {
            ModuleModel savedModule = invocation.getArgument(0);
            savedModule.setModuleId(UUID.randomUUID());
            return savedModule;
        });

        ModuleModel result = moduleService.create(moduleRecordeDto, courseModel);

        assertNotNull(result);
        assertEquals("Test Module", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(courseModel, result.getCourse());
        assertNotNull(result.getCreationDate());
        verify(moduleRepository).save(any(ModuleModel.class));
    }

    // Tests for findAllModulesIntoCourse
    @Test
    void findAllModulesIntoCourse_returnsModules() {
        UUID courseId = UUID.randomUUID();
        ModuleModel module1 = new ModuleModel();
        ModuleModel module2 = new ModuleModel();
        List<ModuleModel> modules = List.of(module1, module2);

        when(moduleRepository.findAllModulesIntoCourse(courseId)).thenReturn(modules);

        List<ModuleModel> result = moduleService.getAllModules(courseId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(modules, result);
        verify(moduleRepository).findAllModulesIntoCourse(courseId);
    }

    @Test
    void findAllModulesIntoCourse_returnsEmptyListWhenNull() {
        UUID courseId = UUID.randomUUID();

        when(moduleRepository.findAllModulesIntoCourse(courseId)).thenReturn(null);

        List<ModuleModel> result = moduleService.getAllModules(courseId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(moduleRepository).findAllModulesIntoCourse(courseId);
    }

    // Tests for getById
    @Test
    void getById_moduleExists_returnsModule() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));

        ModuleModel result = moduleService.getById(moduleId);

        assertNotNull(result);
        assertEquals(moduleModel, result);
        verify(moduleRepository).findById(moduleId);
    }

    @Test
    void getById_moduleNotFound_throwsException() {
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> moduleService.getById(moduleId));
        verify(moduleRepository).findById(moduleId);
    }

    // Tests for updateById
    @Test
    void updateById_success_updatesModule() {
        ModuleRecordDto updateDto = new ModuleRecordDto("Updated Title", "Updated Description");

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(moduleModel));
        when(moduleRepository.save(any(ModuleModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ModuleModel result = moduleService.updateById(moduleId, updateDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository).save(moduleModel);
    }

    @Test
    void updateById_moduleNotFound_throwsException() {
        ModuleRecordDto updateDto = new ModuleRecordDto("Updated Title", "Updated Description");

        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> moduleService.updateById(moduleId, updateDto));
        verify(moduleRepository).findById(moduleId);
        verify(moduleRepository, never()).save(any());
    }
}
