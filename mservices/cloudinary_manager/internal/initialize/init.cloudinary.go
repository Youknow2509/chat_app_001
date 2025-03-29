package initialize

import (
	"context"
	"log"

	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/cloudinary/cloudinary-go/v2"
)

// initialize for cloudinary
func InitCloudinary(){
	// Add your Cloudinary product environment credentials.
	cld, err := cloudinary.NewFromParams(
		global.Config.CloudinarySetting.CloudName, 
		global.Config.CloudinarySetting.APIKey, 
		global.Config.CloudinarySetting.APISecret,
	)
	if err != nil {
		log.Println("Error initializing Cloudinary:", err)
	}
	// test connection to cloudinary
	pres, err := cld.Admin.Ping(context.Background())
	if err != nil {
		log.Println("Error pinging Cloudinary:", err)
		panic(err)
	}
	if pres.Status != "ok" {
		log.Println("Cloudinary is not ok")
		panic("Cloudinary is not ok")
	}
	log.Println("Cloudinary is ok")
	// set cloudinary to global
	global.Cloudinary = cld
	log.Println("Cloudinary initialized successfully")
}
