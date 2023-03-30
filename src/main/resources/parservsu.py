import requests
from bs4 import BeautifulSoup
import re
import os
from pathlib import Path
import shutil

listNameOfFaculties = ["obucheniya-inostrannykh-grazhdan","matematiki-i-it","biologicheskij","pedagogicheskij-fakultet"
,"sotsialnoj-pedagogiki-i-psikhologii","fizicheskoj-kultury-i-sporta","fakultet-gumanitaristiki-i-yazykovyh-kommunikacij","khudozhestvenno-graficheskij","yuridicheskij"]
typesOfShedules = {"экзаменов":"exams",
                "занятий":"lessons",
                "зачётов":"offsets" }
for faculty in listNameOfFaculties:
    html = requests.get(f"https://vsu.by/universitet/fakultety/{faculty}/raspisanie.html")
    soup = BeautifulSoup(html.text,"html.parser")

    tegs = soup.find_all("a",string = re.compile(r"Расписание"))
    links = []
    for teg in tegs:
        if "href" not in teg:
            for item in str(teg).split('"'):
                if "images" in item:
                    links.append(item)
    pathFaculty = Path(Path.cwd(),faculty)
    try:
        os.mkdir(pathFaculty)
    except FileExistsError:
        shutil.rmtree(pathFaculty)
        os.mkdir(pathFaculty)
    finally:
        for i in range(1,6):
            os.mkdir(Path(Path.cwd(),faculty,str(i)))
            for typeOfshedule in typesOfShedules.values():
                pathTypeShedule = Path(Path.cwd(),faculty,str(i),typeOfshedule)
                os.mkdir(pathTypeShedule)

    for link in links:
        try:
            resp = requests.get(f'https://vsu.by/{link}')
            if "курс" in str(link).lower():
                if link.split("курс")[0][-1] != "_":
                    course = link.split("курс")[0][-1]
                else:
                    course = link.split("курс")[0][-2]
            else:
                raise KeyError
            if "расп" in str(link).lower():
                type = typesOfShedules[link.split("Расписание_")[-1].split("_")[0]]
            
            if "экзам" in str(link).lower():
                type = "exams"

            with open(str(pathFaculty)+"\\" +course+"\\"+type+ "\\"+link.split("/")[-1],"wb") as file:
                file.write(resp.content)
        except KeyError:
             with open(str(pathFaculty)+"\\" +link.split("/")[-1],"wb") as file:
                file.write(resp.content)