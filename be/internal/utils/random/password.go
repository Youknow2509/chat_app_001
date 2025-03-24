package random

import (
	"crypto/rand"
	"log"
	"strings"
)

const (
	lowerChars = "abcdefghijklmnopqrstuvwxyz"
	upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
	digits     = "0123456789"
	specials   = "!@#$%^&*()-_=+[]{}<>?"
)

func GeneratePassword(length int) string {
	if length < 8 {
		log.Fatal("Password length should be at least 8 characters")
	}

	allChars := lowerChars + upperChars + digits + specials
	var password strings.Builder

	// Ensure at least one character from each category
	password.WriteByte(lowerChars[randInt(len(lowerChars))])
	password.WriteByte(upperChars[randInt(len(upperChars))])
	password.WriteByte(digits[randInt(len(digits))])
	password.WriteByte(specials[randInt(len(specials))])

	// Fill the rest with random characters
	for i := 4; i < length; i++ {
		password.WriteByte(allChars[randInt(len(allChars))])
	}

	// Shuffle the password to randomize the order
	shuffledPassword := shuffleString(password.String())
	return shuffledPassword
}

func randInt(n int) int {
	b := make([]byte, 1)
	_, err := rand.Read(b)
	if err != nil {
		log.Fatal(err)
	}
	return int(b[0]) % n
}

func shuffleString(s string) string {
	runes := []rune(s)
	for i := range runes {
		j := randInt(len(runes))
		runes[i], runes[j] = runes[j], runes[i]
	}
	return string(runes)
}

