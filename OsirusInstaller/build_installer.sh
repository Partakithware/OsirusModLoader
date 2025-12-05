#!/bin/bash

echo "Building Osirus Loader Installer..."

# Set your project paths
LOADER_JAR="/home/max/.minecraft/libraries/com/osirus/osirus-loader/1.0.0/osirus-loader-1.0.0.jar"
VERSION_JSON="/home/max/.minecraft/versions/1.21.11-pre5-osirus/1.21.11-pre5-osirus.json"
INSTALLER_SRC="/home/max/maven-projects/OsirusInstaller/src/com/osirus/installer/OsirusInstaller.java"
OUTPUT_DIR="/home/max/maven-projects/OsirusInstaller"

# Check if loader JAR exists
if [ ! -f "$LOADER_JAR" ]; then
    echo "❌ Error: Loader JAR not found at $LOADER_JAR"
    echo "Run build.sh first to create the loader!"
    exit 1
fi

# Check if version JSON exists
if [ ! -f "$VERSION_JSON" ]; then
    echo "❌ Error: Version JSON not found at $VERSION_JSON"
    exit 1
fi

# Create directories
mkdir -p installer/build
mkdir -p installer/temp

# Step 1: Create the ZIP file that will be bundled
echo "Step 1: Creating osirus-files.zip..."

cd installer/temp

# Create the directory structure that will be extracted to .minecraft
mkdir -p versions/1.21.11-pre5-osirus
mkdir -p libraries/com/osirus/osirus-loader/1.0.0
mkdir -p mods

# Copy version JSON
cp "$VERSION_JSON" versions/1.21.11-pre5-osirus/
echo "  Copied version JSON"

# Copy loader JAR
cp "$LOADER_JAR" libraries/com/osirus/osirus-loader/1.0.0/
echo "  Copied loader JAR"

# Create a README in mods folder
cat > mods/README.txt << 'EOF'
Osirus Loader - Mods Folder

Place your .jar mod files here.

Mods must implement the OsirusMod interface:
  public class MyMod implements OsirusMod {
      @Override
      public void onInitialize() {
          // Your mod code here
      }
  }

For more information, visit: [Your GitHub URL]

Osirus Uses ClassGraph:

The MIT License (MIT)

Copyright (c) 2019 Luke Hutchison

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

EOF

# Create the zip
zip -r ../osirus-files.zip * > /dev/null

cd ../..

echo "✅ osirus-files.zip created"

# Step 2: Compile the installer
echo "Step 2: Compiling installer..."

javac -d installer/build "$INSTALLER_SRC"

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✅ Installer compiled"

# Step 3: Copy osirus-files.zip into the build directory
cp installer/osirus-files.zip installer/build/

# Step 4: Create the installer JAR with manifest
echo "Step 3: Creating installer JAR..."

cd installer/build

# Create manifest
cat > MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Main-Class: com.osirus.installer.OsirusInstaller
EOF

# Create JAR with manifest and include osirus-files.zip as a resource
jar cvfm osirus-installer-1.0.0.jar MANIFEST.MF com/ osirus-files.zip > /dev/null

# Move to output directory
mv osirus-installer-1.0.0.jar "$OUTPUT_DIR/"

cd ../..

echo "✅ Installer JAR created: $OUTPUT_DIR/osirus-installer-1.0.0.jar"

# Step 5: Test the installer (optional)
echo ""
echo "Build complete! Your installer is at:"
echo "  $OUTPUT_DIR/osirus-installer-1.0.0.jar"
echo ""
echo "To test the installer, run:"
echo "  java -jar $OUTPUT_DIR/osirus-installer-1.0.0.jar"
echo ""
echo "To upload to Modrinth:"
echo "  1. Upload osirus-installer-1.0.0.jar"
echo "  2. Mark it as compatible with Minecraft 1.21.x"
echo "  3. Set loader type as 'Modded'"

# Clean up temp files
rm -rf installer/temp
rm -rf installer/build
rm -f installer/osirus-files.zip

echo ""
echo "🎉 Done!"
