package com.inceptionnotes.sync.util

import com.inceptionnotes.sync.ws.WebsocketServer
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import java.util.stream.Collectors
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by jacob on 2/14/18.
 */


class InceptionNotesSync : HttpServlet() {

    private val on = WebsocketServer.on

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.writer.write("hey")
        resp.writer.close()
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        addMainHeaders(resp)

        if (req.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            resp.status = HttpStatus.SC_UNAUTHORIZED
            resp.writer.close()
            return
        }

        val data = req.reader.lines().collect(Collectors.joining(System.lineSeparator()))

        val response = on<HttpTransport>().onHttpMessage(
                req.getHeader(HttpHeaders.AUTHORIZATION),
                data)

        resp.writer.write(response ?: "[]")
        resp.writer.close()
    }

    override fun doOptions(req: HttpServletRequest, resp: HttpServletResponse) {
        addMainHeaders(resp)
        resp.writer.close()
    }

    private fun addMainHeaders(resp: HttpServletResponse) {
        resp.addHeader("Content-Type", "application/json;charset=utf-8")
        resp.addHeader("Access-Control-Allow-Origin", "*")
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        resp.addHeader("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept")
    }
}
