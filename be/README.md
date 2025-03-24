# Contact:
- **Mail**: *lytranvinh.work@gmail.com*
- **Github**: *https://github.com/Youknow2509*

# Introduction
- **Name**: *Lý Trần Vinh*
- **Description**: *Core back end game*

# How to use
- Install sqlc global, after render model with `.sql`
```bash
    go install github.com/sqlc-dev/sqlc/cmd/sqlc@latest
    make sqlc_generate
```

- Recreate docs swagger
```bash
    make swagger_generate
```

- Create file `.env` for `docker-compose`, after move to folder `environment`
```bash
    make cre_env
    mv .env ./environment
```

- Run docker-compose
```bash
    make docker_run
```

- - Install `goose` create database 
```bash
    go install github.com/pressly/goose/v3/cmd/goose@latest
    make goose_up
```

- Run server
```bash
    make run_server
```
