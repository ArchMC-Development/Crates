package gg.scala.crates.player

import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Service
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
@Service
object CratesPlayerService : ProfileOrchestrator<CratesPlayer>()
{
    override fun new(uniqueId: UUID) = CratesPlayer(uniqueId)
    override fun type() = CratesPlayer::class

    fun getOrFetch(uniqueId: UUID): CompletableFuture<CratesPlayer?> =
        if (find(uniqueId) != null) CompletableFuture.completedFuture(find(uniqueId)!!) else findInMongo(uniqueId)

    private fun findInMongo(uniqueId: UUID): CompletableFuture<CratesPlayer?> = DataStoreObjectControllerCache
        .findNotNull<CratesPlayer>()
        .useLayerWithReturn<MongoDataStoreStorageLayer<CratesPlayer>, CompletableFuture<CratesPlayer?>>(
            DataStoreStorageType.MONGO
        ) {
            this.load(uniqueId)
        }
}
