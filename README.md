# Oneplus UI Library

This library has the scope to look like an Oneplus UI. It is based on the Oxygen Customizer project.

# Installation

To include this library in your Android project, add the following to your Gradle files:

```gradle
allprojects {
    repositories {
      ...
      maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.DHD2280:Oneplus-UI:1.0.0'
}
```

# Usage

Use one of these themes in your `themes.xml` file:

`
Theme.Oneplus.Light.NoActionBar
Theme.Oneplus.Dark.NoActionBar
`

Extend your activity to `OplusActivity`, to get dark mode support. [Enhanced/Medium/Gentle]

# © License

Oxygen Customizer is licensed under GPLv3. Please see [`LICENSE`](./LICENSE.md) for the full license text.
