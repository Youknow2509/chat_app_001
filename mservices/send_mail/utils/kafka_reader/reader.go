package kafkareader

import (
	"fmt"
	"strings"
	"time"

	"github.com/segmentio/kafka-go"
)

func GetKafkaReader(topic, kafkaURL, groupID string) *kafka.Reader {
	fmt.Println("Creating kafka reader: Brokers: ", kafkaURL, " Topic: ", topic, " GroupID: ", groupID)
	return kafka.NewReader(kafka.ReaderConfig{
		Brokers: strings.Split(kafkaURL, ","),
		Topic:   topic,
		GroupID: groupID,
		CommitInterval: 1 * time.Second,
		StartOffset: kafka.LastOffset,
	})
}