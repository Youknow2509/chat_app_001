package initialize

import (
	"context"
	"fmt"
	"log"
	"strconv"

	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/Youknow2509/cloudinary_manager/internal/model"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

func InitDBMongo() {
	// initialize mongo connection
	initMongoConnection()
	// create database and collections
	createMongoCollections()
	// initialize mongo indexes
	initMongoIndexes()
	// initialize mongo migration
	initMongoMigration()

	log.Println("MongoDB initialized successfully")
}

// init mongo connection
func initMongoConnection() {
	strConnection := fmt.Sprintf("mongodb://%s:%s@%s:%s/%s?authSource=admin",
		global.Config.Mongo.UserName,
		global.Config.Mongo.Password,
		global.Config.Mongo.Host,
		strconv.Itoa(global.Config.Mongo.Port),
		global.Config.Mongo.Collection,
	)
	log.Println("MongoDB connection string: ", strConnection)
	// establish connection with mongodb
	c, err := mongo.Connect(context.Background(), options.Client().ApplyURI(strConnection))
	if err != nil {
		panic(err)
	}
	// check the connection
	err = c.Ping(context.Background(), nil)
	if err != nil {
		log.Println("Error connecting to MongoDB", err)
		panic(err)
	}
	// set the connection to global
	global.MongoDB = c
}

// create mongo collections
func createMongoCollections() {
	// create collections product in mongodb
	err := global.MongoDB.Database(model.MEDIA_DATABASE).CreateCollection(context.Background(), model.MEDIA_COLLECTION)
	if err != nil {
		// Handle error
		log.Println("Error creating collection", err)
        panic(err)
	}

	// ...
}

// initialize mongo indexes
func initMongoIndexes() {
	collection := global.MongoDB.Database(model.MEDIA_DATABASE).Collection(model.MEDIA_COLLECTION)
	// create index for public_id
	indexPublicID := mongo.IndexModel{
		Keys:    map[string]interface{}{"public_id": 1},
		Options: options.Index().SetUnique(true),
	}
	// create index for media_url - help serch
	indexMediaURL := mongo.IndexModel{
		Keys:    map[string]interface{}{"media_url": 1},
		Options: options.Index().SetUnique(false),
	}
	// create index for owner_id - help serch
	indexOwnerID := mongo.IndexModel{
		Keys:    map[string]interface{}{"owner_id": 1},
		Options: options.Index().SetUnique(false),
	}
	// list index
	listIndex := []mongo.IndexModel{
		indexPublicID,
		indexMediaURL,
		indexOwnerID,
	}
	_, err := collection.Indexes().CreateMany(context.Background(), listIndex)
	if err != nil {
		// Handle error
        log.Println("Error creating index", err)
        panic(err)
	}
}

// initialize mongo migration
func initMongoMigration() {
	// initialize mongo migration

}
