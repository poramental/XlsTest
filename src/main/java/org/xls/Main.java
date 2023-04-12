package org.xls;




import org.xls.entity.Group;
import org.xls.entity.Teacher;
import org.xls.parser.Parser;


import java.io.File;
import java.nio.file.Path;
import java.util.List;


public class Main {


    public static void main(String[] args) throws Exception{
        File file = new File("D:\\public\\XlsTest\\src\\main\\resources\\matematiki-i-it\\1\\lessons",
                "Расписание_занятий_1_курс_ФМиИТ_27.03.2023-01.04.2023_3.xls");
        Parser parser = new Parser();
        parser.parse(file);
        List<Group> groups = parser.getGroups();
        for(Group group : groups){
            System.out.println(group);
        }
    }
}


