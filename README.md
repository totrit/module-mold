Create module for Android project from your own template

![Create config file and template(s)](https://github.com/totrit/module-mold/blob/main/img/setup.png?raw=true)
![Choose which type of module to create](https://github.com/totrit/module-mold/blob/main/img/choose-type.png?raw=true)
![Choose a name for the new module](https://github.com/totrit/module-mold/blob/main/img/choose-name.png?raw=true)
![New module created!](https://github.com/totrit/module-mold/blob/main/img/result.png?raw=true)

## What This Tries To Solve
In an Android's typical multi-module project, we often need to create new modules. 
And the Android Studio's built-in `New Module...` isn't for everyone because, for instance, you may have an established pattern in your project of how the `build.gradle` of a module would look like.
Traditionally we need to copy from an existent module in your project. 
Then we need to tweak the copy by deleting some irrelevant files, changing `build.gradle`, registering the new module to `settings.gradle`, etc.

It'll be good to automate this process so that minimum intervene is required from the developer, such as the module name.

## How It Works
Assuming you have different *types* of modules in your project, such as some non-user-facing `library` modules and some other user-facing `feature` modules, etc.
You can create a config file, for the plugin, in your project to dictate how each type of module should be created.
Then the plugin will read the config file and loads up menu options in `Tools` in Android Studio (or Intellij in general). 
From the menu options you can choose what type of module you'd want to create. Then you'd be prompted to input the module name. Then the automation will do as follows:
- Copy the corresponding module template to a temporary directory
- Replace placeholders in `build.gradle` file in the copy
- Create source folder as per the package of the module
- Copy *the copy* in the temporary directory into the project
- Register the new module `settings.gradle`
- Invoke a Gradle Sync for the project

## Steps To Set Up
### 1. Install `Module Mold` plugin from Intellij Marketplace
### 2. Create `module-mold.yaml` config in root directory of the project

And it'll look something like this:
```
templateRootDir: module-template
rootPackage: com.example
moduleTypes:
  - type: feature # Notice here 'feature' will also be used as the root folder for all the created modules of this type
    template: android-lib
  - type: kotlin library
    template: kotlin-lib
    rootDir: library  # Use 'rootDir' when the module needs to be under a folder that's different from the name of the 'type'
  - type: android library
    template: android-lib
    rootDir: library
  - type: top-level library
    template: android-lib
    rootDir: .  # Notice here the module can be directly put under project root folder
```
In the example above:
- `templateRootDir`: The relative path of the directory that'll contain the templates you're going to create
- `rootPackage`: This will be used to concatenate with module name to form the package of the module. For example if the `rootPackage` is `com.example` and if you create a module named `foo`, then the package for the module will be `com.example.foo`. The package will be inserted into `build.gradle`, and also to create source folder in `src/main/java` etc.
- `moduleTypes`:  List out all the possible module types in your project
  - `type`: This will a directory resides under the root directory of the project.
  - `template`: This is a directory under the `templateRootDir`. And you can put whatever you want in this directory, and they'll be copied over to the new module in the end.
  - `rootDir`: Use this when the dir that you want to put the new module into is different from the name of 'type'. And use '.' to indicate you want the new module to be directly under root directory of the project

### 3. Create your template(s)
Like described above, you'll need to designate the `templateRootDir` in the config file. Let's assume it is `module-template`, in which I have a template `android-lib` for android modules, and 'kotlin-lib' for plain Kotlin modules.
Now I'm having the template(s) directory structure as this:
```
module-template
├── android-lib
│   ├── build.gradle
│   └── src
│       ├── main
│       │   ├── AndroidManifest.xml
│       │   ├── java
│       │   └── res
│       └── test
│           └── java
└── kotlin-lib
    ├── build.gradle
    └── src
        └── main
            └── java
```
Obviously you can create as many templates as you want.

### 4. About Placeholders
If you have the following placeholders in your template's `build.gradle`, the plugin will replace the placeholders with real values:
- `MODULE_MOLD_MODULE_NAME`: The module name you've provided when you create the module, such as `foo` as above example
- `MODULE_MOLD_MODULE_PACKAGE`: The package of the new module, such as `com.example.foo` as above example

## Compatibility
- This plugin also works for projects that have `build.gradle.kts` and `settings.gradle.kts`

## Contributing
You're welcome to contribute to this project to cater for different project structures than current assumptions.