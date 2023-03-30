package org.xls.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;


// карточка преподователя
@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class Teacher {
    String firstName;
    String lastName;
    String Surname;
    String initials;
    List<Lesson> lessons = new ArrayList<>();
    String qualification;
    public Teacher addLesson(Lesson lesson){

        return this;
    }

}
