package com.ead.course.services;

import com.ead.course.dtos.ModuleRecordeDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface ModuleService {
    ModuleModel create(@Valid ModuleRecordeDto moduleRecordDto, CourseModel courseModel);
    List<ModuleModel> getAllModules(UUID courseId);
    ModuleModel getById(UUID moduleId);
    ModuleModel updateById(UUID moduleId, @Valid ModuleRecordeDto moduleRecordeDto);
    void deleteById(UUID moduleId);
}


