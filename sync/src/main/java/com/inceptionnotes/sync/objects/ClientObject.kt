package com.inceptionnotes.sync.objects

import com.arangodb.entity.DocumentField

class ClientObject {
    @DocumentField(DocumentField.Type.ID)
    var id: String? = null

    var kind: String? = null
    var person: String? = null
    var token: String? = null
    var view: String? = null
}