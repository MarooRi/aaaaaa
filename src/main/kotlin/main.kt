import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.litote.kmongo.*
import java.io.FileWriter
import kotlin.system.exitProcess

fun printInHtml(list : List<Any>) {
    val file = FileWriter("C:/oop/course/src/main/kotlin/main.html")

    file.appendHTML().html {
        body {
            div {
                h1 {
                    +"${list[0]}"
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
//создание таблицы, которые мы будем передавать потом
fun createTable(formula: Formula, students:List<Student>, works: List<KindOfWorks>, grades:List<Result>): List<Any> {
        val table = listOf("Рейтинг " + students[0].nameGroup, works.map{ it.nameOfWork }, students.map{
            student -> listOf(student.name,
            grades.filter { it.nameStudent == student.name }.mapIndexed { index: Int, result: Result -> result.count * formula.listOfRules[index]})
        })
    return table
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
    val gradesWithStudents: List<Pair<List<Int>, Student>> =
        grades.zip(students) { grade, student -> grade to student } // здесь мы соединяем студентов с их оценками
    val namesGroup = students.mapTo(HashSet()) { it.nameGroup } // берем имена групп

    // соединяем оценки с видом работы
    val results = gradesWithStudents.map {
        it.first.mapIndexed { index, grade ->
            Result(kindOfWorks[index].nameOfWork, it.second.id, it.second.name, grade)
        }
    }.flatMap { it }

    //filter
    val groups = namesGroup.map { name ->
        val group = students.filter { it.nameGroup == name }
        Group(name, group.map {
            results.filter { result ->
                it.name == result.nameStudent
            }
        }.flatMap { it }) // если есть студент в группе той, то его добавляем в оценки
    }

    groupsMongo.insertMany(groups)
    return students
}


fun inFormula() {
    val formula = Formula("test", listOf(1, 1, 2))
    folmulaMongo.insertOne(formula)
}

// Коллекци в Mongo
val studentsMongo = mongoDatabase.getCollection<Student>().apply { drop() }
val groupsMongo = mongoDatabase.getCollection<Group>().apply { drop() }
val worksMongo = mongoDatabase.getCollection<KindOfWorks>().apply { drop() }
val folmulaMongo = mongoDatabase.getCollection<Formula>().apply { drop() }

fun main() {
    val kindWorks = inWorks()
    val students = inStudents(kindWorks)
    inFormula()
    println(
        "Добрый день! Вы попали в программу выставления рейтинга. " +
                "Вы хотите использовать уже имеющиюся формулу или ввести свою? Если свою, то нажмите 0, если уже имеются, то 1"
    )
    var num: Int? = null
    try {
        do {
            num = readLine()?.toInt()
        } while (num != 1 && num != 0)
    } catch (_: Exception) {
        println("К сожалению, вы нарушили правила нашего приложения и мы вынуждены вас забанить.")
    }

    if (num == 1) {
        for (formula in folmulaMongo.find().toList()) {
            println("Название " + formula.nameFormula + ", Коэфф " + formula.listOfRules)
        }
        println("Напишите название формулы, которую вы хотите использовать")
        var nameOfFormula = ""
        do {
            do {
                nameOfFormula = readLine().toString()
            } while (nameOfFormula.isEmpty()) //пока пользователь ничего не ввел
        } while (folmulaMongo.find(Formula::nameFormula eq nameOfFormula).toList().isEmpty()) // пока неверное название

        val formula = folmulaMongo.find(Formula::nameFormula eq nameOfFormula).toList().first()
        //создаем таблицу
        val table = createTable(formula, students.filter { it.nameGroup == "20z" },
            kindWorks,
            groupsMongo.find(Group::nameGroup eq "20z").toList().flatMap { it.grades }
        )
        printInHtml(table)
    } else {
        try {
            println("Введите название и индексы: \n")
            val formula = readLine()
            val nameOfFormula = formula?.split(' ')?.first().toString()
            val indexes = formula?.split(' ')?.drop(1)!!.map { it.toInt() }

            if (indexes.filter { it != 0 }.size < 3) {
                println("Необходимо писать 3 цифры и не нулевые")
                exitProcess(1)
            }
            // чтобы пользователь не мог ввести одинаковые названия формул
            folmulaMongo.insertOne(
                Formula(
                    nameOfFormula,
                    indexes
                )
            )
        } catch (e: Exception) {
            println("Необходимо писать ЦИФРЫ")
        }
    }
}