package collection

import basicClasses.SpaceMarine
import exceptions.SpaceMarineIdAlreadyExists
import java.util.Date
import java.util.TreeSet
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Predicate

/**
 * A [TreeSet] collection of [SpaceMarine]
 * Implements methods used in commands
 * @property date Saves creation date
 */
class CollectionManager {
    private val collection = TreeSet<SpaceMarine>()
    private val relationship = mutableMapOf<Long, String>()

    private val date: Date = Date()
    private val lock = ReentrantLock()

    fun getCollection(): TreeSet<SpaceMarine> {
        return collection
    }

    fun add(element: SpaceMarine, username: String) {
        if (element == this.getByID(element.getId())) throw SpaceMarineIdAlreadyExists("Space Marine" +
                "$element cannot be added to collection as a Space Marine with this id already exists")
        lock.lock()
        try {
            collection.add(element)
            relationship[element.getId()] = username
        } finally {
            lock.unlock()
        }
    }

    /**
     * Creates a formatted string with info about collection
     * @return Formatted string with size and [date] values
     */
    fun getInfo() : String {
        return "Tree Set of SpaceMarine: size=${collection.size}, date=${date}"
    }

    /**
     * Prints all elements of the collection
     */
    fun show(): MutableList<String> {
        return if (collection.isEmpty()) {
            mutableListOf("Collection is empty")
        } else {
            val output = mutableListOf<String>()
            lock.lock()
            try {
                for (spaceMarine in collection) {
                    output.add(spaceMarine.toString())
                }
            } finally {
                lock.unlock()
            }
            output
        }
    }

    /**
     * Updates values of an element
     */
    fun update(data: SpaceMarine, spaceMarine: SpaceMarine, username: String) {
        if (relationship[spaceMarine.getId()] != username) throw Exception("You don't have permission to update this element")

        lock.lock()
        try {
            spaceMarine.setName(data.getName())
            spaceMarine.setCoordinates(data.getCoordinates())
            spaceMarine.setCategory(data.getCategory())
            spaceMarine.setChapter(data.getChapter())
            spaceMarine.setHealth(data.getHealth())
            spaceMarine.setLoyalty(data.getLoyalty())
            spaceMarine.setMeleeWeapon(data.getWeapon())
        } finally {
            lock.unlock()
        }
    }

    /**
     * Removes element
     * @param spaceMarine element in the collection
     */
    fun remove(spaceMarine: SpaceMarine, username: String) : Boolean {
        if (relationship[spaceMarine.getId()] != username) throw Exception("You don't have permission to remove this element")

        lock.lock()
        try {
            return collection.remove(spaceMarine)
        } finally {
            lock.unlock()
        }
    }

    fun clear(username: String) {
        for (spaceMarine in collection) {
            if (relationship[spaceMarine.getId()] == username) {
                collection.remove(spaceMarine)
            }
        }
    }

    /**
     * Searches for element with provided [id]
     * @param id id of the element to search
     * @return Found element or null
     */
    fun getByID(id: Long) : SpaceMarine? {
        lock.lock()
        try {
            for (spaceMarine in collection) {
                if (spaceMarine.getId() == id) {
                    return spaceMarine
                }
            }
        } finally {
            lock.unlock()
        }
        return null
    }

    fun filter(predicate: Predicate<SpaceMarine>): List<SpaceMarine> {
        lock.lock()
        try {
            return collection.filter { e -> predicate.test(e) }
        } finally {
            lock.unlock()
        }
    }
}
