package hu.mostoha.mobile.android.huki.ui.home.gpx.history

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.model.ui.GpxRenameResult
import hu.mostoha.mobile.android.huki.util.ResultSharedViewModel
import javax.inject.Inject

@HiltViewModel
class GpxRenameSharedViewModel @Inject constructor() : ResultSharedViewModel<GpxRenameResult>()
