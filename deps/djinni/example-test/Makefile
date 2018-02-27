all: android ios localhost

android: example.djinni
	@make -C .. GypAndroid.mk

ios: example.djinni
	@make -C .. ./build_ios/example/libtestapp.xcodeproj

localhost: example.djinni
	cd localhost && ant compile jar run

.PHONY: android ios localhost
