package consts

// redis
const (
	ENV_REDIS_HOST      = "REDIS_HOST"
	ENV_REDIS_PORT      = "REDIS_PORT"
	ENV_REDIS_PASSWORD  = "REDIS_PASSWORD"
	ENV_REDIS_DB        = "REDIS_DB"
	ENV_REDIS_POOL_SIZE = "REDIS_POOL_SIZE"
)

// jwt
const (
	ENV_JWT_SECRET          = "JWT_SECRET"
	ENV_JWT_EXPIRED         = "JWT_EXPIRED"
	ENV_JWT_REFRESH_EXPIRED = "JWT_REFRESH_EXPIRED"
	ENV_JWT_ISSUER          = "JWT_ISSUER"
	ENV_JWT_AUDIENCE        = "JWT_AUDIENCE"
)

// mongo
const (
	ENV_MONGO_USERNAME   = "MONGO_USERNAME"
	ENV_MONGO_PASSWORD   = "MONGO_PASSWORD"
	ENV_MONGO_HOST       = "MONGO_HOST"
	ENV_MONGO_PORT       = "MONGO_PORT"
	ENV_MONGO_COLLECTION = "MONGO_COLLECTION"
)

// cloudinary
const (
	ENV_CLOUDINARY_CLOUD_NAME     = "CLOUDINARY_CLOUD_NAME"
	ENV_CLOUDINARY_API_KEY        = "CLOUDINARY_API_KEY"
	ENV_CLOUDINARY_API_SECRET     = "CLOUDINARY_API_SECRET"
	ENV_CLOUDINARY_UPLOAD_PRESETS = "CLOUDINARY_UPLOAD_PRESETS"
)
