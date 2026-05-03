package com.ead.course.services;

import com.ead.course.dtos.ModuleRecordeDto;
import com.ead.course.models.CourseModel;
import com.ead.course.models.ModuleModel;
import jakarta.validation.Valid;

import java.util.UUID;

public interface ModuleService {

    void delete(ModuleModel moduleModel);

    void deleteById(UUID moduleId);

    ModuleModel save(@Valid ModuleRecordeDto moduleRecordDto, CourseModel courseModel);
}


