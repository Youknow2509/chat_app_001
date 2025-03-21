package com.example.chatapp.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

import com.example.chatapp.models.sqlite.Contact;

@Dao
public interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Contact contact);

    @Update
    void update(Contact contact);

    @Delete
    void delete(Contact contact);

    @Query("SELECT * FROM contacts WHERE contactId = :contactId")
    Contact getContactById(long contactId);

    @Query("SELECT * FROM contacts WHERE userId = :userId")
    List<Contact> getContactsByUser(String userId);

    @Query("SELECT * FROM contacts WHERE userId = :userId AND contactUserId = :contactUserId")
    Contact getContactRelationship(String userId, String contactUserId);

    @Query("SELECT * FROM contacts WHERE userId = :userId AND relationshipStatus = :status")
    List<Contact> getContactsByStatus(String userId, String status);

    @Query("SELECT * FROM contacts WHERE userId = :userId AND isFavorite = 1")
    List<Contact> getFavoriteContacts(String userId);

    @Query("UPDATE contacts SET relationshipStatus = :status WHERE userId = :userId AND contactUserId = :contactUserId")
    void updateRelationshipStatus(String userId, String contactUserId, String status);

    @Query("UPDATE contacts SET isFavorite = :isFavorite WHERE contactId = :contactId")
    void updateFavoriteStatus(long contactId, boolean isFavorite);

    @Query("UPDATE contacts SET contactName = :contactName WHERE contactId = :contactId")
    void updateContactName(long contactId, String contactName);

    @Query("DELETE FROM contacts WHERE userId = :userId AND contactUserId = :contactUserId")
    void removeContact(String userId, String contactUserId);
}
