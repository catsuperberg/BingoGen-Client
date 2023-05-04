package dev.catsuperberg.bingogen.client.common

class Grid<T>(val rows: List<List<T>>) {
    init {
        require(rows.map { it.size }.toSet().let { it.size == 1 && it.first() == rows.size }) {
            throw IllegalArgumentException("Grid only accepts square list of lists")
        }
    }
    val sideCount: Int
        get() = rows.size
    val count: Int
        get() = rows.size * rows.size
    val columns: List<List<T>>
        get() = List(rows.size) { column -> List(rows.size) { row -> rows[row][column] } }

    fun column(index: Int): List<T> = List(rows.size) { row -> rows[row][index] }
    fun row(index: Int): List<T> = rows[index]

    override fun toString(): String = rows.toString()
    override fun equals(other: Any?) = this === other || other is Grid<*> && rows == other.rows
}
