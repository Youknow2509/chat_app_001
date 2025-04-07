package main

import (
	"context"
	"fmt"
	"log"
	"time"

	"example.com/be/global"
	"example.com/be/internal/database"
	"example.com/be/internal/initialize"
	"example.com/be/internal/model"
	"example.com/be/internal/service"
	"example.com/be/internal/service/impl"
	"example.com/be/response"
)

/**
 * File create db template project
 * Note: Run all services before creating db template project
 */
func main() {
	InitServiceInterface()
	time.Sleep(time.Second * 1)
	fmt.Println("Server started")
	CreateUser()
	fmt.Println("Create user successfully")
	time.Sleep(time.Second * 1)
	CreateReqFriendUser()
	fmt.Println("Create friend user successfully")
}

/**
 * Create frien user
 */
func CreateReqFriendUser() {
	q := database.New(global.Mdbc)
	service.InitUserLogin(impl.NewSUserLogin(q))

	iUser := service.UserInfo()
	listEmail := []string{
		"lytranvinh.work@gmail.com",
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
	ctx := context.TODO()
	// get user id
	user1ID, err := q.GetIDUserWithEmail(ctx, listEmail[1])
	if err != nil {
		log.Fatalf("GetIDUserWithEmail error: %v", err)
	}
	if user1ID == "" {
        log.Fatalf("User not found")
    }
	// create friend request
	iUser.CreateFriendRequest(ctx, &model.CreateFriendRequestInput{
		UserID: user1ID,
		EmailFriend: listEmail[2],
	})
	// create friend request
	iUser.CreateFriendRequest(ctx, &model.CreateFriendRequestInput{
		UserID: user1ID,
		EmailFriend: listEmail[3],
	})
	// create friend request
	iUser.CreateFriendRequest(ctx, &model.CreateFriendRequestInput{
		UserID: user1ID,
		EmailFriend: listEmail[4],
	})
	// create friend request
	iUser.CreateFriendRequest(ctx, &model.CreateFriendRequestInput{
		UserID: user1ID,
		EmailFriend: listEmail[5],
	})

	// get user id
	user2ID, err := q.GetIDUserWithEmail(ctx, listEmail[2])
	if err != nil {
		log.Fatalf("GetIDUserWithEmail error: %v", err)
	}
	if user2ID == "" {
        log.Fatalf("User not found")
    }
	// create friend request
	iUser.CreateFriendRequest(ctx, &model.CreateFriendRequestInput{
		UserID: user2ID,
		EmailFriend: listEmail[3],
	})
	// create friend request
	iUser.CreateFriendRequest(ctx, &model.CreateFriendRequestInput{
		UserID: user2ID,
		EmailFriend: listEmail[4],
	})
	// create friend request
	iUser.CreateFriendRequest(ctx, &model.CreateFriendRequestInput{
		UserID: user2ID,
		EmailFriend: listEmail[5],
	})
}

/**
 * Create user
 */
func CreateUser() {
	q := database.New(global.Mdbc)
	service.InitUserLogin(impl.NewSUserLogin(q))

	iUser := service.UserLogin()

	listEmail := []string{
		"lytranvinh.work@gmail.com",
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

	// flow: register -> verify OTP -> create pw -> successfully
	ctx := context.Background()
	for index, email := range listEmail {
		// register
		regInput := &model.RegisterInput{
			VerifyKey:     email,
			VerifyType:    1,
			VerifyPurpose: verifyPurpose,
		}
		codeRes, err := iUser.Register(ctx, regInput)
		if err != nil {
			log.Fatalf("Register error: %v", err)
		}
		log.Printf("Register %d successfully with code %d: %s", index, codeRes, response.GetMessageCode(codeRes))
		time.Sleep(time.Second * 1)
		// verify OTP
		verifyInput := &model.VerifyInput{
			VerifyKey:     email,
			VerifyCode: otpCodeTest,
		}
		out, err := iUser.VerifyOTP(ctx, verifyInput)
		if err != nil {
			log.Fatalf("Verify OTP error: %v", err)
		}
		tokenVerify := out.Token
		log.Printf("Verify OTP %d successfully with token %s", index, tokenVerify)
		time.Sleep(time.Second * 1)
		// create password
		updatePasswordInput := &model.UpdatePasswordInput{
			Password: passWordTemp,
			Token:    tokenVerify,
		}
		codeRes, err = iUser.UpdatePasswordRegister(ctx, updatePasswordInput)
		if err != nil {
			log.Fatal("UpdatePasswordRegister error: %v", err)
		}
		if codeRes != response.ErrCodeSuccess {
			log.Fatalf("UpdatePasswordRegister failed with code %d: %s", codeRes, response.GetMessageCode(codeRes))
		}
	}
}

/**
 * Init service interface use
 */
func InitServiceInterface() {
	// load configuration
	initialize.LoadConfigProd()
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
