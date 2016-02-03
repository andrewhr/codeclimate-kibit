.PHONY: image

IMAGE_NAME ?= codeclimate/codeclimate-kibit

image:
	docker build --rm -t $(IMAGE_NAME) .
