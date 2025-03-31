package cloudinary

import (
	"log"
	"net/url"

	"github.com/cloudinary/cloudinary-go/v2/api"
)

// create singed
func CreateSignedParams(paramsToSign map[string]string, apiSecret string) (string, error) {
	// create params url
	paramsToSignURL := make(url.Values)
	for key, value := range paramsToSign {
		if key == "resource_type" {
			continue
		}
		if key == "max_file_size" {
			continue
		}
		paramsToSignURL.Set(key, value)
	}
	// 
	log.Println("paramsToSignURL: ", paramsToSignURL.Encode())
	// sign params
	resp, err := api.SignParameters(
		paramsToSignURL,
		apiSecret,
	)
	if err != nil {
		return "", err
	}
	return resp, nil
}
