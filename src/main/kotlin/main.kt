import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import java.io.FileWriter

fun printInHtml(){
    val file = FileWriter("C:/oop/newttt/src/main/kotlin/main.html")

    file.appendHTML().html {
        body {
            div {
                h1 {
                    + "Добро пожаловать в рейтинг групп от Марии Фароновой"
                }
                table {
                    h2 { + "Рейтинг группы" }
                    colGroup("Предмет11")
                    tbody {

                        tr{
                            + "Предмет"

                        }
                        tr{
                            td{ + "русский"
                            }
                        }
                        tr{
                            td{ + "английский"}
                        }

                    }
                }
            }
        }
    }
    file.close()
}

fun main(){
    printInHtml()
    println("Добрый день! Вы попали в программу выставления рейтинга. " +
            "Вы хотите использовать уже имеющиюся формулу или ввести свою? Если свою, то нажмите 0, если уже имеюся, то 1")

/*try {
    do {
        val num = readLine()?.toInt()
    }
    while(num != 1 && num != 0)
}catch (_: Exception){
    println("НЕВЕРНО!!!!!!!!!!!! ФУУУУУУУУ")
}*/


}

