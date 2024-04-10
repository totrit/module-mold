Create module for Android project from template

!['Create Module' in 'Tools' menu in Intellij](https://github.com/totrit/module-mold/blob/main/img/screenshot.png?raw=true "'Create Module' in 'Tools' menu in Intellij")

## What This Tries To Solve
In an Android's typical multi-module project, we often need to create new modules. 
Traditionally we need to copy from an existent module. 
Then we need to tweak the copy by deleting some irrelevant files, changing `build.gradle`, adding the new module to `settings.gradle`.

It'll be good to automate this process and only need some necessary input, such module name, from the developer.

## How It Works
Assuming you have different *types* of modules in your project, such as some non-user-facing `library` modules and some other user-facing `feature` modules, etc.
You can create a config file, for the plugin, in your project to dictate how each type of module should be created.
Then the plugin will read the config file and loads up menu options in `Tools` in Android Studio (or Intellij in general). 
From the menu options you can choose what type of module you'd want to create. Then you'd be prompted to input the module name. Then the automation will do as follows:
- Copy the corresponding module template to a temporary directory
- Replace placeholders in `build.gradle` file in the copy
- Create source folder as per the package of the module
- Copy the copy in the temporary directory into the project
- Insert an entry for the new module into `settings.gradle`
- Invoke a Gradle Sync for the project

## Steps To Set Up
### 1. Install `Module Mold` plugin from Intellij Marketplace
### 2. Create `module-mold.yaml` config in root directory of the project

And it'll look like this:
```
templateRootDir: module-template
rootPackage: com.example
moduleTypes:
  - type: data
    template: android-module
  - type: feature
    template: android-module
  - type: infrastructure
    template: android-module
  - type: navigation
    template: android-module
```
In the example above:
- `templateRootDir`: The relative path of the directory that'll contain the templates you're going to create
- `rootPackage`: This will be used to concatenate with module name to form the package of the module. For example if the `rootPackage` is `com.example` and if you create a module named `foo`, then the package for the module will be `com.example.foo`. The package will be inserted into `build.gradle`, and also to create source folder in `src/main/java` etc.
- `moduleTypes`:  List out all the possible module types in your project
  - `type`: This will a directory resides under the root directory of the project.
  - `template`: This is a directory under the `templateRootDir`. And you can put whatever you want in this directory, and they'll be copied over to the new module in the end.

### 3. Create your template(s)
Like described above, you'll need a `templateRootDir` in the config file. Let's assume it is `module-template`, in which I have a template `android-module` for android modules.
Now I'm having a template(s) directory structure as this:
```
module-template
└── android-module
    ├── build.gradle
    └── src
        └── main
            ├── AndroidManifest.xml
            ├── java
            └── res
```

### 4. About Placeholders
If you have the following placeholders in your template's `build.gradle`, the plugin will replace the placeholders with real values:
- `MODULE_MOLD_MODULE_NAME`: The module name you've provided when you create the module, such as `foo` as above example
- `MODULE_MOLD_MODULE_PACKAGE`: The package of the new module, such as `com.example.foo` as above example

## Compatibility
- This plugin also works for projects that have `build.gradle.kts` and `settings.gradle.kts`

## Contributing
You're welcome to contribute to this project to cater for different project structures than current assumptions.