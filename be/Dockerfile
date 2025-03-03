
FROM golang:alpine AS builder

WORKDIR /build

COPY . .

RUN go mod tiny

RUN go mod download

RUN go build -o crm.chatappbackend.com ./cmd/server

FROM scratch

COPY ./config /config

COPY --from=builder /build/crm.chatappbackend.com /

ENTRYPOINT [ "/crm.chatappbackend.com", "config/prod.yaml" ]