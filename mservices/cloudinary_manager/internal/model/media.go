package model

import "go.mongodb.org/mongo-driver/bson/primitive"

const (
	MEDIA_TYPE_IMAGE     = "image"
	MEDIA_TYPE_VIDEO     = "video"
	MEDIA_TYPE_THUMBNAIL = "thumbnail"
)

const (
	MEDIA_DATABASE = "cloudinary"
	MEDIA_COLLECTION = "media"
	MEDIA_COLLECTION_INDEX = "media_index"
	MEDIA_COLLECTION_INDEX_UNIQUE = "media_index_unique"
)

// schema media go
type Media struct {
	ID        primitive.ObjectID `bson:"_id" json:"id"`
	PublicID  string             `bson:"public_id" json:"public_id"`
	MediaType string             `bson:"media_type" json:"media_type"`
	MediaURL  string             `bson:"media_url" json:"media_url"`
	Deleted   bool               `bson:"deleted" json:"deleted"`
	OwnerID   string             `bson:"owner_id" json:"owner_id"`
	CreatedAt primitive.DateTime `bson:"created_at" json:"created_at"`
	UpdatedAt primitive.DateTime `bson:"updated_at" json:"updated_at"`
	DeletedAt primitive.DateTime `bson:"deleted_at" json:"deleted_at"`
}

// Input media
type MediaInput struct {
	PublicID  string `bson:"public_id" json:"public_id"`
	MediaType string `bson:"media_type" json:"media_type"`
	MediaURL  string `bson:"media_url" json:"media_url"`
	Deleted   bool   `bson:"deleted" json:"deleted"`
	OwnerID   string `bson:"owner_id" json:"owner_id"`
}

// validate media input
func (m *MediaInput) Validate() error {
	// TODO: handle invalid input
	return nil
}