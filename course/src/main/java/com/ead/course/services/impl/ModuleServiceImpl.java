package com.ead.course.services.impl;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.services.ModuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;

    public ModuleServiceImpl(ModuleRepository moduleRepository, LessonRepository lessonRepository) {
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
    }

    @Transactional
    @Override
    public ModuleModel create(ModuleRecordDto dto, CourseModel course) {
        validateModuleNameAvailability(dto.title());
        ModuleModel module = buildEntity(dto, course);
        return moduleRepository.save(module);
    }

    @Override
    public List<ModuleModel> getAllModules(UUID courseId) {
        return moduleRepository.findAllModulesIntoCourse(courseId); //paginacao por fazer...
    }

    @Override
    public ModuleModel getById(UUID moduleId) {
        return findModuleOrThrow(moduleId);
    }

    @Transactional
    @Override
    public ModuleModel updateById(UUID moduleId, ModuleRecordDto dto) {
        ModuleModel module = findModuleOrThrow(moduleId);
        validateNameChange(dto.title(), module);
        applyUpdates(module, dto);
        return moduleRepository.save(module);
    }

    @Transactional
    @Override
    public void deleteById(UUID moduleId) {
        ModuleModel module = findModuleOrThrow(moduleId);
        validateNoDependencies(moduleId);
        moduleRepository.delete(module);
    }

    // =========================
    // PRIVATE METHODS
    // =========================

    private ModuleModel findModuleOrThrow(UUID moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found with id: " + moduleId));
    }

    private void validateModuleNameAvailability(String title) {
        if (moduleRepository.existsByTitle(title)) {
            throw new ConflictException("Module title is already taken: " + title);
        }
    }

    private void validateNameChange(String newTitle, ModuleModel module) {
        boolean nameChanged = !module.getTitle().equals(newTitle);
        if (nameChanged && moduleRepository.existsByTitle(newTitle)) {
            throw new ConflictException("Module title is already taken: " + newTitle);
        }
    }

    private void validateNoDependencies(UUID moduleId) {
        boolean hasLessons = lessonRepository.existsByModule_ModuleId(moduleId);
        if (hasLessons) {
            throw new ConflictException("Cannot delete module because it has associated lessons");
        }
    }

    private ModuleModel buildEntity(ModuleRecordDto dto, CourseModel course) {
        ModuleModel module = new ModuleModel();

        module.setTitle(dto.title());
        module.setDescription(dto.description());
        module.setCourse(course);
        module.setCreationDate(LocalDateTime.now(ZoneOffset.UTC));

        return module;
    }

    private void applyUpdates(ModuleModel module, ModuleRecordDto dto) {
        module.setTitle(dto.title());
        module.setDescription(dto.description());
    }
}