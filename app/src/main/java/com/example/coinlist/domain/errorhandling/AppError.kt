package com.example.coinlist.domain.errorhandling

import java.lang.Exception

sealed class AppError : Exception() {
    data class NetworkError(override val message: String = "Connection Error") : AppError()
    data class UnknownError(override val message: String = "An error occurred") : AppError()
}