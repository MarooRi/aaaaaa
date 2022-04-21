import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.litote.kmongo.*
import java.io.FileWriter

fun printInHtml() {
    val file = FileWriter("C:/oop/newttt/src/main/kotlin/main.html")

    file.appendHTML().html {
        body {
            div {
                h1 {
                    +"Добро пожаловать в рейтинг групп от Марии Фароновой"
                }
                table {
                    h2 { +"Рейтинг группы" }
                    colGroup("Предмет11")
                    tbody {

                        tr {
                            +"Предмет"

                        }
                        tr {
                            td {
                                +"русский"
                            }
                        }
                        tr {
                            td { +"английский" }
                        }

                    }
                }
            }
        }
    }
    file.close()
}

// Загружаем виды работ
fun inWorks(): List<KindOfWorks> {
    val works = listOf("Lab", "Test", "Course Work").mapIndexed { index, name ->
        when (index) {
            0 -> KindOfWorks(name, 5 * (index + 1))
            1 -> KindOfWorks(name, 5 * (index + 1))
            else -> KindOfWorks(name, 5 * (index + 1))
        }
    }
    worksMongo.insertMany(works)
    return works
}

// Загрущаем на Базу студентов вместе с группами
fun inStudents(kindOfWorks: List<KindOfWorks>): List<Student> {
    // Записываем студентов
    val students = listOf("Maria", "Sasha", "Pasha").mapIndexed { index, name ->
        if (index < 2)
            Student(name, "20z")
        else
            Student(name, "20m")
    }
    studentsMongo.insertMany(students)


    val grades = listOf(listOf(5, 10, 15), listOf(3, 4, 10), listOf(5, 1, 15)) // оценки студентов
    val gradesWithStudents: List<Pair<List<Int>, Student>> = grades.zip(students) { grade, student -> grade to student} // здесь мы соединяем студентов с их оценками
    val namesGroup = students.mapTo(HashSet()){ it.nameGroup } // берем имена групп

    // соединяем оценки с видом работы
    val results = gradesWithStudents.map{
        it.first.mapIndexed{ index, grade ->
            Result(kindOfWorks[index].nameOfWork, it.second.id, it.second.name, grade)
        }
    }.flatMap { it }


    //filter
    val groups = namesGroup.map{ name ->
        val group = students.filter { it.nameGroup == name}
        println("ГРУППА$group")
        Group(name, group.map{
            results.filter {
                result -> it.name == result.nameStudent
            }
        }.flatMap { it }) // если есть студент в группе той, то его добавляем в оценки
    }

    groupsMongo.insertMany(groups)
    return students
}

// Коллекци в Mongo
val studentsMongo = mongoDatabase.getCollection<Student>().apply { drop() }
val groupsMongo = mongoDatabase.getCollection<Group>().apply { drop() }
val worksMongo = mongoDatabase.getCollection<KindOfWorks>().apply { drop() }

fun main() {
    val kindWorks = inWorks()
    val students = inStudents(kindWorks)

    /*printInHtml()
    println("Добрый день! Вы попали в программу выставления рейтинга. " +
            "Вы хотите использовать уже имеющиюся формулу или ввести свою? Если свою, то нажмите 0, если уже имеюся, то 1")*/

/*try {
    do {
        val num = readLine()?.toInt()
    }
    while(num != 1 && num != 0)
}catch (_: Exception){
    println("НЕВЕРНО!!!!!!!!!!!! ФУУУУУУУУ")
}*/


}

