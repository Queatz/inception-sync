package com.inceptionnotes.sync.util

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by jacob on 2/14/18.
 */

class InceptionNotesSync : HttpServlet() {
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.writer.write("hey")
        resp.writer.close()
    }
}
