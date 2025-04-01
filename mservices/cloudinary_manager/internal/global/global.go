package global

import (
	"github.com/Youknow2509/cloudinary_manager/pkg/setting"
	"github.com/cloudinary/cloudinary-go/v2"
	"go.mongodb.org/mongo-driver/mongo"
	"github.com/redis/go-redis/v9"
)

var (
	Config      *setting.Config
	MongoDB     *mongo.Client
	Cloudinary  *cloudinary.Cloudinary
	RedisClient *redis.Client
)
