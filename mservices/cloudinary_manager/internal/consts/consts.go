package consts

const (
	PATH_FILE_ENV_DEV = "config/dev.env"
	PATH_FILE_ENV_PROD = "config/.env"
)

const (
	// MAX_SIZE_IMAGE = 1024 * 1024 * 5 // 5MB
	MAX_SIZE_IMAGE = 5 << 20 // 5MB
	MAX_SIZE_VIDEO = 1024 * 1024 * 50 // 50MB
)

var allowedFileTypes = map[string]bool{
    ".jpg":  true,
    ".jpeg": true,
    ".png":  true,
    ".gif":  true,
}