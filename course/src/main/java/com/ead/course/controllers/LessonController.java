package com.ead.course.controllers;

import com.ead.course.dtos.LessonRecordDto;
import com.ead.course.models.LessonModel;
import com.ead.course.services.LessonService;
import com.ead.course.services.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class LessonController {

    final ModuleService moduleService;
    final LessonService lessonService;

    public LessonController(ModuleService moduleService, LessonService lessonService) {
        this.moduleService = moduleService;
        this.lessonService = lessonService;
    }

    @PostMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<Object> saveLesson(@PathVariable UUID moduleId,
                                             @RequestBody @Valid LessonRecordDto lessonRecordDto) {
        try {
            var lesson = lessonService.create(lessonRecordDto, moduleService.getById(moduleId));
            return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/modules/{moduleId}/lessons")
    public ResponseEntity<List<LessonModel>> getAllLessons(@PathVariable UUID moduleId) {
        List<LessonModel> lessons = lessonService.getAllLessons(moduleId);
        return ResponseEntity.ok(lessons != null ? lessons : List.of());
    }

    @GetMapping("/modules/lessons/{lessonId}")
    public ResponseEntity<Object> getLessonById(@PathVariable UUID lessonId) {
        try {
            var lesson = lessonService.getById(lessonId);
            return ResponseEntity.ok(lesson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/modules/lessons/{lessonId}")
    public ResponseEntity<Object> updateLessonById(@PathVariable UUID lessonId,
                                                   @RequestBody @Valid LessonRecordDto lessonRecordeDto) {
        try {
            var lessonUpdated = lessonService.updateById(lessonId, lessonRecordeDto);
            return ResponseEntity.ok(lessonUpdated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/modules/lessons/{lessonId}")
    public ResponseEntity<Object> deleteLessonById(@PathVariable UUID lessonId) {
        try {
            lessonService.deleteById(lessonId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
