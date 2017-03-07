# 使用手册

[TOC]

## 引入

Android Studio 在module的build.gradle中添加：

```groovy
compile 'com.squareup.okhttp3:okhttp:3.5.0'
```

## 使用

1. 在layout中添加控件`ProgressBar`，设置如下属性：

   ```xml
   <ProgressBar
           android:id="@+id/progress_bar"
           android:layout_width="match_parent"
           android:layout_height="wrap_content"
           style="?android:attr/progressBarStyleHorizontal"
           android:visibility="visible" />
   ```

2. 创建`lili.com.simpledownloader.Download`类实例并传入`Contex`

   ```java
   Download download = new Download(contex);
   ```

3. 设置三个按钮，分别为`开始下载`、`暂停`、`继续下载`。

4. 在`开始下载`按钮响应中调用`url(String url)`传入下载链接（必须），调用`setSavePath(File file)`传入存储地址(不调用则以默认名存入默认地址)，调用`setProgress(ProgressBar progressBar)`传入ProgressBar实例（不调用则无进度条显示，调用 `proceed()`（必须，且最后调用）。

   ```java
   download.url(PACKAGE_URL).setSavePath(new File(fileParent, "sample.apk")).setProgress(progressBar).proceed();
   ```

5. 在`暂停`按钮响应中调用`pause()`方法。

   ```java
   download.pause();
   ```

6. 在`继续下载`按钮响应中调用`goOn()`方法。

   ```java
   download.goOn();
   ```

7. 在`AndroidManifest.xml`中加入权限。

   ```xml
       <uses-permission android:name="android.permission.INTERNET" />
   	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
       <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
       <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   ```

## 效果图

![效果图](https://raw.githubusercontent.com/LiLiKazine/SimpleDownloader_Demo/master/S70307-101952.jpg)

## 示例apk

[SimpleDownloader_Demo.apk](https://github.com/LiLiKazine/SimpleDownloader_Demo/raw/master/SimpleDownloader_Demo.apk)