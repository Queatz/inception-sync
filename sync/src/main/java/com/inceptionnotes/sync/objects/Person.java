package com.inceptionnotes.sync.objects;

import com.arangodb.entity.DocumentField;

/**
 * Created by jacob on 1/28/18.
 */

public class Person {

    @DocumentField(DocumentField.Type.KEY)
    private String id;
    private String token;
    private String vlllageId;
    private String firstName;
    private String lastName;
    private String googleUrl;
    private String imageUrl;

    public String getId() {
        return id;
    }

    public Person setId(String id) {
        this.id = id;
        return this;
    }

    public String getToken() {
        return token;
    }

    public Person setToken(String token) {
        this.token = token;
        return this;
    }

    public String getVlllageId() {
        return vlllageId;
    }

    public Person setVlllageId(String vlllageId) {
        this.vlllageId = vlllageId;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Person setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Person setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getGoogleUrl() {
        return googleUrl;
    }

    public Person setGoogleUrl(String googleUrl) {
        this.googleUrl = googleUrl;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Person setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }
}
