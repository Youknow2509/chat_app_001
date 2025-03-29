package main

import "github.com/Youknow2509/cloudinary_manager/internal/initialize"

func main() {
	r := initialize.Init()

	r.Run(":8080")
}

