package org.xls;




import org.xls.entity.Group;
import org.xls.entity.Teacher;
import org.xls.parser.Parser;


import java.io.File;
import java.nio.file.Path;


public class Main {


    public static void main(String[] args) throws Exception{
        File file = new File("D:\\public\\XlsTest\\src\\main\\resources\\obucheniya-inostrannykh-grazhdan\\2\\lessons",
                "Расписание_занятий_2_курс_27.03.23-01.04.23.xls");
        Parser parser = new Parser();
        parser.parse(file);
        for(Group group : parser.getGroups()){
            System.out.println(group);
        }
    }
}


