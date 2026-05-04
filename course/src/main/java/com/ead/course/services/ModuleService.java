package com.ead.course.services;

import com.ead.course.dtos.ModuleRecordeDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface ModuleService {

    void deleteById(UUID moduleId);

    ModuleModel save(@Valid ModuleRecordeDto moduleRecordDto, CourseModel courseModel);

    List<ModuleModel> findAllModulesIntoCourse(UUID courseId);

    ModuleModel getById(UUID moduleId);

    Object updateById(UUID moduleId, @Valid ModuleRecordeDto moduleRecordeDto);
}


