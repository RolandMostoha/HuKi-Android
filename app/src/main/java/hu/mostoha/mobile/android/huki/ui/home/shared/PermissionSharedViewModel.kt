package hu.mostoha.mobile.android.huki.ui.home.shared

import dagger.hilt.android.lifecycle.HiltViewModel
import hu.mostoha.mobile.android.huki.model.ui.PermissionResult
import hu.mostoha.mobile.android.huki.util.ResultSharedViewModel
import javax.inject.Inject

@HiltViewModel
class PermissionSharedViewModel @Inject constructor() : ResultSharedViewModel<PermissionResult>()
