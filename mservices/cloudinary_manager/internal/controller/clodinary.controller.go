package controller

import (
	"encoding/json"
	"log"
	"net/http"
	"strconv"
	"time"

	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/Youknow2509/cloudinary_manager/internal/model"
	"github.com/Youknow2509/cloudinary_manager/internal/utils"
	"github.com/redis/go-redis/v9"

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

func (c *CloudinaryController) GetSignature(gc *gin.Context) {
    // get body config url with type string
    var bodyConfigURL string
    if err := gc.ShouldBindJSON(&bodyConfigURL); err != nil {
        gc.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
        return
    }
    log.Println("bodyConfigURL: ", bodyConfigURL)

    // check cache sign in redis
    cacheKey := "signature:" + bodyConfigURL
    cachedSign, err := global.RedisClient.Get(gc.Request.Context(), cacheKey).Result()
    switch {
    case err == redis.Nil:
        // cache miss
        log.Println("cache miss")
        break
    case err != nil:
        // some other error
        log.Println("Error getting cache:", err)
        gc.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
        return
    }
    if cachedSign != "" {
        log.Println("cache hit")
        log.Println("cachedSign: ", cachedSign)
        // unmarshal cachedSign to object response
        var cacheData *model.SignatureResponse
        err = json.Unmarshal([]byte(cachedSign), &cacheData)
        if err != nil {
            log.Println("Error unmarshalling cachedSign:", err)
            gc.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to unmarshal cachedSign"})
            return
        }

        log.Println("cacheData: ", cacheData)

        // make response
        respon := model.ResponseData{
            Code:    http.StatusOK,
            Message: "Get signature success",
            Data:    cacheData,
        }
		log.Println("respon: ", respon)
        gc.JSON(respon.Code, respon)
    } else {
        log.Println("cache miss")
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

        // create object response with all required fields (including UploadPreset if necessary)
        objResponse := model.SignatureResponse{
            Signature: sign,
            Timestamp: params["timestamp"],
            APIKey:    global.Config.CloudinarySetting.APIKey,
            CloudName: global.Config.CloudinarySetting.CloudName,
            // UploadPreset: global.Config.CloudinarySetting.UploadPreset, // Ensure UploadPreset is included
        }

        // save object response to redis
        cacheData, err := json.Marshal(objResponse)
        if err != nil {
            log.Println("Error marshalling data:", err)
            gc.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to marshal data"})
            return
        }
        timeEx := time.Minute * 50
        err = global.RedisClient.Set(gc.Request.Context(), cacheKey, cacheData, timeEx).Err()
        if err != nil {
            log.Println("Error setting cache:", err)
            gc.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to set cache"})
            return
        }

        log.Println("signature: ", sign)
        log.Println("timestamp: ", params["timestamp"])

        // make response
        respon := model.ResponseData{
            Code:    http.StatusOK,
            Message: "Get signature success",
            Data:    objResponse,
        }
		log.Println("respon: ", respon)
        gc.JSON(http.StatusOK, respon)
    }
}
