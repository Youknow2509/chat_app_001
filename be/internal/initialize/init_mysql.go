package initialize

import (
	"fmt"
	"time"

	"example.com/be/global"

	"go.uber.org/zap"
	"gorm.io/driver/mysql"
	"gorm.io/gen"
	"gorm.io/gorm"
)

// Handle err panic
func checkErrorPanic(err error, errString string) {
	if err != nil {
		global.Logger.Error(errString, zap.Error(err))
		panic(err)
	}
}

// Initial my sql
func InitMysql() {
	m := global.Config.MySQL

	dsn := "%s:%s@tcp(%s:%v)/%s?charset=utf8mb4&parseTime=True&loc=Local"
	var s = fmt.Sprintf(dsn, m.Username, m.Password, m.Host, m.Port, m.Dbname)

	db, err := gorm.Open(mysql.Open(s), &gorm.Config{
		// SkipDefaultTransaction: false,
	})
	if err != nil {
		global.Logger.Error("MySQL connection failed to " + s + ": " + err.Error())
		panic(err)
	}

	// checkErrorPanic(err, "MySQL connection failed")

	global.Logger.Info("MySQL connection successful")
	global.Mdb = db

	// set Pool
	SetPool()

}

// InitMysql().SetPool()
func SetPool() {
	m := global.Config.MySQL

	sqlDb, err := global.Mdb.DB()
	if err != nil {
		fmt.Println("MySQL error: %s::", err)
		global.Logger.Error("SetPool error", zap.Error(err))
	}

	sqlDb.SetConnMaxIdleTime(time.Duration(m.MaxIdleConns))    // Thoi gian toi da ket noi nhan doi -> Phuc hoi ket noi
	sqlDb.SetMaxOpenConns(m.MaxOpenConns)                      // Gioi han so luong ket noi toi da
	sqlDb.SetConnMaxLifetime(time.Duration(m.ConnMaxLifetime)) // Gioi han thoi gian toi da cua ket noi

}

// genTableDAO
func genTableDAO() {
	g := gen.NewGenerator(gen.Config{
		OutPath: "./internal/model",                                                 // output path
		Mode:    gen.WithoutContext | gen.WithDefaultQuery | gen.WithQueryInterface, // generate mode
	})

	// gormdb, _ := gorm.Open(mysql.Open("root:@(127.0.0.1:3306)/demo?charset=utf8mb4&parseTime=True&loc=Local"))
	g.UseDB(global.Mdb) // reuse your gorm db

	// Generate all table
	g.GenerateAllTable()

	// Generate the code
	g.Execute()
}
