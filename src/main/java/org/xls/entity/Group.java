package org.xls.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

// обьект группы


@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
public class Group {
    String id;

    List<Lesson> lessons = new ArrayList<>(); // общие пары

    List<Subgroup> subgroups = new ArrayList<>();

    String name;

    int countOfSubGroups; // transitional

    public void addLesson(Lesson lesson){
        lessons.add(lesson);
    }

    public void addSubgroup(Subgroup subgroup){
        subgroups.add(subgroup);
    }

}
