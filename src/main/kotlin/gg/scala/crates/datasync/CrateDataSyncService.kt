package gg.scala.crates.datasync

import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.service.Service
import net.kyori.adventure.key.Key

@Service
object CrateDataSyncService: DataSyncService<CrateContainer>()
{
    object CrateKeys : DataSyncKeys
    {
        override fun newStore() = "crate-sync"

        override fun store() = Key.key("crates", "store")
        override fun sync() = Key.key("crates", "sync")
    }

    override fun locatedIn() = DataSyncSource.Mongo

    override fun keys() = CrateKeys
    override fun type() = CrateContainer::class.java
}