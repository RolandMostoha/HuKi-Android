package hu.mostoha.mobile.android.huki.interactor

sealed class TaskResult<out R> {

    data class Success<out T>(val data: T) : TaskResult<T>()

    data class Error(val domainException: DomainException) : TaskResult<Nothing>()

}