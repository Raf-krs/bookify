#!/bin/bash

export SERVER_PORT=8081
export ADMIN_PASSWORD=$2a$10$ObIuP710TxeIZBhVSkQjIehwLggAb1SW2dJj.WQ7e6fnfO45Gy/EG
export SWAGGER_DEV_URL=http://localhost:8081/docs
export BOOKIFY_DB_URL=jdbc:postgresql://localhost:5439/bookify
export BOOKIFY_DB_USERNAME=postgres
export BOOKIFY_DB_PASSWORD=postgres
export JWT_KEY=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
export JWT_EXPIRATION=86400000
export RABBITMQ_HOST=localhost
export RABBITMQ_PORT=5673
export RABBITMQ_USERNAME=guest
export RABBITMQ_PASSWORD=guest
export RABBITMQ_VHOST=/
export RABBITMQ_QUEUE_NOTIFICATION=user-registration