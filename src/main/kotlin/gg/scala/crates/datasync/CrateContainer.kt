package gg.scala.crates.datasync

import gg.scala.commons.graduation.Progressive
import gg.scala.crates.crate.Crate

data class CrateContainer(
    val crates: MutableMap<String, Crate> = mutableMapOf(
        "testing" to Crate(
            "testing",
            "heya",
            mutableListOf()
        )
    ),
    override var matured: Set<String>? = mutableSetOf()
): Progressive