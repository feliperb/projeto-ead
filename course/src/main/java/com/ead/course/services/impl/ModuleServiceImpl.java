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

    @Transactional
    protected void delete(ModuleModel moduleModel) {
        if (moduleModel == null) {  throw new IllegalArgumentException("moduleModel não pode ser null");    }
        var lessons = lessonRepository.findAllLessonsIntoModule(moduleModel.getModuleId());
        if (lessons != null && !lessons.isEmpty()) {
            lessonRepository.deleteAll(lessons);
        }
        moduleRepository.delete(moduleModel);
    }

    @Transactional
    @Override
    public void deleteById(UUID moduleId) {
        var moduleModel = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleId));
        delete(moduleModel);
    }

    @Override
    public ModuleModel save(ModuleRecordeDto moduleRecordDto, CourseModel courseModel) {
        var moduleModel = new ModuleModel();
        BeanUtils.copyProperties(moduleRecordDto, moduleModel);
        moduleModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        moduleModel.setCourse(courseModel);
        return moduleRepository.save(moduleModel);
    }

    @Override
    public List<ModuleModel> findAllModulesIntoCourse(UUID courseId) {
        List<ModuleModel> modules = moduleRepository.findAllModulesIntoCourse(courseId);
        return modules != null ? modules : List.of();
    }

    @Override
    public ModuleModel getById(UUID moduleId) {
        return moduleRepository.findByCourseIdAndModuleId(moduleId)
               .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleId));
    }

    private ModuleModel update(ModuleRecordeDto courseRecordDto, ModuleModel moduleModel) {
        BeanUtils.copyProperties(courseRecordDto, moduleModel);
        //moduleModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        return moduleRepository.save(moduleModel);
    }

    @Transactional
    @Override
    public ModuleModel updateById(UUID moduleId, ModuleRecordeDto moduleRecordeDto) {
        var moduleModel = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with id: " + moduleId));
        return update(moduleRecordeDto, moduleModel);
    }

}
