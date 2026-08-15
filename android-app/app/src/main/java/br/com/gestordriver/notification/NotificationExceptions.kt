package br.com.gestordriver.notification

open class NotificationError(message: String) : Exception(message)

class UnsupportedPlatform(message: String) : NotificationError(message)

class InvalidNotification(message: String) : NotificationError(message)

class ExtractionError(message: String) : NotificationError(message)
