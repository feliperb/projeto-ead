package com.ead.course.services.impl;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.exceptions.BusinessException;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.services.ModuleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
        validateCourse(course);
        validateModuleNameAvailability(dto.title(), course.getCourseId());
        ModuleModel module = buildEntity(dto, course);
        return moduleRepository.save(module);
    }

    @Override
    public Page<ModuleModel> getAllModulesIntoCourse(Specification<ModuleModel> spec, Pageable pageable) {
        return moduleRepository.findAll(spec, pageable);
    }

    @Override
    public List<ModuleModel> getAllModules(UUID courseId) {
        validateId(courseId, "courseId");
        return Optional.ofNullable(moduleRepository.findAllModulesIntoCourse(courseId)).orElse(List.of());
    }

    @Override
    public ModuleModel getById(UUID moduleId) {
        return findModuleOrThrow(moduleId);
    }

    @Transactional
    @Override
    public ModuleModel updateById(UUID moduleId, ModuleRecordDto dto) {
        ModuleModel module = findModuleOrThrow(moduleId);
        validateModuleOwnership(module);
        validateNameChange(dto.title(), module);
        applyUpdates(module, dto);
        return moduleRepository.save(module);
    }

    @Transactional
    @Override
    public void deleteById(UUID moduleId) {
        validateId(moduleId, "moduleId");
        ModuleModel module = findModuleOrThrow(moduleId);
        validateNoDependencies(moduleId);
        moduleRepository.delete(module);
    }

    // =========================
    // PRIVATE METHODS
    // =========================

    private void validateId(UUID id, String fieldName) {
        if (id == null) {
            throw new BusinessException(fieldName + " cannot be null");
        }
    }

    private void validateCourse(CourseModel course) {
        if (course == null) {throw new BusinessException("Course cannot be null");}
        validateId(course.getCourseId(), "courseId");
    }

    private ModuleModel findModuleOrThrow(UUID moduleId) {
        validateId(moduleId, "moduleId");
        return moduleRepository.findById(moduleId).orElseThrow(() -> new NotFoundException("Module not found with id: " + moduleId));
    }

    private void validateModuleNameAvailability(String title, UUID courseId) {
        if (moduleRepository.existsByTitleAndCourse_CourseId(title, courseId)) {
            throw new ConflictException("Module title is already taken: " + title);
        }
    }

    private void validateNameChange(String newTitle, ModuleModel module) {
        validateModuleOwnership(module);
        boolean nameChanged = !Objects.equals(module.getTitle(), newTitle);
        if (nameChanged && moduleRepository.existsByTitleAndCourse_CourseIdAndModuleIdNot(newTitle, module.getCourse().getCourseId(), module.getModuleId())) {
            throw new ConflictException("Module title is already taken: " + newTitle);
        }
    }

    private void validateModuleOwnership(ModuleModel module) {
        CourseModel course = module.getCourse();
        if (course == null || course.getCourseId() == null) {
            throw new BusinessException("Module must be associated with a valid course");
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