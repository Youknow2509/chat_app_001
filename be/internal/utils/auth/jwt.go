package auth

import (
	"fmt"
	"strconv"
	"time"

	"example.com/be/global"
	"example.com/be/internal/model"
	"github.com/golang-jwt/jwt"
	"github.com/google/uuid"
)

// create token with uuid and user id
func CreateToken(uuidToken string, userID string) (string, error) {
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

	return GenerateToken(&model.PayloadClaim{
		StandardClaims: jwt.StandardClaims{
			Id:        uuid.New().String(),
			ExpiresAt: expirationAt.Unix(),
			IssuedAt:  now.Unix(),
			Issuer:    "go-ecommerce",
			Subject:   uuidToken,
		},
		UserID: userID,
	})
}

// create refresh token
func CreateRefreshToken(uuidToken string) (string, error) {
	// set time expiration
	timEx := global.Config.Jwt.JWT_REFRESH_EXPIRED
	if timEx == "" {
		timEx = "7"
	}
	timExInt, err := strconv.ParseInt(timEx, 10, 64)
	if err != nil {
		return "Pare time", err
	}

	timeExInt := timExInt * 24
	timeEx := fmt.Sprintf("%dh", timeExInt)
	// convert to time duration
	expiration, err := time.ParseDuration(timeEx)
	if err != nil {
		return "", err
	}

	now := time.Now()
	expirationAt := now.Add(expiration)

	return GenerateToken(&model.PayloadClaim{
		StandardClaims: jwt.StandardClaims{
			Id:        uuid.New().String(),
			ExpiresAt: expirationAt.Unix(),
			IssuedAt:  now.Unix(),
			Issuer:    "go-ecommerce",
			Subject:   uuidToken,
		},
	})
}

// generate token
func GenerateToken(payload jwt.Claims) (string, error) {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, payload)
	return token.SignedString([]byte(global.Config.Jwt.API_SECRET))
}

// validate token subject
func ValidateTokenSubject(tokenString string) (*model.PayloadClaim, error) {
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
func ParseTokenSubject(tokenString string) (*model.PayloadClaim, error) {
	tokenClaim, err := jwt.ParseWithClaims(
		tokenString,
		&model.PayloadClaim{},
		func(token *jwt.Token) (interface{}, error) {
			return []byte(global.Config.Jwt.API_SECRET), nil
		})
	if err != nil {
		return nil, err
	}
	return tokenClaim.Claims.(*model.PayloadClaim), nil
}
