package org.xls.parser;

import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.xls.entity.Group;
import org.xls.entity.Lesson;
import org.xls.entity.Subgroup;
import org.xls.entity.Teacher;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


@NoArgsConstructor
public  class Parser {

    private List<Teacher> teachers = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();

    private ParserStage parsMode;

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public List<Group> getGroups() {
        return groups;
    }

    // метод удаляет мусорные ячейки из тех которые мы напарсили в методе getAllCellsWhereBorderLeftExist()
    private  List<Cell> returnWorkspace(Workbook wb){
        List<Cell> cells = getAllCellsWhereBorderLeftExist(wb);

        cells.removeIf(cell -> (cell.getStringCellValue().contains("курс") || cell.getStringCellValue().contains("№")) ||
                (((cell.getColumnIndex() == 2 || cell.getColumnIndex() == 0 || cell.getColumnIndex() == 1)
                        && cell.getStringCellValue().equals(""))) ||
                cell.getRowIndex() < 12);


        return cells;
    }

    // метод берет только те ячейки у которых есть левая граница в стилях
    private  List<Cell> getAllCellsWhereBorderLeftExist(Workbook wb){
        Sheet st = wb.getSheetAt(0);
        List<Cell> cells = new ArrayList<>();
        st.forEach(row->{
            row.forEach(
                    cell -> {
                        try {
                            cell.getStringCellValue();
                            if (cell.getCellStyle().getBorderLeft() > 0)
                                cells.add(cell);
                            if(cell.getColumnIndex() == 1)
                                cells.add(cell);
                        }catch (IllegalStateException e){
                            cell.setCellValue(String.valueOf((int)cell.getNumericCellValue()));
                            cells.add(cell);
                        }

                    }
            );

        });
        return cells;
    }
    private void print(List<Cell> cells){
        for(int l = 0; l < cells.size(); l++){
            System.out.println(cells.get(l).getStringCellValue() + " | " + cells.get(l).getColumnIndex() + " | " + cells.get(l).getCellStyle().getBorderLeft());
        }
    }

    private String[] splitTimeCellToArr(Cell cell){
        List<String> arr = Arrays.stream(cell.getStringCellValue().split(" "))
                .collect(Collectors.toList());
        arr.removeIf(x -> x.equals(""));
        return arr.get(arr.size() - 1).split("-");
    }

    private String parseStartTime(Cell cell){
        return splitTimeCellToArr(cell)[0].replace("(","")
                .replace(".",":");
    }

    private String parseEndTime(Cell cell){
        String[] arr = splitTimeCellToArr(cell);
        return arr[arr.length - 1].replace(")","")
                .replace(".",":");
    }
    public void parse(File xlsFile) throws Exception{
        Workbook wb = readWorkbook(xlsFile);
        List<Cell> workspace = returnWorkspace(wb);
        final int length = getLengthOfWorkspace(workspace);
        HashMap<Integer,Subgroup> map = new HashMap<>();

        String day = "";
        String date = "";
        String startTime = "";
        String endTime = "";
        Queue<Lesson> queueOfLessons = new ArrayDeque<>();
        Stack<Lesson> stack = new Stack<>();

        for(int i = 0; i < workspace.size() - 1 ; i++){

            if(i < length / 2) {
                if(workspace.get(i + 1).getStringCellValue().equals("")){
                    throw new Exception("table format Exception.");
                }
                groups.add(new Group().setName(workspace.get(i).getStringCellValue()));
                continue;
            }
            if(i >= length / 2 && i < length ) {
                groups.get(i - length / 2).setId(workspace.get(i).getStringCellValue());
                continue;
            }
            if(i >= length && i < length * 2) {
                if(workspace.get(i + 1).getStringCellValue().equals("")){
                    throw new Exception("table format Exception.");
                }
                int index = (i - length ) / 2;
                Subgroup first = new Subgroup().setId(workspace.get(i).getStringCellValue());
                Subgroup second = new Subgroup().setId(workspace.get(i + 1).getStringCellValue());
                groups.get(index).setFirstSubgroup(first);
                groups.get(index).setSecondSubgroup(second);
                map.put(workspace.get(i).getColumnIndex(), first);
                map.put(workspace.get(i).getColumnIndex() + 1, second);
                i++;
                continue;
            }

            if(workspace.get(i).getCellStyle().getBorderLeft() == 5 && workspace.get(i).getColumnIndex() == 0){
                day = workspace.get(i).getStringCellValue();
                continue;
            }
            if(workspace.get(i).getCellStyle().getBorderLeft() == 5 && workspace.get(i).getColumnIndex() == 2){
                startTime = parseStartTime(workspace.get(i));
                endTime = parseEndTime(workspace.get(i));
                parsMode = ParserStage.PARSER_STAGE_LESSONS;
                continue;
            }
            if(workspace.get(i).getCellStyle().getBorderLeft() == 0 && workspace.get(i).getColumnIndex() == 1){
                date = workspace.get(i).getStringCellValue();
                continue;
            }
            if(parsMode == ParserStage.PARSER_STAGE_LESSONS ) {
                Lesson lesson = parseLesson(workspace.get(i));
                lesson .setDate(date)
                        .setDayName(day)
                        .setStartTime(startTime)
                        .setEndTime(endTime);
                queueOfLessons.add(lesson);
                if ((isAdjacentColumns(workspace.get(i),workspace.get(i + 1)) ||
                        isPenultimateColumnAndTheNextIsTheFirst(workspace.get(i),workspace.get(i+1),length)) ||
                        isSingleColumnInTheRow(workspace.get(i),workspace.get(i+1))){

                    int firstColumnIndex = workspace.get(i).getColumnIndex();
                    int secondColumnIndex = workspace.get(i + 1).getColumnIndex();
                    if(firstColumnIndex == 3 && secondColumnIndex == 3){
                        secondColumnIndex = length + 3;
                    }
                    for(int j = firstColumnIndex; j < secondColumnIndex; j++) {
                        Optional<Group> opt_group = findGroupBySubgroupIndex(map.get(j).getId());
                        if (opt_group.isPresent()) {
                            Group group = opt_group.get();
                            if (isThisLessonInGroup(lesson, group)) {
                                continue;
                            }
                            group.addLesson(lesson);
                        } else
                            throw new Exception("parser exception");
                    }

                } else {
                    map.get(workspace.get(i).getColumnIndex()).addLesson(lesson);
                }
                if(workspace.get(i + 1).getColumnIndex() == 3){
                    parsMode = ParserStage.PARSER_STAGE_TEACHERS;
                    continue;
                }
            }if(parsMode == ParserStage.PARSER_STAGE_TEACHERS){
                Lesson lesson = queueOfLessons.poll();
                stack.add(lesson);

                if(!workspace.get(i).getStringCellValue().equals("")) {
                    if(!workspace.get(i).getStringCellValue().contains(",")) {
                        Teacher teacher = parseTeacher(workspace.get(i).getStringCellValue());
                        lesson.addTeacher(teacher);
                        teachers.add(teacher);
                        teacher.addLesson(lesson);
                    }else{
                        for(String teacherString : splitManyTeachersToList(workspace.get(i))){
                            Teacher teacher = parseTeacher(teacherString);
                            lesson.addTeacher(teacher);
                            teacher.addLesson(lesson);
                        }
                    }
                }
                else
                    lesson.addTeacher(new Teacher());
                if(workspace.get(i + 1).getColumnIndex() == 3){
                    parsMode = ParserStage.PARSER_STAGE_AUDITORIUMS;
                    for(int j = 0; j < stack.size(); j++){
                        queueOfLessons.add(stack.pop());
                    }
                    continue;
                }
            }if(parsMode == ParserStage.PARSER_STAGE_AUDITORIUMS){
                Lesson lesson = queueOfLessons.poll();
                if(lesson == null) continue;
                lesson.setAuditorium(workspace.get(i).getStringCellValue());
                stack.clear();
            }

        }
    }

    private Teacher parseTeacher(String s){
        String teacherLastName = parseTeacherLastname(s);
        String teacherInitials = parseTeacherInitials(s);
        String teacherQualification = parseTeacherQualification(s);
        return new Teacher()
                        .setLastName(teacherLastName)
                        .setQualification(teacherQualification)
                        .setInitials(teacherInitials);


    }


    private Lesson parseLesson(Cell cell){
        String lessonName = parseLessonName(cell);
        String lessonType = parseLessonType(cell);
        return new Lesson()
                .setName(lessonName)
                .setType(lessonType);
    }
    private List<String> splitManyTeachersToList(Cell cell){
        return Arrays.stream(cell.getStringCellValue().split(",")).toList();
    }

    private String[] splitTeacherStringToArr(String s){
        return s.split(" ");

    }

    private String parseTeacherLastname(String s){
        String[] arr = splitTeacherStringToArr(s);
        return arr[0];
    }

    private String parseTeacherQualification(String s){
        String[] arr = splitTeacherStringToArr(s);
        return arr[arr.length - 1].replace("(","").replace(")","");
    }

    private String parseTeacherInitials(String s){
        String[] arr = splitTeacherStringToArr(s);
        return arr[1];
    }



    private boolean isSingleColumnInTheRow(Cell cell, Cell nextCell){
        return (nextCell.getColumnIndex() == 3  && cell.getColumnIndex() == 3);
    }

    private boolean isAdjacentColumns(Cell cell, Cell nextCell){
        return (nextCell.getColumnIndex() - cell.getColumnIndex()) >= 2;

    }
    private boolean isPenultimateColumnAndTheNextIsTheFirst(Cell cell, Cell nextCell, int lengthOfWorkSpace){
        return ((cell.getColumnIndex() == lengthOfWorkSpace  + 1) && nextCell.getColumnIndex() == 3);
    }
    private String parseLessonName(Cell cell){
        List<String> lessonNameArr = Arrays.stream(cell.getStringCellValue().split(" "))
                .filter(x -> !(x.contains("(") && x.contains(")"))).collect(Collectors.toList());
        String lessonName = "";
        for(String word : lessonNameArr ){
            if(!word.equals(""))
                lessonName += word + " ";
        }
        return lessonName;
    }

    private String parseLessonType(Cell cell){
        String cellValue = cell.getStringCellValue();
        String type = "";
        if(cellValue.contains("(лк)"))
            type = "лк";
        if(cellValue.contains("(пз)"))
            type = "пз";
        if(cellValue.contains("(лаб)"))
            type = "лаб";
        return type;
    }

    private boolean isThisLessonInGroup(Lesson lesson, Group group){
        for(Lesson groupLesson : group.getLessons()){
            if(groupLesson.getStartTime().equals(lesson.getStartTime()) && groupLesson.getDate().equals(lesson.getDate())){
                return true;
            }
        }
        return false;

    }

    public Workbook readWorkbook(File file) {
        try {
            Workbook wb = WorkbookFactory.create(file);
            return wb;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }


    private Optional<Group> findGroupBySubgroupIndex(String index){
        for(int i = 0; i < groups.size(); i++ ){
            if(groups.get(i).getFirstSubgroup().getId().equals(index) || groups.get(i).getSecondSubgroup().getId().equals(index)){
                return Optional.of(groups.get(i));
            }
        }
        return Optional.empty();
    }

    private int getLengthOfWorkspace(List<Cell> cells){ // только после returnWorkspace()
        int length = 0;
        for(int i = 1; true; i++) {
            if (cells.get(i).getCellStyle().getBorderLeft() >= 3)
                break;
            length++;
        }
        return (length +1)* 2;
    }


}
