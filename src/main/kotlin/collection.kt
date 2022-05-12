import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Student(
    val name: String,
    val nameGroup: String,
    val id: @Contextual Id<Student> = newId()
)

@Serializable
data class Group(val nameGroup: String,
                 val grades: List<Result> = emptyList()
)

@Serializable
data class KindOfWorks(val nameOfWork: String,
                       val maxCount: Int = 0
)

@Serializable
data class Result(val nameOfWork:String,
                  val idStudent: @Contextual Id<Student>,
                  val nameStudent: String,
                  val count: Int = 0
)

@Serializable
data class Formula(val nameFormula: String,
                   val listOfRules: List<Int>)