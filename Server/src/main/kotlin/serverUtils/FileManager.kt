package serverUtils

import basicClasses.SpaceMarine
import collection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.JsonCreator

/**
 * Class that contains environment variables and handles files
 */
class FileManager(
    private val dbManager: DBManager
) {

    private val logger: Logger = LogManager.getLogger(FileManager::class.java)
    private val jsonCreator = JsonCreator()

    /**
     * Reads data from database and adds objects to [collection]
     * @param collectionManager Current collection
     */
    fun load(collectionManager: CollectionManager) {
        try {
            logger.info("Loading from database")
            val collection = dbManager.loadCollection()

            for (element in collection) {
                if (element.isNotBlank()) {
                    val spaceMarine = jsonCreator.stringToObject<SpaceMarine>(element)
                    collectionManager.add(spaceMarine)
                    logger.info("Loaded $spaceMarine")
                }
            }

            logger.info("Loaded ${collectionManager.getCollection().size} elements successfully")

        } catch (e: Exception) {
            logger.warn(e.message.toString())
        }
    }

    fun save(collectionManager: CollectionManager) {
        try {
            logger.info("Saving to database")

            for (element in collectionManager.getCollection()) {
                dbManager.save(jsonCreator.objectToString(element))
                logger.info("Saved $element")
            }

            logger.info("Saved ${collectionManager.getCollection().size} elements successfully")

        } catch (e: Exception) {
            logger.warn(e.message.toString())
        }
    }

}