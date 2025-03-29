package setting

// config setting
type Config struct {
	JwtSetting        *JwtSetting        `json:"jwt"`
	Mongo             *MongoSetting      `json:"mongo"`
	CloudinarySetting *CloudinarySetting `json:"cloudinary"`
}

// Jwt setting
type JwtSetting struct {
	JWT_SECRET          string `json:"api_secret"`
	JWT_EXPIRED         string `json:"jwt_expired"`
	JWT_REFRESH_EXPIRED string `json:"jwt_refresh_expired"`
	JWT_ISSUER          string `json:"jwt_issuer"`
	JWT_AUDIENCE        string `json:"jwt_audience"`
}

// cloudinary setting
type CloudinarySetting struct {
	CloudName    string `json:"cloud_name"`
	APIKey       string `json:"api_key"`
	APISecret    string `json:"api_secret"`
	UploadPreset string `json:"upload_preset"`
}

// mongo setting
type MongoSetting struct {
	Host       string `json:"host"`
	Port       int    `json:"port"`
	UserName   string `json:"username"`
	Password   string `json:"password"`
	Collection string `json:"collection"`
}
