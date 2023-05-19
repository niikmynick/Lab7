package clientUtils

import basicClasses.*
import clientUtils.readers.*
import utils.InputManager
import utils.OutputManager

/**
 * This class is a functional wrapper around various readers.
 * It uses these classes to read user inputs and then constructs objects based on those inputs.
 */
class Creator(outputManager: OutputManager, inputManager: InputManager) {
    private val stringReader = StringReader(outputManager, inputManager)
    private val booleanReader = BooleanReader(outputManager, inputManager)
    private val floatReader = FloatReader(outputManager, inputManager)
    private val enumReader = EnumReader(outputManager, inputManager)
    private val longReader = LongReader(outputManager, inputManager)
    private val intReader = IntReader(outputManager, inputManager)
    private val doubleReader = DoubleReader(outputManager, inputManager)

    /**
     * Takes necessary user inputs using readers and constructs a SpaceMarine instance.
     * @return [SpaceMarine] object
     */
    fun createSpaceMarine(): SpaceMarine {
        val name = stringReader.read("Enter name of the new Space Marine: ")
        val coordinates = createCoordinates()
        val health = floatReader.read("Enter health value (\\null for null value): ", true)
        val loyal = booleanReader.read("Enter loyalty [true / false]: ")
        val category = enumReader.read<AstartesCategory>("Enter Astartes category from the list: ", false)!!
        val weapon = enumReader.read<MeleeWeapon>("Enter Weapon category from the list: ", true)
        val chapter = createChapter()

        return SpaceMarine(name, coordinates, health, loyal, category, weapon, chapter)
    }

    /**
     * Creates and returns a new [Chapter] object
     * @return [Chapter] from entered values
     */
    fun createChapter() : Chapter {
        val name:String = stringReader.read("Enter name of the Chapter: ")
        val marinesCount: Long = longReader.read("Enter amount of Space Marines: ", 0, 1000)

        return Chapter(name, marinesCount)
    }

    /**
     * Creates and returns a new [Coordinates] object
     * @return [Coordinates] from entered values
     */
    fun createCoordinates() : Coordinates {
        val x: Double = doubleReader.read("Enter value of X: ")
        val y: Int = intReader.read("Enter value of Y: ")

        return Coordinates(x, y)
    }

}
