package com.ead.course.services.impl;

import com.ead.course.dtos.ModuleRecordeDto;
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
    public ModuleModel create(ModuleRecordeDto moduleRecordDto, CourseModel courseModel) {
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
        return moduleRepository.findByCourseIdAndModuleId(moduleId)
               .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleId));
    }

    @Transactional
    @Override
    public ModuleModel updateById(UUID moduleId, ModuleRecordeDto moduleRecordeDto) {
        var moduleModel = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleId));
        return update(moduleRecordeDto, moduleModel);
    }

    @Transactional
    @Override
    public void deleteById(UUID moduleId) {
        var moduleModel = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleId));
        delete(moduleModel);
    }



    // PRIVATE METHODS

    private void validateNameAvailability(String name) {
        if (moduleRepository.existsByTitle(name)) {
            throw new IllegalArgumentException("Course name is already taken: " + name);
        }
    }

    private ModuleModel mapToEntity(ModuleRecordeDto moduleRecordDto, CourseModel courseModel) {
        var moduleModel = new ModuleModel();
        BeanUtils.copyProperties(moduleRecordDto, moduleModel);
        moduleModel.setCourse(courseModel);
        return moduleModel;
    }

    private void setAuditFields(ModuleModel moduleModel) {
        moduleModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
    }

    private ModuleModel update(ModuleRecordeDto courseRecordDto, ModuleModel moduleModel) {
        BeanUtils.copyProperties(courseRecordDto, moduleModel);
        //moduleModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return moduleRepository.save(moduleModel);
    }

    @Transactional
    protected void delete(ModuleModel moduleModel) {
        if (moduleModel == null) {  throw new IllegalArgumentException("moduleModel não pode ser null");    }
        var lessons = lessonRepository.findAllLessonsIntoModule(moduleModel.getModuleId());
        if (lessons != null && !lessons.isEmpty()) {
            lessonRepository.deleteAll(lessons);
        }
        moduleRepository.delete(moduleModel);
    }

}
