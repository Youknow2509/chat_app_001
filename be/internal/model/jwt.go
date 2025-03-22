package model

import "github.com/golang-jwt/jwt"

// jwt input
type JwtInput struct {
	Data   string `json:"data"`
	UserID string `json:"user_id"`
}

// JwtValidateInput
type JwtValidateInput struct {
	Data string `json:"data"`
}

// jwt output
type JwtOutput struct {
	Data string `json:"data"`
}

// jwt valid token output
type JwtValidOutput struct {
	jwt.StandardClaims
}

// jwt refresh token input
type JwtRefreshInput struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
}

// Define PayloadClaim with UserID as a separate field
type PayloadClaim struct {
	jwt.StandardClaims
	UserID string `json:"user_id"`
}
