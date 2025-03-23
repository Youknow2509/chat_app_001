package initialize

import (
	"fmt"

	"example.com/be/global"
	"github.com/spf13/viper"
)

// func load configuration with viper - DEV
func LoadConfig() {
	viper := viper.New()
	viper.AddConfigPath("./../config") // paht to config
	viper.SetConfigName("local")    // ten file
	viper.SetConfigType("yaml")     // loai file

	// read config
	err := viper.ReadInConfig()
	if err != nil {
		panic(fmt.Errorf("failed to read config: %v\n", err))
	}

	// configure struct
	err = viper.Unmarshal(&global.Config)
	if err != nil {
		panic(fmt.Errorf("failed to unmarshal config: %v\n", err))
	}
}

// func load configuration with viper - PRODUCT
func LoadConfigProd() {
	viper := viper.New()
	viper.AddConfigPath("./config") // paht to config
	viper.SetConfigName("prod")    // ten file
	viper.SetConfigType("yaml")     // loai file

	// read config
	err := viper.ReadInConfig()
	if err != nil {
		panic(fmt.Errorf("failed to read config: %v\n", err))
	}

	// configure struct
	err = viper.Unmarshal(&global.Config)
	if err != nil {
		panic(fmt.Errorf("failed to unmarshal config: %v\n", err))
	}
}


// func load config test
func LoadConfigTest() {
    viper := viper.New()
    viper.AddConfigPath("../../config") // paht to config
    viper.SetConfigName("local")    // ten file
    viper.SetConfigType("yaml")     // loai file

    // read config
    err := viper.ReadInConfig()
    if err != nil {
        panic(fmt.Errorf("failed to read config: %v\n", err))
    }

    // configure struct
    err = viper.Unmarshal(&global.Config)
    if err != nil {
        panic(fmt.Errorf("failed to unmarshal config: %v\n", err))
    }
}
