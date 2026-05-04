package com.ead.course.controllers;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

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

    @Test
    void saveCourse_success_returns201() {
        when(courseService.saveIfNotExists(courseRecordDto)).thenReturn(courseModel);
        ResponseEntity<Object> response = courseController.saveCourse(courseRecordDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(courseModel, response.getBody());
        verify(courseService).saveIfNotExists(courseRecordDto);
    }

    @Test
    void saveCourse_duplicatedName_returns409() {
        when(courseService.saveIfNotExists(courseRecordDto))
                .thenThrow(new IllegalArgumentException("Course name is already taken"));
        ResponseEntity<Object> response = courseController.saveCourse(courseRecordDto);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("already taken"));
        verify(courseService).saveIfNotExists(courseRecordDto);
    }

    // Tests for getAllCourses
    @Test
    void getAllCourses_success_returns200() {
        var courses = List.of(new CourseModel(), new CourseModel());
        when(courseService.findAll()).thenReturn(courses);
        ResponseEntity<List<CourseModel>> response = courseController.getAllCourses();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(2, response.getBody().size());
        verify(courseService).findAll();
    }

    @Test
    void getAllCourses_emptyList_returns200() {
        when(courseService.findAll()).thenReturn(List.of());
        ResponseEntity<List<CourseModel>> response = courseController.getAllCourses();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertTrue(response.getBody().isEmpty());
        verify(courseService).findAll();
    }

    @Test
    void getAllCourses_returnsEmptyListWhenNull() {
        when(courseService.findAll()).thenReturn(null);
        ResponseEntity<List<CourseModel>> response = courseController.getAllCourses();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assert response.getBody() != null;
        assertTrue(response.getBody().isEmpty());
        verify(courseService).findAll();
    }

    // Tests for getCourseById

    @Test
    void getCourseById_success_returns200() {
        when(courseService.getByIdOrThrow(courseId)).thenReturn(courseModel);
        ResponseEntity<Object> response = courseController.getCourseById(courseId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(courseModel, response.getBody());
        verify(courseService).getByIdOrThrow(courseId);
    }

    @Test
    void getCourseById_notFound_returns404() {
        when(courseService.getByIdOrThrow(courseId))
                .thenThrow(new IllegalArgumentException("Course not found"));
        ResponseEntity<Object> response = courseController.getCourseById(courseId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("Course not found"));
        verify(courseService).getByIdOrThrow(courseId);
    }

    // Tests for deleteCourseById
    @Test
    void deleteCourseById_success_returns200() {
        doNothing().when(courseService).deleteById(courseId);
        ResponseEntity<Object> response = courseController.deleteCourseById(courseId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(courseService).deleteById(courseId);
    }

    @Test
    void deleteCourseById_notFound_returns404() {
        doThrow(new IllegalArgumentException("Course not found"))
                .when(courseService).deleteById(courseId);
        ResponseEntity<Object> response = courseController.deleteCourseById(courseId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("Course not found"));
        verify(courseService).deleteById(courseId);
    }

    // Tests for updateCourseById
    @Test
    void updateCourseById_success_returns200() {
        var updated = new CourseModel();
        updated.setCourseId(courseId);
        updated.setName("Updated Course");
        when(courseService.updateById(courseRecordDto, courseId)).thenReturn(updated);
        ResponseEntity<Object> response = courseController.updateCourseById(courseId, courseRecordDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CourseModel body = assertInstanceOf(CourseModel.class, response.getBody());
        assertEquals("Updated Course", body.getName());
        verify(courseService).updateById(courseRecordDto, courseId);
    }

    @Test
    void updateCourseById_notFound_returns404() {
        when(courseService.updateById(courseRecordDto, courseId))
                .thenThrow(new IllegalArgumentException("Course not found"));
        ResponseEntity<Object> response = courseController.updateCourseById(courseId, courseRecordDto);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("Course not found"));
        verify(courseService).updateById(courseRecordDto, courseId);
    }
}