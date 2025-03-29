package global

import (
	"github.com/Youknow2509/cloudinary_manager/pkg/setting"
	"go.mongodb.org/mongo-driver/mongo"
	"github.com/cloudinary/cloudinary-go/v2"
)

var (
	Config     *setting.Config
	MongoDB    *mongo.Client
	Cloudinary *cloudinary.Cloudinary
)
