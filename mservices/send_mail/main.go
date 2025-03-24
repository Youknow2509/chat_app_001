package main

import (
	"sync"

	"example.com/send_mail/initialize"
)

func main() {
	var wg sync.WaitGroup
    wg.Add(3)
	initialize.Run(&wg)

	wg.Wait()
}