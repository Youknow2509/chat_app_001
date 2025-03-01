package model

type Message struct {
	From string `json:"from"`
	To   string `json:"to"`
	Data string `json:"data"`
	Type int    `json:"type"`
}
