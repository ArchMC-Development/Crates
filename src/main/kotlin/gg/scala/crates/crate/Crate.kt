package gg.scala.crates.crate

import gg.scala.crates.crate.prize.CratePrize
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
class Crate(
    override val identifier: UUID,
    val prizes: List<CratePrize>
) : IDataStoreObject
{

}
