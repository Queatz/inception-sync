package com.inceptionnotes.sync.objects

import com.arangodb.entity.DocumentField

/**
 * Created by jacob on 1/28/18.
 */

class Person {
    @DocumentField(DocumentField.Type.KEY)
    var id: String? = null
    var token: String? = null
    var vlllageId: String? = null
    var firstName: String? = null
    var lastName: String? = null
    var googleUrl: String? = null
    var imageUrl: String? = null
}
