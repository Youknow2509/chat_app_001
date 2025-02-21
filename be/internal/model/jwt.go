package model

import "github.com/golang-jwt/jwt"

// jwt input
type JwtInput struct {
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

