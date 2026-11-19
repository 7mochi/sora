package io.github.nanamochi.sora.shared.error

class ServiceException(val errorCode: ErrorCode) : RuntimeException(errorCode.message) {
    override val message: String
        get() = errorCode.message
}
