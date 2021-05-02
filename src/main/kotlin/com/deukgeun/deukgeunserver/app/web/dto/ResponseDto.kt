package com.deukgeun.deukgeunserver.app.web.dto

class ResponseDto<T> (
    val data: T,
    val message: String = "success"
) {
    companion object {
        const val EMPTY = ""
    }
}