package auth

import (
	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/Youknow2509/cloudinary_manager/internal/model"
	"github.com/golang-jwt/jwt"
)

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
			return []byte(global.Config.JwtSetting.JWT_SECRET), nil
		})
	if err != nil {
		return nil, err
	}
	return tokenClaim.Claims.(*model.PayloadClaim), nil
}
