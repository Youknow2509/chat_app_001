
FROM golang:alpine AS builder

WORKDIR /build

COPY . .

RUN go mod download

RUN go build -o crm.chatapp.com ./cmd/server

FROM scratch

COPY ./config /config

COPY --from=builder /build/crm.chatapp.com /

ENTRYPOINT [ "/crm.chatapp.com", "config/local.yaml" ]