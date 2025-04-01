package model

type ResponseData struct {
	Code    int         `json:"code"`    // Ma status code
	Message string      `json:"message"` // Thong bao loi
	Data    interface{} `json:"data"`    // Du lieu duoc return
}

type SignatureResponse struct { 
	Signature string `json:"signature"`
	Timestamp string `json:"timestamp"`
	APIKey    string `json:"api_key"`
	CloudName string `json:"cloud_name"`
	// UploadPreset string `json:"upload_preset"`
}