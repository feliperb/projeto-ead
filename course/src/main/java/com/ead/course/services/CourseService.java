package com.ead.course.services;

import com.ead.course.dtos.CourseRecordDto;
import com.ead.course.models.CourseModel;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public interface CourseService {
    CourseModel create(@Valid CourseRecordDto courseRecordDto);
    Page<CourseModel> getAllCourses(Specification<CourseModel> spec, Pageable pageable);
    CourseModel getById(UUID courseId);
    CourseModel updateById(UUID courseId, @Valid CourseRecordDto courseRecordDto);
    void deleteById(UUID courseId);
}
