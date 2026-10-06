package hu.mostoha.mobile.android.huki.interactor.exception

import hu.mostoha.mobile.android.huki.R
import hu.mostoha.mobile.android.huki.model.ui.toMessage

data class RoutePlannerLimitReachedException(
    val throwable: Throwable? = null
) : DomainException(R.string.route_planner_error_daily_limit_reached.toMessage(), throwable)
