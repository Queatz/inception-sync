package com.inceptionnotes.sync.objects

import com.arangodb.entity.DocumentField

/**
 * Created by jacob on 1/28/18.
 */

class Relationship {

    @DocumentField(DocumentField.Type.KEY)
    var id: String? = null
    @DocumentField(DocumentField.Type.FROM)
    var from: String? = null
    @DocumentField(DocumentField.Type.TO)
    var to: String? = null
    var type: String? = null

    companion object {
        val PERSON = "person"
        val REFERENCE = "reference"
        val ITEM = "item"
    }
}
