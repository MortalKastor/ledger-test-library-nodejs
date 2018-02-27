all: mac ios android

clean:
	-rm -rf build/
	-rm -rf deps/build/
	-rm -rf build_mac/
	-rm -rf build_ios/
	-rm -rf obj/
	-rm -rf libs/
	-rm -rf djinni-output-temp/
	-rm GypAndroid.mk
	-rm *.target.mk
	-rm deps/*.target.mk
	-rm play

gyp: ./deps/gyp

./deps/gyp:
	git submodule update --init

./deps/djinni:
	git submodule update --init

djinni-output-temp/gen.stamp ledgerapp.cidl:
	./run_djinni.sh

djinni:
	./deps/gradle/gradlew djinni

# instruct gyp to build using the "xcode" build generator, also specify the OS
# (so we can conditionally compile using that var later)
build_mac/ledgerapp.xcodeproj: deps/gyp deps/json11 ledgerapp.gyp djinni
	PYTHONPATH=deps/gyp/pylib deps/gyp/gyp ledgerapp.gyp -DOS=mac --depth=. -f xcode --generator-output=./build_mac -Icommon.gypi

build_ios/ledgerapp.xcodeproj: deps/gyp deps/json11 ledgerapp.gyp djinni
	PYTHONPATH=deps/gyp/pylib deps/gyp/gyp ledgerapp.gyp -DOS=ios --depth=. -f xcode --generator-output=./build_ios -Icommon.gypi


GypAndroid.mk: deps/gyp deps/json11 ledgerapp.gyp djinni
	ANDROID_BUILD_TOP=dirname PYTHONPATH=deps/gyp/pylib $(which ndk-build) deps/gyp/gyp --depth=. -f android -DOS=android --root-target libledgerapp_android -Icommon.gypi ledgerapp.gyp

xb-prettifier := $(shell command -v xcpretty >/dev/null 2>&1 && echo "xcpretty -c" || echo "cat")

# a simple place to test stuff out
play: build_mac/ledgerapp.xcodeproj objc/play.m
	xcodebuild -project build_mac/ledgerapp.xcodeproj -configuration Debug -target play_objc | ${xb-prettifier} && ./build/Debug/play_objc

mac: build_mac/ledgerapp.xcodeproj
	xcodebuild -project build_mac/ledgerapp.xcodeproj -configuration Release -target libledgerapp_objc | ${xb-prettifier}

ios: build_ios/ledgerapp.xcodeproj
	xcodebuild -project build_ios/ledgerapp.xcodeproj -configuration Release -target libledgerapp_objc | ${xb-prettifier}

test: build_mac/ledgerapp.xcodeproj
	xcodebuild -project build_mac/ledgerapp.xcodeproj -configuration Debug -target test | ${xb-prettifier} && ./build/Debug/test

cleanup_gyp: ./deps/gyp ledgerapp.gyp common.gypi
	deps/gyp/tools/pretty_gyp.py deps/json11.gyp > json11_temp.gyp && mv json11_temp.gyp deps/json11.gyp
	deps/gyp/tools/pretty_gyp.py ledgerapp.gyp > ledgerapp_temp.gyp && mv ledgerapp_temp.gyp ledgerapp.gyp
	deps/gyp/tools/pretty_gyp.py common.gypi > common_temp.gypi && mv common_temp.gypi common.gypi

.PHONY: djinni
