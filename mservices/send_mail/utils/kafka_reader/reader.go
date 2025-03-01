package kafkareader

import (
	"strings"
	"time"

	"github.com/segmentio/kafka-go"
)

func GetKafkaReader(topic, kafkaURL, groupID string) *kafka.Reader {
	return kafka.NewReader(kafka.ReaderConfig{
		Brokers: strings.Split(kafkaURL, ","),
		Topic:   topic,
		GroupID: groupID,
		CommitInterval: 1 * time.Second,
		StartOffset: kafka.LastOffset,
	})
}