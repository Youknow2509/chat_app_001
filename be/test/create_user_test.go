package test

import (
	"context"
	"fmt"
	"strconv"
	"testing"

	"example.com/be/global"
	"example.com/be/internal/database"
	"example.com/be/internal/initialize"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/service/impl"
	"example.com/be/response"
)

// test create user
func TestCreateUser(t *testing.T) {
	InitSever()

	q := database.New(global.Mdbc)
	service.InitUserLogin(impl.NewSUserLogin(q))

	iUser := service.UserLogin()

	listEmail := []string{
		"us1@gmail.com",
		"us2@gmail.com",
		"us3@gmail.com",
		"us4@gmail.com",
		"us5@gmail.com",
		"us6@gmail.com",
		"us7@gmail.com",
		"us8@gmail.com",
		"us9@gmail.com",
		"us10@gmail.com",
	}
	verifyPurpose := "TEST_USER"
	passWordTemp := "123"
	otpCodeTest := "123456"
	ctx := context.Background()

	for index, email := range listEmail {
		t.Run("Register User " + strconv.Itoa(index), func(t *testing.T) {
			regInput := &model.RegisterInput{
				VerifyKey:     email,
				VerifyType:    1,
				VerifyPurpose: verifyPurpose,
			}
			codeRes, err := iUser.Register(ctx, regInput)
			if err != nil {
				t.Fatalf("Register error: %v", err)
			}
			if codeRes != response.ErrCodeSuccess {
				t.Fatalf("Register failed with code %d: %s", codeRes, response.GetMessageCode(codeRes))
			}
		})

		var token string
		t.Run("Verify OTP " + strconv.Itoa(index), func(t *testing.T) {
			verifyInput := &model.VerifyInput{
				VerifyKey:  email,
				VerifyCode: otpCodeTest,
			}
			outVerify, err := iUser.VerifyOTP(ctx, verifyInput)
			if err != nil {
				t.Fatalf("VerifyOTP error: %v", err)
			}
			if outVerify.Token == "" {
				t.Fatal("VerifyOTP returned empty token")
			}
			token = outVerify.Token
		})

		t.Run("Update Password " + strconv.Itoa(index), func(t *testing.T) {
			updateInput := &model.UpdatePasswordInput{
				Password: passWordTemp,
				Token:    token,
			}
			codeUpdatePassword, err := iUser.UpdatePasswordRegister(ctx, updateInput)
			if err != nil {
				t.Fatalf("UpdatePasswordRegister error: %v", err)
			}
			if codeUpdatePassword != response.ErrCodeSuccess {
				t.Fatalf("UpdatePasswordRegister failed with code %d: %s", codeUpdatePassword, response.GetMessageCode(codeUpdatePassword))
			}
		})

		t.Run("Login " + strconv.Itoa(index), func(t *testing.T) {
			loginInput := &model.LoginInput{
				UserAccount:  email,
				UserPassword: passWordTemp,
			}
			codeResLogin, outLogin, err := iUser.Login(ctx, loginInput)
			if err != nil {
				t.Fatalf("Login error: %v", err)
			}
			if codeResLogin != response.ErrCodeSuccess {
				t.Fatalf("Login failed with code %d: %s", codeResLogin, response.GetMessageCode(codeResLogin))
			}
			if outLogin.Token == "" {
				t.Fatal("Login returned empty token")
			}
			fmt.Println("Login success: ", outLogin)
		})
	}
}

func InitSever() {
	// load configuration
	initialize.LoadConfigTest()
	fmt.Println("@@@ Loader configuration")

	// initialize logger
	initialize.InitLogger()
	global.Logger.Info("Logger initialized")

	// initialize mysql
	// connect to my sql
	initialize.InitMysql()
	global.Logger.Info("Mysql initialized")

	// innitialize sqlc
	initialize.InitMysqlC()
	global.Logger.Info("MysqlC initialized")

	// initialize service interface
	initialize.InitServiceInterface()
	global.Logger.Info("Service interface initialized")

	// connect to redis
	initialize.InitRedis()
	global.Logger.Info("Redis initialized")
}
