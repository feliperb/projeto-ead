package com.ead.course.services;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.models.CourseModel;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    CourseModel create(@Valid CourseRecordDto courseRecordDto);
    List<CourseModel> getAllCourses();
    CourseModel getById(UUID courseId);
    CourseModel updateById(UUID courseId, @Valid CourseRecordDto courseRecordDto);
    void deleteById(UUID courseId);
}
