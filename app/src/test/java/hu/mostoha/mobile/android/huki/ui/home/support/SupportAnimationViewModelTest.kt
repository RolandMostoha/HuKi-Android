package hu.mostoha.mobile.android.huki.ui.home.support

import com.google.common.truth.Truth.assertThat
import hu.mostoha.mobile.android.huki.model.domain.SupportAnimation
import org.junit.Test

class SupportAnimationViewModelTest {

    @Test
    fun `given support animations, when refreshing through a full cycle, then each one is shown once`() {
        val viewModel = SupportAnimationViewModel()

        val firstCycle = listOf(viewModel.animation.value) + viewModel.refresh(SupportAnimation.entries.size - 1)

        assertThat(firstCycle).containsExactlyElementsIn(SupportAnimation.entries)
    }

    @Test
    fun `given a finished cycle, when refreshing again, then a new cycle shows each one once`() {
        val viewModel = SupportAnimationViewModel()
        viewModel.refresh(SupportAnimation.entries.size - 1)

        val secondCycle = viewModel.refresh(SupportAnimation.entries.size)

        assertThat(secondCycle).containsExactlyElementsIn(SupportAnimation.entries)
    }

    @Test
    fun `given a finished cycle, when refreshing again, then the next animation is not the current one`() {
        repeat(CYCLE_BOUNDARY_ATTEMPTS) {
            val viewModel = SupportAnimationViewModel()
            val lastOfFirstCycle = viewModel.refresh(SupportAnimation.entries.size - 1).last()

            viewModel.nextAnimation()

            assertThat(viewModel.animation.value).isNotEqualTo(lastOfFirstCycle)
        }
    }

    /**
     * Pulls to refresh [count] times and returns the animation shown after each one.
     */
    private fun SupportAnimationViewModel.refresh(count: Int): List<SupportAnimation> {
        return buildList {
            repeat(count) {
                nextAnimation()

                add(animation.value)
            }
        }
    }

    companion object {
        /**
         * The shuffle is random, so the cycle boundary is retried enough times to catch a
         * regression that only shows up on some shuffles.
         */
        private const val CYCLE_BOUNDARY_ATTEMPTS = 200
    }

}
