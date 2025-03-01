package initialize

import (
	"fmt"

	"example.com/send_mail/global"
	"github.com/spf13/viper"
)

// load config
func LoadConfig() {
	viper := viper.New()
	viper.AddConfigPath("./config") // paht to config
	viper.SetConfigName("local")    // file name
	viper.SetConfigType("yaml")     // type file

	// read config
	err := viper.ReadInConfig()
	if err != nil {
		panic(fmt.Errorf("Failed to read config: %v\n", err))
	}

	// configure struct
	err = viper.Unmarshal(&global.Config)
	if err != nil {
		panic(fmt.Errorf("Failed to unmarshal config: %v\n", err))
	}

	fmt.Println("Config loaded successfully.")
}