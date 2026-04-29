package com.ead.course.controllers;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    private CourseModel createCourse(UUID id) {
        CourseModel course = new CourseModel();
        course.setCourseId(id);
        course.setName("Test Course");
        course.setDescription("Test Description");
        return course;
    }

    private CourseRecordDto createDto() {
        return new CourseRecordDto(
                "Test Course",
                "Test Description",
                CourseStatus.IN_PROGRESS,
                CourseLevel.BEGINNER,
                UUID.randomUUID(),
                "image-url"
        );
    }

    // ================= SAVE =================

    @Test
    void givenValidDto_whenSaveCourse_thenReturn201() {
        var dto = createDto();
        var model = createCourse(UUID.randomUUID());

        when(courseService.saveIfNotExists(dto)).thenReturn(model);

        ResponseEntity<Object> response = courseController.saveCourse(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(model, response.getBody());

        verify(courseService).saveIfNotExists(dto);
    }

    @Test
    void givenDuplicatedName_whenSaveCourse_thenReturn409() {
        var dto = createDto();

        when(courseService.saveIfNotExists(dto))
                .thenThrow(new IllegalArgumentException("Course name is already taken"));

        ResponseEntity<Object> response = courseController.saveCourse(dto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("already taken"));

        verify(courseService).saveIfNotExists(dto);
    }

    // ================= GET ALL =================

    @Test
    void whenGetAllCourses_thenReturnList() {
        var courses = List.of(createCourse(UUID.randomUUID()), createCourse(UUID.randomUUID()));

        when(courseService.findAll()).thenReturn(courses);

        ResponseEntity<List<CourseModel>> response = courseController.getAllCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(courseService).findAll();
    }

    @Test
    void whenGetAllCoursesEmpty_thenReturnEmptyList() {
        when(courseService.findAll()).thenReturn(List.of());

        ResponseEntity<List<CourseModel>> response = courseController.getAllCourses();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        var body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());

        verify(courseService).findAll();
    }

    // ================= GET BY ID =================

    @Test
    void givenExistingId_whenGetById_thenReturn200() {
        UUID id = UUID.randomUUID();
        var model = createCourse(id);

        when(courseService.getByIdOrThrow(id)).thenReturn(model);

        ResponseEntity<Object> response = courseController.getCourseById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(model, response.getBody());

        verify(courseService).getByIdOrThrow(id);
    }

    @Test
    void givenInvalidId_whenGetById_thenReturn404() {
        UUID id = UUID.randomUUID();

        when(courseService.getByIdOrThrow(id))
                .thenThrow(new IllegalArgumentException("Course not found"));

        ResponseEntity<Object> response = courseController.getCourseById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("Course not found"));

        verify(courseService).getByIdOrThrow(id);
    }

    // ================= DELETE =================

    @Test
    void givenExistingId_whenDelete_thenReturn200() {
        UUID id = UUID.randomUUID();

        doNothing().when(courseService).deleteById(id);

        ResponseEntity<Object> response = courseController.deleteCourseById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        verify(courseService).deleteById(id);
    }

    @Test
    void givenInvalidId_whenDelete_thenReturn404() {
        UUID id = UUID.randomUUID();

        doThrow(new IllegalArgumentException("Course not found"))
                .when(courseService).deleteById(id);

        ResponseEntity<Object> response = courseController.deleteCourseById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("Course not found"));

        verify(courseService).deleteById(id);
    }

    // ================= UPDATE =================

    @Test
    void givenValidData_whenUpdate_thenReturnUpdatedCourse() {
        UUID id = UUID.randomUUID();
        var dto = createDto();
        var updated = createCourse(id);
        updated.setName("Updated Course");

        when(courseService.updateById(dto, id)).thenReturn(updated);

        ResponseEntity<Object> response = courseController.updateCourseById(id, dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        CourseModel body = assertInstanceOf(CourseModel.class, response.getBody());
        assertEquals("Updated Course", body.getName());

        verify(courseService).updateById(dto, id);
    }

    @Test
    void givenInvalidId_whenUpdate_thenReturn404() {
        UUID id = UUID.randomUUID();
        var dto = createDto();

        when(courseService.updateById(dto, id))
                .thenThrow(new IllegalArgumentException("Course not found"));

        ResponseEntity<Object> response = courseController.updateCourseById(id, dto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        String body = assertInstanceOf(String.class, response.getBody());
        assertTrue(body.contains("Course not found"));

        verify(courseService).updateById(dto, id);
    }
}