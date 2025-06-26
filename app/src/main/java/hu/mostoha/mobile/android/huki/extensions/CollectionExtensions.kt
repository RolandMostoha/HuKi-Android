package hu.mostoha.mobile.android.huki.extensions

import java.util.Collections

fun <T> List<T>.update(item: T, update: (T) -> T): List<T> {
    return map { actualItem ->
        if (item == actualItem) {
            update.invoke(item)
        } else {
            actualItem
        }
    }
}

fun <T> List<T>.swap(fromItem: T, toItem: T): List<T> {
    val fromIndex = this.indexOf(fromItem)
    val toIndex = this.indexOf(toItem)
    val mutableList = this.toMutableList()

    Collections.swap(mutableList, fromIndex, toIndex)

    return mutableList.toList()
}

fun <T> List<T>.second(): T {
    if (isEmpty() || size != 2) {
        throw NoSuchElementException("List size must be 2, actual=$size.")
    }
    return this[1]
}
