package com.ead.course.services;

import com.ead.course.dtos.ModuleRecordDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;

import java.util.List;
import java.util.UUID;

public interface ModuleService {
    ModuleModel create(ModuleRecordDto moduleRecordDto, CourseModel courseModel);
    List<ModuleModel> getAllModules(UUID courseId);
    ModuleModel getById(UUID moduleId);
    ModuleModel updateById(UUID moduleId, ModuleRecordDto moduleRecordDto);
    void deleteById(UUID moduleId);
}


