import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import java.util.*

@Serializable
data class Student(val name:String,
                   val idGroup:  @Contextual Id<Group>,
                   val id: @Contextual Id<Student> = newId())



@Serializable
data class Group(val nameGroup: String,
                 @Contextual val idGroup: Id<Group> = newId())

@Serializable
data class KindOfWorks(val idWork: @Contextual Id<KindOfWorks> = newId(),
                       val nameOfWork: String,
                       val deadLine: @Contextual Date,
                       val maxCount: Int = 0
)

@Serializable
data class Result(val idOfWork: @Contextual Id<KindOfWorks>,
                  val idStudent: @Contextual Id<Student>,
                  val date: @Contextual Date,
                  val count: Int = 0)
@Serializable
data class Rule(val idRule: @Contextual Id<Rule> = newId(),
                val type: String,
                val idResult: @Contextual Id<Result>,
                val number: Int = 0
)

@Serializable
data class Formula(val idFormula: @Contextual Id<Formula>,
                   val nameFormula: String,
                   val listOfRules: List<Rule>)

