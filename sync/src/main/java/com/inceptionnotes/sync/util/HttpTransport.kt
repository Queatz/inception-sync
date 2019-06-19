package com.inceptionnotes.sync.util

import com.queatz.on.On

typealias MessageTrade = (String) -> String?

class HttpTransport constructor(private val on: On) {

    private val clients = mutableMapOf<String, MessageTrade>()

    fun onHttpMessage(token: String, message: String): String? {
        return if (token in clients)
            clients[token]?.invoke(message)
        else
            null
    }

    fun register(token: String, onHttpMessage: MessageTrade) {
        clients[token] = onHttpMessage
    }

    fun unregister(token: String) {
        clients.remove(token)
    }
}