package kakkoiichris.gemboozled.util

import kotlin.math.abs

fun manhattanDistance(ra: Int, ca: Int, rb: Int, cb: Int) =
    abs(ra - rb) + abs(ca - cb)