package com.ead.course.repositories;

import com.ead.course.models.ModuleModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModuleRepositoryTest {

    @Mock
    ModuleRepository moduleRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void findAllModulesIntoCourse_returnsModules() {
        UUID courseId = UUID.randomUUID();
        ModuleModel module = mock(ModuleModel.class);
        when(moduleRepository.findAllModulesIntoCourse(courseId)).thenReturn(java.util.List.of(module));
        var result = moduleRepository.findAllModulesIntoCourse(courseId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(module, result.getFirst());
    }

    @Test
    void findAllModulesIntoCourse_returnsEmptyList() {
        UUID courseId = UUID.randomUUID();
        when(moduleRepository.findAllModulesIntoCourse(courseId)).thenReturn(java.util.Collections.emptyList());
        var result = moduleRepository.findAllModulesIntoCourse(courseId);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

