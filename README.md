# VerticalLayout
Android layout widget to arrange a view vertically (90 or -90 degrees rotated).

Many developers were looking for a solution to render text vertically in android. So I thought
why should text have all the fun? ;)

I created a generic `VerticalLayout` to render any layout
vertically in android (including `TextView`).

## How to use?
### Installing library
In project level `build.gradle`, add Jitpack under `allprojects`
```
allprojects {
  repositories {
    ...
    maven { url "https://jitpack.io" }
  }
}
```
Then in module level `build.gradle`, add this dependency under `dependencies`
```
dependencies {
  ...
  implementation 'com.github.mohitatray:VerticalLayout:1.0.3'
}
```

### Usage
Just put whatever you want rendered vertically inside the `VerticalLayout`.
```
<com.mohitatray.verticallayout.VerticalLayout
  android:layout_width="wrap_content"
  android:layout_height="wrap_content">
  
  <TextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="This text will appear vertically."/>
  
</com.mohitatray.verticallayout.VerticalLayout>
```

The text in this `TextView` will appear verically. Thats it!

If you want the text to appear typed top to bottom instead of bottom to top,
then just add `app:layout_direction="end_to_start"` attribute to the `TextView`.
