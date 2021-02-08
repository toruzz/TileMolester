rm -rf ./build/*
javac -sourcepath src -d ./build src/TileMolester.java
cp -R src/tm/icons/ build/tm/
cp src/tm/splash.gif build/tm/splash.gif
jar cvfm TileMolester.jar META-INF/MANIFEST.MF -C build/ .