SHELL := /bin/sh

.PHONY: setup test verify build up down logs clean

setup:
	./mvnw -B -ntp dependency:go-offline

test:
	./mvnw -B -ntp test

verify:
	./mvnw -B -ntp verify

build:
	./mvnw -B -ntp -DskipTests package

up:
	docker compose up --build --detach

down:
	docker compose down

logs:
	docker compose logs --follow app

clean:
	./mvnw -B -ntp clean
	docker compose down --volumes --remove-orphans
