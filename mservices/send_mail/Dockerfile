
FROM golang:alpine AS builder

WORKDIR /build

COPY . .

RUN go mod download

RUN go build -o crm.chatapp.service.sendmail.com ./main.go

FROM scratch

COPY ./config /config

COPY --from=builder /build/crm.chatapp.service.sendmail.com /

ENTRYPOINT [ "/crm.chatapp.service.sendmail.com", "config/prod.yaml" ]