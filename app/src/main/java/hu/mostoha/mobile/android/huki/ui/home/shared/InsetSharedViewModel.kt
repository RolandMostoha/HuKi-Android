package hu.mostoha.mobile.android.huki.ui.home.shared

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.model.ui.InsetResult
import hu.mostoha.mobile.android.huki.util.ResultSharedViewModel
import javax.inject.Inject

@HiltViewModel
class InsetSharedViewModel @Inject constructor() : ResultSharedViewModel<InsetResult>()
