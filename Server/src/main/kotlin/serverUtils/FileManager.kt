package serverUtils

import basicClasses.SpaceMarine
import collection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import users.UserManager
import utils.JsonCreator

/**
 * Class that contains environment variables and handles files
 */
class FileManager(
    private val dbManager: DBManager
) {
    private val jsonCreator = JsonCreator()
    private val logger: Logger = LogManager.getLogger(FileManager::class.java)

    /**
     * Reads data from database and adds objects to [collection]
     * @param collectionManager Current collection
     */
    fun load(collectionManager: CollectionManager, userManager: UserManager) {
        try {
            logger.info("Loading from database")
            val collection = dbManager.loadCollection()

            for (element in collection) {
                val spaceMarine = jsonCreator.stringToObject<SpaceMarine>(element)
                val parent = dbManager.getRelationship(spaceMarine.getId().toString())
                collectionManager.add(spaceMarine, parent)
                logger.info("Loaded $spaceMarine")
            }

            logger.info("Loaded ${collectionManager.getCollection().size} elements successfully")

        } catch (e: Exception) {
            logger.warn(e.message.toString())
        }
    }

    fun save(collectionManager: CollectionManager, userManager: UserManager) {
        try {
            logger.info("Saving to database")

            for (element in collectionManager.getCollection()) {
                dbManager.saveSpacemarine(element)
                logger.info("Saved $element")
            }
            for (element in collectionManager.getRelationship()) {
                dbManager.saveRelationship(element)
                logger.info("Saved $element")
            }
            for (element in userManager.getUsersMap()) {
                dbManager.saveTokens(element)
                logger.info("Saved $element")
            }
            logger.info("Saved ${collectionManager.getCollection().size} elements successfully")

        } catch (e: Exception) {
            logger.warn(e)
        }
    }

}