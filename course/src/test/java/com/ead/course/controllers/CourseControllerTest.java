package com.ead.course.controllers;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.enums.CourseLevel;
import com.ead.course.enums.CourseStatus;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    void createCourse_success_returns201() {
        when(courseService.create(courseRecordDto)).thenReturn(courseModel);
        ResponseEntity<CourseModel> response = courseController.createCourse(courseRecordDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(courseModel, response.getBody());
        verify(courseService).create(courseRecordDto);
    }

    @Test
    void createCourse_duplicatedName_throwsException() {
        when(courseService.create(courseRecordDto)).thenThrow(new ConflictException("Course name is already taken"));
        assertThrows(ConflictException.class, () -> courseController.createCourse(courseRecordDto));
        verify(courseService).create(courseRecordDto);
    }

    // Tests for getAllCourses
    @Test
    void getAllCourses_success_returns200() {
        var pageable = PageRequest.of(0, 10);

        var course1 = new CourseModel();
        var course2 = new CourseModel();

        var page = new PageImpl<>(List.of(course1, course2), pageable, 2);

        when(courseService.getAllCourses(any(), eq(pageable))).thenReturn(page);

        ResponseEntity<Page<CourseModel>> response =
                courseController.getAllCourses(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());

        verify(courseService).getAllCourses(any(), eq(pageable));
    }

    @Test
    void getAllCourses_emptyList_returns200() {
        var pageable = PageRequest.of(0, 10);

        var page = new PageImpl<CourseModel>(List.of(), pageable, 0);

        when(courseService.getAllCourses(any(), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<CourseModel>> response = courseController.getAllCourses(null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getContent().isEmpty());

        verify(courseService).getAllCourses(any(), any(Pageable.class));
    }

    // Tests for getCourseById

    @Test
    void getCourseById_success_returns200() {
        when(courseService.getById(courseId)).thenReturn(courseModel);
        ResponseEntity<CourseModel> response = courseController.getCourseById(courseId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(courseModel, response.getBody());
        verify(courseService).getById(courseId);
    }

    @Test
    void getCourseById_notFound_throwsException() {
        when(courseService.getById(courseId))
                .thenThrow(new NotFoundException("Course not found"));
        assertThrows(NotFoundException.class, () -> courseController.getCourseById(courseId));
        verify(courseService).getById(courseId);
    }

    // Tests for deleteCourseById
    @Test
    void deleteCourseById_success_returns204() {
        doNothing().when(courseService).deleteById(courseId);
        ResponseEntity<Void> response = courseController.deleteCourse(courseId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(courseService).deleteById(courseId);
    }

    @Test
    void deleteCourseById_notFound_throwsException() {
        doThrow(new NotFoundException("Course not found")).when(courseService).deleteById(courseId);
        assertThrows(NotFoundException.class, () -> courseController.deleteCourse(courseId));
        verify(courseService).deleteById(courseId);
    }

    // Tests for updateCourseById
    @Test
    void updateCourseById_success_returns200() {
        var updated = new CourseModel();
        updated.setCourseId(courseId);
        updated.setName("Updated Course");
        when(courseService.updateById(courseId, courseRecordDto)).thenReturn(updated);
        ResponseEntity<CourseModel> response = courseController.updateCourse(courseId, courseRecordDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        CourseModel body = assertInstanceOf(CourseModel.class, response.getBody());
        assertEquals("Updated Course", body.getName());
        verify(courseService).updateById(courseId, courseRecordDto);
    }

    @Test
    void updateCourseById_notFound_throwsException() {
        when(courseService.updateById(courseId, courseRecordDto))
                .thenThrow(new NotFoundException("Course not found"));
        assertThrows(NotFoundException.class, () -> courseController.updateCourse(courseId, courseRecordDto));
        verify(courseService).updateById(courseId, courseRecordDto);
    }
}