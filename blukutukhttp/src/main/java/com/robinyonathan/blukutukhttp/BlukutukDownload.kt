package com.robinyonathan.blukutukhttp

interface BlukutukDownload {
    fun failed(errorCode: Int, errorMessage: String)

    fun success()
}
