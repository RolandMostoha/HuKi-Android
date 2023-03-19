package hu.mostoha.mobile.android.huki.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CollectionExtensionsTest {

    @Test
    fun `Given valid index, when updateAtIndex, then item is updated`() {
        val list = listOf("A", "B", "C")

        val result = list.update("B") { it + "B" }

        assertThat(result).isEqualTo(listOf("A", "BB", "C"))
    }

    @Test
    fun `Given invalid index, when updateAtIndex, then original list is returned`() {
        val list = listOf("A", "B", "C")

        val result = list.update("D") { it + "B" }

        assertThat(result).isEqualTo(listOf("A", "B", "C"))
    }

    @Test
    fun `Given form and to items, when swap, then items are swapped`() {
        val list = listOf("A", "B", "C")

        val result = list.swap("A", "C")

        assertThat(result).isEqualTo(listOf("C", "B", "A"))
    }

}
