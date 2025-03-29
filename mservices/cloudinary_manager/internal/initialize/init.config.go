package initialize

import (
	"log"
	"os"
	"strconv"
	"github.com/joho/godotenv"
	_ "github.com/joho/godotenv/autoload"

	"github.com/Youknow2509/cloudinary_manager/internal/consts"
	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/Youknow2509/cloudinary_manager/pkg/setting"
)

// Initialize config
func InitConfig() {
	// Load environment variables from .env file
	pathFileConfig := consts.PATH_FILE_ENV_DEV
	err := godotenv.Load(pathFileConfig)
	if err != nil {
		log.Println("Error loading .env file:", err)
		panic(err)
	}

	InitConfigMongo()
	InitConfigCloudinary()
	InitConfigJWT()

	log.Println("Config initialized")
}

// init config jwt
func InitConfigJWT() {
	jwtConfig := &setting.JwtSetting{
		JWT_SECRET: os.Getenv(consts.ENV_JWT_SECRET),
		JWT_EXPIRED: os.Getenv(consts.ENV_JWT_EXPIRED),
		JWT_REFRESH_EXPIRED: os.Getenv(consts.ENV_JWT_REFRESH_EXPIRED),
		JWT_ISSUER: os.Getenv(consts.ENV_JWT_ISSUER),
		JWT_AUDIENCE: os.Getenv(consts.ENV_JWT_AUDIENCE),
	}

	global.Config.JwtSetting = jwtConfig
	log.Println("JWT config initialized")
}

// init config cloudinary
func InitConfigCloudinary() {
	cloudinaryConfig := &setting.CloudinarySetting{
		CloudName: os.Getenv(consts.ENV_CLOUDINARY_CLOUD_NAME),
		APIKey:    os.Getenv(consts.ENV_CLOUDINARY_API_KEY),
		APISecret: os.Getenv(consts.ENV_CLOUDINARY_API_SECRET),
	}
	// TODO: rm
	// log.Println("CloudName::", cloudinaryConfig.CloudName)
	// log.Println("APIKey::", cloudinaryConfig.APIKey)
	// log.Println("APISecret::", cloudinaryConfig.APISecret)

	global.Config.CloudinarySetting = cloudinaryConfig
	log.Println("Cloudinary config initialized")
}

// init config mongo
func InitConfigMongo() {
	// Convert MONGO_PORT from string to int
    port, err := strconv.Atoi(os.Getenv(consts.ENV_MONGO_PORT))
    if err != nil {
		log.Println("Error converting MONGO_PORT to int:", err)
		log.Println("Using default port 27017")
		port = 27017 // Default MongoDB port
    }

	mongoConfig := &setting.MongoSetting{
		Host:       os.Getenv(consts.ENV_MONGO_HOST),
		Port:       port,
		UserName:   os.Getenv(consts.ENV_MONGO_USERNAME),
		Password:   os.Getenv(consts.ENV_MONGO_PASSWORD),
		Collection: os.Getenv(consts.ENV_MONGO_COLLECTION),
	}

	global.Config.Mongo = mongoConfig
	log.Println("MongoDB config initialized")
}
