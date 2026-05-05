package com.ead.course.services.impl;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.exceptions.ConflictException;
import com.ead.course.exceptions.NotFoundException;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import com.ead.course.repositories.LessonRepository;
import com.ead.course.repositories.ModuleRepository;
import com.ead.course.services.ModuleService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ModuleServiceImpl implements ModuleService {

    final ModuleRepository moduleRepository;
    final LessonRepository lessonRepository;

    public ModuleServiceImpl(ModuleRepository moduleRepository, LessonRepository lessonRepository) {
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
    }

    @Override
    public ModuleModel create(ModuleRecordDto moduleRecordDto, CourseModel courseModel) {
        validateNameAvailability(moduleRecordDto.title());
        var moduleModel = mapToEntity(moduleRecordDto, courseModel);
        setAuditFields(moduleModel);
        return moduleRepository.save(moduleModel);
    }

    @Override
    public List<ModuleModel> getAllModules(UUID courseId) {
        List<ModuleModel> modules = moduleRepository.findAllModulesIntoCourse(courseId);
        return modules != null ? modules : List.of();
    }

    @Override
    public ModuleModel getById(UUID moduleId) {
        return moduleRepository.findById(moduleId)
               .orElseThrow(() -> new NotFoundException("Module not found with id: " + moduleId));
    }

    @Transactional
    @Override
    public ModuleModel updateById(UUID moduleId, ModuleRecordDto moduleRecordeDto) {
        var moduleModel = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found with id: " + moduleId));
        return update(moduleRecordeDto, moduleModel);
    }

    @Transactional
    @Override
    public void deleteById(UUID moduleId) {
        ModuleModel module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found with id: " + moduleId));
        boolean hasLessons = lessonRepository.existsByModule_ModuleId(moduleId);
        if (hasLessons) {   throw new ConflictException("Cannot delete module because it has associated lessons");  }
        moduleRepository.delete(module);
    }



    // PRIVATE METHODS

    private void validateNameAvailability(String name) {
        if (moduleRepository.existsByTitle(name)) {
            throw new ConflictException("Module title is already taken: " + name);
        }
    }

    private ModuleModel mapToEntity(ModuleRecordDto moduleRecordDto, CourseModel courseModel) {
        var moduleModel = new ModuleModel();
        BeanUtils.copyProperties(moduleRecordDto, moduleModel);
        moduleModel.setCourse(courseModel);
        return moduleModel;
    }

    private void setAuditFields(ModuleModel moduleModel) {
        moduleModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
    }

    private ModuleModel update(ModuleRecordDto courseRecordDto, ModuleModel moduleModel) {
        BeanUtils.copyProperties(courseRecordDto, moduleModel);
        //moduleModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return moduleRepository.save(moduleModel);
    }
}
