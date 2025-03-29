package model

import "github.com/golang-jwt/jwt"

// Define PayloadClaim with UserID as a separate field
type PayloadClaim struct {
	jwt.StandardClaims
	UserID string `json:"user_id"`
}
