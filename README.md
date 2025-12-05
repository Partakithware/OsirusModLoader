![Osirus Mod Loader](https://imgur.com/C9PqLXc.png)

[![Downloads](https://img.shields.io/github/downloads/Partakithware/OsirusModLoader/total?style=flat&colorA=8a00ff&colorB=ff00f5)](https://github.com/Partakithware/OsirusModLoader/releases) ![PRs](https://img.shields.io/github/issues-pr/Partakithware/OsirusModLoader) ![PRs Closed](https://img.shields.io/github/issues-pr-closed/Partakithware/OsirusModLoader) ![Issues](https://img.shields.io/github/issues/Partakithware/OsirusModLoader?style=flat&color=ff0033) ![Issues Closed](https://img.shields.io/github/issues-closed/Partakithware/OsirusModLoader?style=flat&color=00ff66)







Looking to help? put in a PR, versions and code are per branch ex:(1.21.11-pre5) main will not have such.

🔱 Osirus Loader
A lightweight, transparent mod loader for Minecraft that just works.
What Makes Osirus Different?
Osirus Loader takes a fundamentally different approach to Minecraft modding. Instead of complex toolchains, wrapper launchers, or heavy frameworks, Osirus integrates seamlessly with the official Minecraft launcher while giving you complete control over your modding experience.

🎯 Key Features

Native Launcher Integration

Works directly with the official Minecraft launcher - no custom launchers required

Simple version profile system - just select "Osirus" and play

Inherits all vanilla libraries and settings automatically

Zero configuration needed for most users

Clean Architecture

Minimal overhead - only loads what you need

Transparent class loading - you can see exactly what's happening

Direct access to Minecraft's internals without abstractions

No bytecode manipulation or transformation - mods run in pure Java

Developer-Friendly

Dead simple mod API - just implement OsirusMod interface

Automatic mod discovery with ClassGraph

No build system lock-in - works with Maven, Gradle, or manual compilation

Clear separation between loader and game code

Pure Java Approach

No mixins, no ASM, no bytecode magic

Write mods in straightforward Java code

Easier debugging and development

Faster load times without transformation overhead

📦 How It Works
Osirus uses a two-stage loading system:

Bootstrapper Stage: Initializes the loader and scans for mods in the mods folder

Game Stage: Launches Minecraft with your mods loaded and ready

This approach keeps things simple and predictable - your mods are just Java code running alongside Minecraft.

🚀 Getting Started

For Players:

Download Osirus Loader

Run the installer jar with Java, depending on OS double-click to open.

In your minecraft-launcher...

Select Installations, Create New...

Find "release 1.21.11-pre5-osirus" and choose it.

Launch the profile. Done! 

Drop mods in .minecraft/mods/

Play!

For Developers:

import net.minecraft* and code using the official classes.

```
public class MyMod implements OsirusMod {
    @Override
    public void onInitialize() {
        System.out.println("My mod loaded!");
        // Your mod code here - plain Java!
    }
}
```

For example maven: POM


<details>
<summary>Spoiler</summary>

```
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.max</groupId>
    <artifactId>my-first-mod</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- your MOD API (dummy for example) -->
    <dependency>
    <groupId>com.osirus</groupId>
    <artifactId>osirus-loader</artifactId>
    <version>1.0.0</version>
    <scope>system</scope>
    <systemPath>/YourMinecraftDirectory/libraries/com/osirus/osirus-loader/1.0.0/osirus-loader-1.0.0.jar</systemPath>
	</dependency>
	<dependency>
    <groupId>com.osirus.minecraft</groupId>
    <artifactId>minecraft-client</artifactId>
    <version>1.21.11-pre5</version>
    <scope>system</scope>
    <systemPath>/YourMinecraftDirectory/versions/1.21.11-pre5-osirus/1.21.11-pre5-osirus.jar</systemPath>
	</dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- build a normal jar -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

```


</details>

As of right now you'll need to add something to wait for minecraft to initialize in this early version of the loader. So in OnInitialize() add something like.


<details>
<summary>Spoiler</summary>

```
        // Start a simple tick loop (this is an example not drag and drop)
        Thread tickThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                        
                        Minecraft mc = Minecraft.getInstance();
                        if (mc != null) {
                            mc.execute(new Runnable() {
                                @Override
                                public void run() {}
                            });
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        tickThread.start();
```

</details>




Package your mod as a JAR, drop it in the mods folder, and Osirus handles the rest.

🎓 Philosophy
Transparency Over Magic

You should understand what your mod loader does

No hidden abstraction layers or mysterious wrappers

Clean, readable code that makes sense

Simplicity Over Features

Core loader stays minimal and fast

No complex transformation pipelines

No bloat from unused functionality

Pure Java Over Bytecode Hacks

Write mods the way you write normal Java programs

No learning special APIs or transformation frameworks

Debug with standard Java tools

🔧 Technical Details

Java 21 native support

Unobfuscated Minecraft snapshot compatible

ClassGraph for fast, efficient mod scanning

Custom ClassLoader for clean mod isolation

Module system compliant with modern Java

Client-side modding focused

🌟 Perfect For

Developers who want simplicity and transparency

Beginners learning Minecraft modding without complex frameworks

Advanced users who prefer pure Java over bytecode manipulation

Anyone tired of bloated frameworks and steep learning curves (They do have more capabilities though)

🛣️ Roadmap

✅ Basic mod loading

✅ Official launcher integration

✅ Automatic mod discovery

🚧 In-game mod menu (basic mod for it available)

🚧 Mod configuration API

🚧 Better error reporting

📋 Multi-version compatibility

📋 Mod dependency resolution

💬 Community

Osirus is built on the principle that modding should be accessible and understandable. Whether you're a veteran modder or just starting out, Osirus gives you the tools without the complexity.

No mixins to learn. No ASM to master. Just Java.

Current Version: 1.0.0
Minecraft Compatibility: 1.21.11-pre5+ (unobfuscated)
License: MIT
Discord: Coming Soon!
GitHub: Not yet!


Osirus Loader - Pure Java modding.
