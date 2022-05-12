import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import org.litote.kmongo.*
import java.io.FileWriter
import java.util.*
import kotlin.collections.HashSet
import kotlin.system.exitProcess

fun printInHtml(list: Map<String, List<String>>) {
    val file = FileWriter("C:/oop/rating/main.html")
    file.appendHTML().html {
        for (group in list.getOrDefault("Name", listOf(""))) {
            body {
                div {
                    +"Рейтинг группы $group"
                }
                table {
                    thead {
                        tr {
                            for (names in list["Works"]!!) {
                                td {
                                    +names
                                }
                            }
                        }
                    }
                    tbody {
                        val listGrade = list.getOrDefault(group, listOf(""))
                        for (i in (0..listGrade.lastIndex) step 5) {
                            tr {
                                for (j in 0..4) {
                                    td {
                                        +listGrade[i + j]
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    file.close()
}

//создание таблицы, которые мы будем передавать
fun createTable(
    students: List<Student>,
    works: List<KindOfWorks>,
    grades: List<Result>
): Map<String, List<String>> {
    val groups = students.mapTo(HashSet()) { it.nameGroup }.toList() // названия групп
    val colon = listOf("Name") + works.map { it.nameOfWork } + listOf("Result") //название колонок
    //максимальное количество баллов
    val max = works.map { it.maxCount }
        .fold(0) { acc, grade ->
            acc + grade
        }
    // ряды
    val elements = students.map { student ->
        val now = grades.filter { it.nameStudent == student.name }
        listOf(student.name) +
                now.mapIndexed { index, grade -> (grade.count * 100 / works[index].maxCount).toString() } +
                listOf((now.map { result -> result.count }.fold(0) { acc, i -> acc + i } * 100 / max).toString())
    }
    val studentsInGroups = TreeMap<String, List<String>>()
    groups.map { group ->
        studentsInGroups.merge(group, students.filter { it.nameGroup == group }.map { it.name }) { _, new ->
            new
        }
    }
    val gradesWithStudents = TreeMap<String, List<String>>()
    studentsInGroups.mapKeys { group ->
        gradesWithStudents.merge(
            group.key,
            elements.filter { studentsInGroups[group.key]!!.contains(it[0]) }.flatMap { it })
        { old, new -> old + new }
    }

    val table = mapOf(
        "Name" to groups,
        "Works" to colon
    ) + gradesWithStudents

    return table
}

// Загружаем виды работ
fun inWorks(formula: Formula): List<KindOfWorks> {
    val works = listOf("Lab", "Test", "Course Work").mapIndexed { index, name ->
        when (index) {
            0 -> KindOfWorks(name, 5 * formula.listOfRules[index])
            1 -> KindOfWorks(name, 5 * formula.listOfRules[index])
            else -> KindOfWorks(name, 5 * formula.listOfRules[index])
        }
    }
    worksMongo.insertMany(works)
    return works
}

// Загрущаем на Базу студентов вместе с группами
fun inStudents(kindOfWorks: List<KindOfWorks>, formula: Formula): List<Student> {
    // Записываем студентов
    val students = listOf("Maria", "Sasha", "Pasha").mapIndexed { index, name ->
        if (index < 2)
            Student(name, "20z")
        else
            Student(name, "20m")
    }
    studentsMongo.insertMany(students)

    val grades = listOf(listOf(5, 5, 5), listOf(3, 4, 3), listOf(3, 1, 5)).map {
        it.mapIndexed{ index, result ->
            result * formula.listOfRules[index]
        }
    }
        // оценки студентов
    val gradesWithStudents: List<Pair<List<Int>, Student>> =
        grades.zip(students) { grade, student -> grade to student } // здесь мы соединяем студентов с их оценками
    val namesGroup = students.mapTo(HashSet()) { it.nameGroup } // берем имена групп

        // соединяем оценки с видом работы
        val results = gradesWithStudents.map {
            it.first.mapIndexed { index, grade ->
                Result(kindOfWorks[index].nameOfWork, it.second.id, it.second.name, grade)
            }
        }.flatMap { it }

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


fun inTable(formula: Formula) {
    val kindWorks = inWorks(formula)
    val students = inStudents(kindWorks, formula)
    printInHtml(createTable(students, kindWorks, groupsMongo.find().toList().flatMap { it.grades }))
}

// Коллекци в Mongo
val studentsMongo = mongoDatabase.getCollection<Student>().apply { drop() }
val groupsMongo = mongoDatabase.getCollection<Group>().apply { drop() }
val worksMongo = mongoDatabase.getCollection<KindOfWorks>().apply { drop() }
val formulaMongo = mongoDatabase.getCollection<Formula>().apply { }

fun main() {
    println(
        "Добрый день! Вы попали в программу выставления рейтинга.\n " +
                "Вы хотите использовать уже имеющиюся формулу или ввести свою? Если свою, то нажмите 0, если уже имеются, то 1"
    )
    var num: Int?
    try {
        do {
            num = readLine()?.toInt()
        } while (num != 1 && num != 0)
    } catch (_: Exception) {
        println("К сожалению, вы нарушили правила нашего приложения и мы вынуждены вас забанить.")
        exitProcess(1)
    }
    if (num == 1) {
        for (formula in formulaMongo.find().toList()) {
            println("Название " + formula.nameFormula + ", Коэфф " + formula.listOfRules)
        }
        var nameOfFormula: String
        do {
            println("Напишите название формулы, которую вы хотите использовать")
            do {
                nameOfFormula = readLine().toString()
            } while (nameOfFormula.isEmpty()) //пока пользователь ничего не ввел
        } while (formulaMongo.find(Formula::nameFormula eq nameOfFormula).toList().isEmpty()) // пока неверное название
        inTable(formulaMongo.find(Formula::nameFormula eq nameOfFormula).toList().first())
    } else {
        try {
            println("Введите название и индексы: \n")
            val formula = readLine()
            var nameOfFormula = formula?.split(' ')?.first().toString()
            val indexes = formula?.split(' ')?.drop(1)!!.map { it.toInt() }

            if (indexes.filter { it != 0 }.size < 3) {
                println("Необходимо писать 3 цифры и не нулевые")
                exitProcess(1)
            }
            // чтобы пользователь не мог ввести одинаковые названия формул
            while (formulaMongo.find(Formula::nameFormula eq nameOfFormula).toList().isNotEmpty()) {
                println("Вы ввели название, которое уже существует, напишите другое.")
                nameOfFormula = readLine().toString()
            }
            formulaMongo.insertOne(Formula(nameOfFormula, indexes))
            inTable(formulaMongo.find(Formula::nameFormula eq nameOfFormula).toList().first())
        } catch (e: Exception) {
            println("Необходимо писать ЦИФРЫ")
        }
    }
}