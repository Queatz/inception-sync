package com.inceptionnotes.sync.objects

import com.arangodb.entity.DocumentField
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.util.*

/**
 * Created by jacob on 1/20/18.
 */

class Note {
    @DocumentField(DocumentField.Type.KEY)
    var id: String? = null
    var version: String? = null
    var created: Date? = null
    var updated: Date? = null

    var name: String? = null
    var description: String? = null
    var color: String? = null
    var items: List<String>? = null
    var ref: List<String>? = null
    var people: List<String>? = null
    var backgroundUrl: String? = null
    var collapsed: Boolean? = null
    var checked: Boolean? = null
    var estimate: Float? = null

    var sync: MutableList<String>? = null

    fun toSyncNote(): Note {
        val result = Note()
        result.id = id
        result.version = version
        result.sync = ArrayList()

        if (name != null) result.sync!!.add("name")
        if (description != null) result.sync!!.add("description")
        if (color != null) result.sync!!.add("color")
        if (items != null) result.sync!!.add("items")
        if (ref != null) result.sync!!.add("ref")
        if (people != null) result.sync!!.add("people")
        if (backgroundUrl != null) result.sync!!.add("backgroundUrl")
        if (collapsed != null) result.sync!!.add("collapsed")
        if (checked != null) result.sync!!.add("checked")
        if (estimate != null) result.sync!!.add("estimate")

        return result
    }

    fun setProp(type: String, value: JsonElement) {
        when (type) {
            "name" -> name = value.asString
            "description" -> description = value.asString
            "color" -> color = value.asString
            "items" -> items = aToL(value.asJsonArray)
            "ref" -> ref = aToL(value.asJsonArray)
            "people" -> people = aToL(value.asJsonArray)
            "backgroundUrl" -> backgroundUrl = value.asString
            "collapsed" -> collapsed = value.asBoolean
            "checked" -> checked = value.asBoolean
            "estimate" -> estimate = value.asFloat
        }
    }

    private fun aToL(jsonArray: JsonArray): List<String> {
        val result = ArrayList<String>()
        jsonArray.forEach { x -> result.add(x.asString) }
        return result
    }
}
