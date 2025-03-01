package initialize

import (
	"fmt"
	"sync"
)

// function run
func Run(wg *sync.WaitGroup) {
	fmt.Println("Service is running")

	// Load configuration
	LoadConfig()

	// Initialize Service and .. use
	Initialize()
	
	// Read and process messages from Kafka
	ReaderAndProcess(wg)
}