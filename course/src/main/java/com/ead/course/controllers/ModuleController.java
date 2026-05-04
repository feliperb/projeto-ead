package com.ead.course.controllers;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.models.ModuleModel;
import com.ead.course.services.CourseService;
import com.ead.course.services.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ModuleController {

    final ModuleService moduleService;
    final CourseService courseService;

    public ModuleController(ModuleService moduleService, CourseService courseService) {
        this.moduleService = moduleService;
        this.courseService = courseService;
    }

    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<Object> saveModule(@PathVariable UUID courseId,
                                             @RequestBody @Valid ModuleRecordDto moduleRecordDto) {
        try {
            var module = moduleService.create(moduleRecordDto, courseService.getById(courseId));
            return ResponseEntity.status(HttpStatus.CREATED).body(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<ModuleModel>> getAllModules(@PathVariable UUID courseId) {
        List<ModuleModel> modules = moduleService.getAllModules(courseId);
        return ResponseEntity.ok(modules != null ? modules : List.of());
    }

    @GetMapping("/courses/modules/{moduleId}")
    public ResponseEntity<Object> getModuleById(@PathVariable UUID moduleId) {
        try {
            var module = moduleService.getById(moduleId);
            return ResponseEntity.ok(module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/courses/modules/{moduleId}")
    public ResponseEntity<Object> updateModuleById(@PathVariable UUID moduleId,
                                                   @RequestBody @Valid ModuleRecordDto moduleRecordeDto) {
        try {
            var moduleUpdated = moduleService.updateById(moduleId, moduleRecordeDto);
            return ResponseEntity.ok(moduleUpdated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/courses/modules/{moduleId}")
    public ResponseEntity<Object> deleteModuleById(@PathVariable UUID moduleId) {
        try {
            moduleService.deleteById(moduleId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
