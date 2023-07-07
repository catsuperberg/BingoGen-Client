package dev.catsuperberg.bingogen.client.common

import kotlin.math.sqrt

class Grid<T>(private val data: List<T>) : List<T> by data, AbstractList<T>() {
    companion object {
        fun <T> fromRows(rows: List<List<T>>): Grid<T> = Grid(rows.flatten())
        fun <T> List<T>.toGrid() = Grid(this)
    }

    var sideCount: Int = run {
        val count = sqrt(this.size.toDouble())
        require(count.toInt().toDouble() == count) {
            throw IllegalArgumentException("Provided sequence length doesn't allow for square grid")
        }
        count.toInt()
    }
    val rows: List<List<T>> by lazy { this.chunked(sideCount) }
    val columns: List<List<T>> by lazy {
        List(sideCount) { position -> List(sideCount) { offset -> this[offset * sideCount + position] } }
    }

    fun column(index: Int): List<T> = List(rows.size) { row -> rows[row][index] }
    fun row(index: Int): List<T> = rows[index]


    override fun toString(): String = rows.toString()
}
