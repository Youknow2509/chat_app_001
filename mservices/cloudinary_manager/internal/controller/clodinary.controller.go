package controller

import (
	"log"
	"net/http"
	"strconv"
	"time"

	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/Youknow2509/cloudinary_manager/internal/model"
	"github.com/Youknow2509/cloudinary_manager/internal/utils"

	// "github.com/Youknow2509/cloudinary_manager/internal/utils"
	utilsCloudinary "github.com/Youknow2509/cloudinary_manager/internal/utils/cloudinary"
	"github.com/gin-gonic/gin"
)

type CloudinaryController struct {
}

var vCloudinaryController = new(CloudinaryController)

func GetCloudinaryController() *CloudinaryController {
	if vCloudinaryController == nil {
		log.Println("CloudinaryController is nil, initializing...")
	}
	return vCloudinaryController
}

// get singed post media
func (c *CloudinaryController) GetSignature(gc *gin.Context) {
	// get body config url with type string
	var bodyConfigURL string 
	if err := gc.ShouldBindJSON(&bodyConfigURL); err != nil {
		gc.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	log.Println("bodyConfigURL: ", bodyConfigURL)

	// create params to sign
	params := make(map[string]string)
	params["timestamp"] = strconv.FormatInt(time.Now().Unix(), 10)	
	// add params from body request
	bodyParams, _ := utils.GetConfigURLToParameters(bodyConfigURL)
	if bodyParams == nil {
		gc.JSON(http.StatusBadRequest, gin.H{"error": "params is nil"})
		return
	}
	// add params to params
	for key, value := range bodyParams {
		params[key] = value
	}
	log.Println("params: ", params)

	// create signature
	sign, err := utilsCloudinary.CreateSignedParams(
		params,
		global.Config.CloudinarySetting.APISecret,
	)
	if err != nil {
		gc.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	// create response
	data := gin.H{
		"signature":  sign,
		"timestamp":  params["timestamp"],
		"api_key":    global.Config.CloudinarySetting.APIKey,
		"cloud_name": global.Config.CloudinarySetting.CloudName,
		// "upload_preset": global.Config.CloudinarySetting.UploadPreset,
	}
	log.Println("signature: ", sign)
	log.Println("timestamp: ", params["timestamp"])

	// make response
	respon := model.ResponseData{
		Code:    http.StatusOK,
		Message: "Get signature success",
		Data:    data,
	}
	gc.JSON(http.StatusOK, respon)
}
