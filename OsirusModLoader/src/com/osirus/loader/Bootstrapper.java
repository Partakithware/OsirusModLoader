package com.osirus.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.*;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.github.classgraph.ClassInfoList;

import com.osirus.loader.*;

public class Bootstrapper {

    public static void main(String[] args) {
        System.out.println("Osirus Loader starting...");
        
        // The launcher passes game arguments - we'll use them directly
        System.out.println("Received " + args.length + " arguments from launcher");
        
        System.out.println("Creating mods folder");
        Path gameDir = Paths.get("").toAbsolutePath();
        Path modsFolder = gameDir.resolve("mods");
        try {
            Files.createDirectories(modsFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create mods folder at: " + modsFolder, e);
        }
        
        /* LWJGL probe
        System.out.println("--- LWJGL PROBE START ---");
        try {
            Class.forName("org.lwjgl.system.Library");
            System.out.println("✅ LWJGL Class found.");

            System.loadLibrary("lwjgl"); 
            System.out.println("✅ liblwjgl.so loaded successfully.");
            
            System.loadLibrary("lwjgl_opengl");
            System.out.println("✅ liblwjgl_opengl.so loaded successfully.");

            System.loadLibrary("lwjgl_glfw"); 
            System.out.println("✅ liblwjgl_glfw.so loaded successfully.");
            
            System.loadLibrary("openal"); 
            System.out.println("✅ libopenal.so loaded successfully.");

            System.out.println("--- LWJGL PROBE PASSED ---");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ CRITICAL: LWJGL Jar files are missing from Build Path! (Linux concern only, rename appropriate .so to liblwjgl_opengl in your minecraft bin folder)");
            e.printStackTrace();
            return;
        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ CRITICAL: Native library failed to load!");
            System.err.println("Current java.library.path: " + System.getProperty("java.library.path"));
            e.printStackTrace();
            
        } */
        
        // Module exports (Java 9+)
        try {
            Class<?> moduleClass = Class.forName("java.lang.Module");
            Method getModuleMethod = Class.class.getMethod("getModule");
            Method addExportsMethod = moduleClass.getMethod("addExports", String.class, moduleClass);
            
            Object baseModule = getModuleMethod.invoke(String.class);
            Object unnamedModule = getModuleMethod.invoke(Bootstrapper.class);
            
            addExportsMethod.invoke(baseModule, "sun.misc", unnamedModule);
            addExportsMethod.invoke(baseModule, "sun.nio.ch", unnamedModule);
            
            System.out.println("✅ Module exports added programmatically");
        } catch (Exception e) {
            System.err.println("⚠️ Could not add module exports: " + e.getMessage());
        }
        
        // GLFW error callback
        try {
            Class<?> glfwClass = Class.forName("org.lwjgl.glfw.GLFW");
            Class<?> errorCallbackClass = Class.forName("org.lwjgl.glfw.GLFWErrorCallback");
            
            Method setErrorCallback = glfwClass.getMethod("glfwSetErrorCallback", errorCallbackClass);
            Method createPrint = errorCallbackClass.getMethod("createPrint", java.io.PrintStream.class);
            
            Object errorCallback = createPrint.invoke(null, System.err);
            setErrorCallback.invoke(null, errorCallback);
            
            System.out.println("✅ GLFW error callback installed");
        } catch (Exception e) {
            System.err.println("⚠️ Could not install GLFW error callback: " + e.getMessage());
        }
        
        try {
            // ==========================================================
            // KEY DIFFERENCE: Get Minecraft JAR from classpath instead of hardcoding
            // ==========================================================
            
            // The launcher already added Minecraft to the classpath
            // We need to create our custom loader that wraps the current context
            ClassLoader parentLoader = Bootstrapper.class.getClassLoader();
            
            // Create our custom game loader (it will intercept Minecraft classes)
            // We pass empty URL array since Minecraft is already on parent classpath
            CustomGameClassLoader gameLoader = new CustomGameClassLoader(
                new URL[0], 
                parentLoader
            );
            
            // ==========================================================
            // MOD LOADING
            // ==========================================================

            System.out.println("Scanning mods folder...");
            File[] modFiles = modsFolder.toFile().listFiles((dir, name) -> name.endsWith(".jar"));

            if (modFiles != null && modFiles.length > 0) {
                List<URL> modUrls = new ArrayList<>();

                for (File mf : modFiles) {
                    modUrls.add(mf.toURI().toURL());
                    System.out.println("Found mod: " + mf.getName());
                }

                URLClassLoader modLoader = new URLClassLoader(
                    modUrls.toArray(new URL[0]),
                    gameLoader
                );

                try (ScanResult modScan = new ClassGraph()
                        .overrideClassLoaders(modLoader)
                        .enableClassInfo()
                        .scan()) {

                    modScan.getClassesImplementing("com.osirus.api.OsirusMod")
                        .forEach(ci -> {
                            try {
                                Class<?> modClass = modLoader.loadClass(ci.getName());
                                Object inst = modClass.getDeclaredConstructor().newInstance();
                                
                                System.out.println("Initializing mod: " + ci.getName());
                                ((com.osirus.api.OsirusMod) inst).onInitialize();
                                
                            } catch (Exception e) {
                                System.err.println("Failed to load mod: " + ci.getName());
                                e.printStackTrace();
                            }
                        });
                }
            } else {
                System.out.println("No mods found.");
            }

            // ==========================================================
            // Launch Minecraft with the arguments provided by the launcher
            // ==========================================================
            
            String mcMainClassName = "net.minecraft.client.main.Main";
            Class<?> mainClass = gameLoader.loadClass(mcMainClassName);
            
            System.out.println("Launching Minecraft...");
            mainClass.getMethod("main", String[].class).invoke(null, (Object) args);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to launch Minecraft!");
        }
    }
}