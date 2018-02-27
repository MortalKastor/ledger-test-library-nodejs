#npm i

#If we need to build xcode project, leave only those commands
npm run clean
npm i
rm -rf build
npm run gypconfigx
npm run gypconfig
npm run gypbuild
open build/binding.xcodeproj
