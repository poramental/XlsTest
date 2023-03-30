package org.xls.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;



@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class Subgroup {

    String id;

    List<Lesson> lessons = new ArrayList<>();
    public void addLesson(Lesson lesson){
        lessons.add(lesson);
    }
}
