package initialize

import (
	"context"
	"log"
	"strconv"

	"github.com/Youknow2509/cloudinary_manager/internal/global"
	"github.com/redis/go-redis/v9"
)

var ctx = context.Background()

// init redis
func InitRedis() {
	r := global.Config.RedisSetting
	// Connect to Redis
	rdb := redis.NewClient(&redis.Options{
		Addr:     r.Host + ":" + strconv.Itoa(r.Port),
		Password: r.Password, // no password set
		DB:       r.Db,       // use default DB
		PoolSize: r.PoolSize, // default pool size - 10 connections in the pool
	})
	// Check connection
	_, err := rdb.Ping(ctx).Result()
	if err != nil {
		log.Printf("connection failed to | address:: %s | Password:: %s | Database:: %d", r.Host+":"+strconv.Itoa(r.Port), r.Password, r.Db)
		panic(err)
	}

	global.RedisClient = rdb
	log.Printf("connection success to | address:: %s | Password:: %s | Database:: %d", r.Host+":"+strconv.Itoa(r.Port), r.Password, r.Db)
	log.Println("Redis connection established successfully.")
}
