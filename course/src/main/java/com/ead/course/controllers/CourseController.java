package com.ead.course.controllers;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.models.CourseModel;
import com.ead.course.services.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
public class CourseController {

    final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseModel> createCourse(@RequestBody @Valid CourseRecordDto dto) {
        CourseModel course = courseService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(course);
    }

    @GetMapping
    public ResponseEntity<List<CourseModel>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseModel> getCourseById(@PathVariable UUID courseId) {
        return ResponseEntity.ok(courseService.getById(courseId));
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<CourseModel> updateCourse(@PathVariable UUID courseId, @RequestBody @Valid CourseRecordDto dto) {
        return ResponseEntity.ok(courseService.updateById(courseId, dto));
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID courseId) {
        courseService.deleteById(courseId);
        return ResponseEntity.noContent().build();
    }
}