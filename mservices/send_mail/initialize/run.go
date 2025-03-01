package initialize

import (
	"fmt"
)

// function run
func Run() {
	fmt.Println("Service is running")

	// Load configuration
	LoadConfig()

	// Initialize Service and .. use
	Initialize()
	
	// Read and process messages from Kafka
	ReaderAndProcess()
}