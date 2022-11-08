import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import models.Androide
import models.Muestra
import models.Terminal
import java.io.File
import java.nio.file.Paths

/*
*
* Examen de Star Wars usando corrutinas, voy a proporcionar dos metodos.
*
* Version usando Mutex, que equivale a ReentrantLock
*
* En este caso los modelos (a excepcion de Muestra) no son tan importantes, y podrian guardarse como dos
* listas de String
*/

private const val MAX_PRODUCCION = 5
private const val MAX_MUESTRA = 5

private fun main() = runBlocking {
    // TXT
    val userDir = System.getProperty("user.dir")
    val pathFile = Paths.get(userDir + File.separator + "data").toString()
    val file = File(pathFile + File.separator + "manifiesto.txt")

    // Preparacion - Limpieza TXT
    limpiezaTxt(file)

    // Productor - Consumidor
    producerConsumer(file)

    println("\nMuestras extraidas correctamente.")
}

private suspend fun producerConsumer(f: File) = coroutineScope {
    val mutex = Mutex() // Cerrojo mutex
    // Lista de muestras que generaran los productores y sera consumida por los consumidores
    val muestrasGeneradas = mutableListOf<Muestra>()

    // Lista de Androides/Productores
    val androides = listOf(Androide("R2D2"), Androide("BB8"))

    // Lista de Terminales/Consumidores
    val terminales = listOf(Terminal("Luke"), Terminal("Leia"))

    // Boolean para finalizar la consumicion.
    var aviso = false

    for (a in androides) {
        launch(Dispatchers.Default) {
            for (i in 1..MAX_PRODUCCION) {
                val produccionMuestra = 1500

                delay(produccionMuestra.toLong())
                mutex.withLock {
                    val muestra = Muestra(id = i)
                    muestrasGeneradas.add(muestra)
                    println("Androide: ${a.nombre} | Muestra: $muestra")
                }
            }
        }
    }

    for (t in terminales) {
        launch(Dispatchers.IO) {
            while (!aviso) {
                if (muestrasGeneradas.size == 0) {
                    val tiempoEspera = (2000..2500).random()
                    println("Terminal ${t.nombre} esperando $tiempoEspera ms")
                    delay(tiempoEspera.toLong())
                }
                for (m in 1..MAX_MUESTRA) {
                    val recogidaMuestra = (1000..1500).random()
                    delay(recogidaMuestra.toLong())

                    mutex.withLock {
                        val muestra = muestrasGeneradas.removeFirst()
                        println("\t-Terminal: ${t.nombre} | Muestra: $muestra\n---------------------------------------")
                        if (muestra.porcentajePureza > 60) {
                            f.appendText("Terminal: ${t.nombre} | Muestra: $muestra\n")
                            println("\t\t --Terminal: ${t.nombre} escribio en fichero")
                        }
                    }
                }
                println("\t ---Limite alcanzado | Terminal: ${t.nombre}---")
                aviso = true
            }
        }
    }
}

private suspend fun limpiezaTxt(f: File) = coroutineScope {
    println("Borrando informacion antigua del archivo: $f")
    f.writeText("")
}

