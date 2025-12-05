#!/bin/bash

# Osirus Loader Build Script

echo "Building Osirus Loader..."

# Clean previous build
rm -rf bin
mkdir -p bin

# Compile (adjust classgraph path if needed)
javac -cp "libs/classgraph-4.8.180.jar" \
      -d bin \
      src/com/osirus/**/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Create JAR
cd bin
jar cvf osirus-loader-1.0.0.jar com/
cd ..

# Install to launcher libraries
mkdir -p ~/.minecraft/libraries/com/osirus/osirus-loader/1.0.0/
cp bin/osirus-loader-1.0.0.jar ~/.minecraft/libraries/com/osirus/osirus-loader/1.0.0/

echo "✅ Build complete! JAR installed to ~/.minecraft/libraries/"
echo "Launch Minecraft with the 'Osirus Loader' profile"

read p ""
