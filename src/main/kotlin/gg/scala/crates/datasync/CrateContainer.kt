package gg.scala.crates.datasync

import gg.scala.crates.crate.Crate

data class CrateContainer(
    val crates: MutableMap<String, Crate> = mutableMapOf(
        "testing" to Crate(
            "testing",
            "heya",
            mutableListOf()
        )
    )
)