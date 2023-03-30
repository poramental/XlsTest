package org.xls.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;


//одно занятие с его временем, аудиторией и датой
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class Lesson {
    String startTime;

    String endTime;

    String auditorium;

    String date;

    String dayName;

    List<Teacher>  teachers = new ArrayList<>();

    String name;

    String type;

    public Lesson addTeacher(Teacher teacher){
        teachers.add(teacher);
        return this;
    }

}
