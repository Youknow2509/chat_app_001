package service

import (
	"context"
)

// interface reader
type IReaderKafka interface {
	ReadMessageAndHandle(ctx context.Context)
}

