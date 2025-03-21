package com.example.chatapp.models.sqlite;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "contacts",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "userId",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "userId",
                        childColumns = "contactUserId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("userId"), @Index("contactUserId")})
public class Contact {
    @PrimaryKey(autoGenerate = true)
    private long contactId;

    @NonNull
    private String userId;

    @NonNull
    private String contactUserId;

    private String contactName;
    private String relationshipStatus;  // "friend", "pending", "blocked"
    private Date addedOn;
    private String notes;
    private boolean isFavorite;

    // Constructors, Getters and Setters

    public Contact(@NonNull String userId, @NonNull String contactUserId) {
        this.userId = userId;
        this.contactUserId = contactUserId;
        this.relationshipStatus = "pending";
        this.addedOn = new Date();
        this.isFavorite = false;
    }

    // Getters and Setters
    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getContactUserId() {
        return contactUserId;
    }

    public void setContactUserId(@NonNull String contactUserId) {
        this.contactUserId = contactUserId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }

    public Date getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Date addedOn) {
        this.addedOn = addedOn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }
}