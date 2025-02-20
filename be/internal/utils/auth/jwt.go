package auth

import (
	"time"

	"example.com/be/global"
	"github.com/golang-jwt/jwt"
	"github.com/google/uuid"
)

// PayloadClaim struct
type PayloadClaim struct {
	jwt.StandardClaims
}

// create token with uuid
func CreateToken(uuidToken string) (string, error) {
	// set time expiration
	timEx := global.Config.Jwt.JWT_EXPIRATION
	if timEx == "" {
		timEx = "1h"
	}
	// convert to time duration
	expiration, err := time.ParseDuration(timEx)
	if err != nil {
		return "", err
	}

	now := time.Now()
	expirationAt := now.Add(expiration)

	return GenerateToken(&PayloadClaim{
		StandardClaims: jwt.StandardClaims{
			Id:        uuid.New().String(),
			ExpiresAt: expirationAt.Unix(),
			IssuedAt:  now.Unix(),
			Issuer:    "go-ecommerce",
			// TODO: save id user 
			Subject:   uuidToken,
		},
	})
}

// create refresh token
func CreateRefreshToken(uuidToken string, uuidAccessToken string) (string, error) {
	// set time expiration
	timeEx := global.Config.Jwt.JWT_REFRESH_EXPIRED
	if timeEx == "" {
		timeEx = "7"
    }
	// convert to time duration
	expiration, err := time.ParseDuration(timeEx)
	if err != nil {
		return "", err
	}
	expiration = expiration * (24 * time.Hour)
	// get time expiration token
	now := time.Now()
	expirationAt := now.Add(expiration)

	return GenerateToken(&PayloadClaim{
		StandardClaims: jwt.StandardClaims{
			Id:        uuid.New().String(),
			ExpiresAt: expirationAt.Unix(),
			IssuedAt:  now.Unix(),
			Issuer:    "go-ecommerce",
			Subject:   uuidAccessToken,
		},
	})
}

// generate token
func GenerateToken(payload jwt.Claims) (string, error) {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, payload)
	return token.SignedString([]byte(global.Config.Jwt.API_SECRET))
}

// validate token subject
func ValidateTokenSubject(tokenString string) (*PayloadClaim, error) {
	// parse token
	claims, err := ParseTokenSubject(tokenString)
	if err != nil {
		return nil, err
	}
	if err = claims.Valid(); err != nil {
		return nil, err
	}
	return claims, nil
}

// parse token subject
func ParseTokenSubject(tokenString string) (*PayloadClaim, error) {
	tokenClaim, err := jwt.ParseWithClaims(
		tokenString,
		&PayloadClaim{},
		func(token *jwt.Token) (interface{}, error) {
			return []byte(global.Config.Jwt.API_SECRET), nil
		})
	if err != nil {
		return nil, err
	}
	return tokenClaim.Claims.(*PayloadClaim), nil
}
