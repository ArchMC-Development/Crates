package gg.scala.crates.datasync

import gg.scala.commons.graduation.Schoolmaster
import gg.scala.commons.persist.datasync.DataSyncKeys
import gg.scala.commons.persist.datasync.DataSyncService
import gg.scala.commons.persist.datasync.DataSyncSource
import gg.scala.flavor.service.Service
import net.evilblock.cubed.util.CC
import net.kyori.adventure.key.Key

@Service
object CrateDataSyncService : DataSyncService<CrateContainer>()
{
    object CrateKeys : DataSyncKeys
    {
        override fun newStore() = "crate-sync"

        override fun store() = Key.key("crates", "store")
        override fun sync() = Key.key("crates", "sync")
    }

    val schoolmaster = Schoolmaster<CrateContainer>().apply {
        stage("add-crate-physicalblocks") {
            crates.values.forEach {
                it.physicalLocation = null
            }
        }

        stage("add-crate-holograms") {
            crates.values.forEach {
                it.isHologramEnabled = false
                it.hologramLines = mutableListOf(
                    it.displayName,
                    "",
                    "${CC.WHITE}Left-Click to preview crate",
                    "${CC.WHITE}Right-Click to open crate",
                )
            }
        }

        stage("add-meta-details") {
            crates.values.forEach {

            }
        }
    }

    override fun postReload()
    {
        with(cached()) {
            if (schoolmaster.mature(this))
            {
                sync(this)
            }
        }
    }

    override fun locatedIn() = DataSyncSource.Mongo

    override fun keys() = CrateKeys
    override fun type() = CrateContainer::class.java
}