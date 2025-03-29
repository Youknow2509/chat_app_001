package utils

import (
	"fmt"
	"strings"
	"time"
)

// get timestamp signature cloudinary
func GetTimestampSignature() string {
	return fmt.Sprintf("%d", time.Now().Unix())
}

// get config url to parameters
func GetConfigURLToParameters(configURL string) (map[string]string, error) {
	params := make(map[string]string)
	// check if configURL is empty
	if configURL == "" {
		return params, nil
	}
	// split configURL by "&"
	configURLSplit := strings.Split(configURL, "&")
	// loop through configURLSplit
	for _, param := range configURLSplit {
		// split param by "="
		paramSplit := strings.Split(param, "=")
		// check if paramSplit has 2 elements
		if len(paramSplit) != 2 {
			return nil, fmt.Errorf("invalid param: %s", param)
		}
		// check if paramSplit[0] is empty
		if paramSplit[0] == "" {
			return nil, fmt.Errorf("invalid param: %s", param)
		}
		// check if paramSplit[1] is empty
		if paramSplit[1] == "" {
			return nil, fmt.Errorf("invalid param: %s", param)
		}
		// add paramSplit to params
		params[paramSplit[0]] = paramSplit[1]
	}
	return params, nil
}