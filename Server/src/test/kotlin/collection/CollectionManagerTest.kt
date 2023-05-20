package collection

import basicClasses.SpaceMarine
import serverUtils.DBManager
import users.UserManager

import kotlin.test.Test


class CollectionManagerTest {

    private val dbManager = DBManager("jdbc:postgresql://localhost:5432/studs", "s372819", System.getenv("dbPassword"))

    private val collectionManager = CollectionManager(dbManager)
    private val userManager = UserManager(dbManager)

    @Test
    fun `Adding a test user`() {
        userManager.register("yo", "oy")
    }

    @Test
    fun `Adding SpaceMarines`() {
        collectionManager.add(SpaceMarine(), "yo")
        collectionManager.add(SpaceMarine(), "yo")
        assert(collectionManager.getCollection().size == 2)
    }

    @Test
    fun `Remove with correct user`() {
        val relationships = collectionManager.getRelationship()
        for (r in relationships) {
            if (r.value == "yo") {
                val id = r.key
                val sm = collectionManager.getByID(id)
                if (sm != null) {
                    collectionManager.remove(sm, "yo")
                }
                assert(collectionManager.getByID(id) == null)
                break
            }
        }

    }
    @Test
    fun `Clear with admin`() {
        collectionManager.clear("admin")
        assert(collectionManager.getCollection().isEmpty())
    }


}