rm -rf ./build/*
javac -sourcepath src -d ./build src/TileMolester.java
cp -R resources/icons/ build/production/TileMolester/tm/
cp resources/splash.gif build/production/TileMolester/tm/splash.gif
jar cvfm TileMolester.jar META-INF/MANIFEST.MF -C build/ .