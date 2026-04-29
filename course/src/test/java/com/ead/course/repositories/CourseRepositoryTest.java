package com.ead.course.repositories;

import com.ead.course.models.CourseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRepositoryTest {

    @Mock
    private CourseRepository courseRepository;

    @Test
    void delete_deletesCourse() {
        CourseModel course = mock(CourseModel.class);
        doNothing().when(courseRepository).delete(course);

        courseRepository.delete(course);

        verify(courseRepository).delete(course);
    }
}

