package hu.mostoha.mobile.android.huki.ui.home.support

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.model.domain.SupportAnimation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SupportAnimationViewModel @Inject constructor() : ViewModel() {

    private val remaining = ArrayDeque<SupportAnimation>()

    private val _animation = MutableStateFlow(nextFromShuffle(null))
    val animation: StateFlow<SupportAnimation> = _animation.asStateFlow()

    fun nextAnimation() {
        _animation.value = nextFromShuffle(_animation.value)
    }

    private fun nextFromShuffle(current: SupportAnimation?): SupportAnimation {
        if (remaining.isEmpty()) {
            val shuffled = SupportAnimation.entries.shuffled().toMutableList()

            if (shuffled.first() == current) {
                shuffled.add(shuffled.removeAt(0))
            }

            remaining.addAll(shuffled)
        }

        return remaining.removeFirst()
    }

}
