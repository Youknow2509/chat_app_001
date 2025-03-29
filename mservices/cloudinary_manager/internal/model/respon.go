package model

type ResponseData struct {
	Code    int         `json:"code"`    // Ma status code
	Message string      `json:"message"` // Thong bao loi
	Data    interface{} `json:"data"`    // Du lieu duoc return
}
