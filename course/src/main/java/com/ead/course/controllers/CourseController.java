package com.ead.course.controllers;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import com.ead.course.specification.SpecificationTemplate;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/courses")
public class CourseController {

    final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseModel> createCourse(@RequestBody @Valid CourseRecordDto dto) {
        log.info("Creating course with name: {}", dto.name());
        CourseModel course = courseService.create(dto);
        log.info("Course created successfully with ID: {}", course.getCourseId());
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @GetMapping
    public ResponseEntity<Page<CourseModel>> getAllCourses(SpecificationTemplate.CourseSpec spec, Pageable pageable) {
        log.debug("Getting all courses with pageable: {}", pageable);
        Page<CourseModel> courses = courseService.getAllCourses(spec, pageable);
        log.debug("Retrieved {} courses", courses.getTotalElements());
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseModel> getCourseById(@PathVariable UUID courseId) {
        log.debug("Getting course by ID: {}", courseId);
        CourseModel course = courseService.getById(courseId);
        log.debug("Course found: {}", course.getName());
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseModel> updateCourse(@PathVariable UUID courseId, @RequestBody @Valid CourseRecordDto dto) {
        log.info("Updating course ID: {} with name: {}", courseId, dto.name());
        CourseModel course = courseService.updateById(courseId, dto);
        log.info("Course updated successfully: {}", course.getName());
        return ResponseEntity.ok(course);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        log.info("Deleting course ID: {}", courseId);
        courseService.deleteById(courseId);
        log.info("Course deleted successfully");
        return ResponseEntity.noContent().build();
    }
}