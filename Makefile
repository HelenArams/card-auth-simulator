.PHONY: build test run coverage docker-build docker-run clean

IMAGE ?= card-auth-simulator

build:
	mvn -B verify

test:
	mvn -B test

run:
	mvn spring-boot:run

coverage: build
	@echo "open target/site/jacoco/index.html"

docker-build:
	docker build -t $(IMAGE) .

docker-run:
	docker run --rm -p 8080:8080 $(IMAGE)

clean:
	mvn -B clean
