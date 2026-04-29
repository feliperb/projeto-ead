package com.ead.course.services;

import com.ead.course.models.ModuleModel;

import java.util.UUID;

public interface ModuleService {

    void delete(ModuleModel moduleModel);

    void deleteById(UUID moduleId);
}


