### 利用Hook技术实现插件化开发

##### 先说说activity启动流程：
 不管是startActivity还是startActivityForResult，最后都是调用到了ActivityThread
 中的IActivityManager，这个IActivityManager最终会把跳转动作交给AMS，AMS中会
 进行一系列的判断，比如：是否需要创建任务栈，目标是否在清单文件中注册了等等，检测后
 IActivityManager会把这个跳转动作通过ActivityThread的 mh 发送出去 what为：LAUNCH_ACTIVITY。
 mh在收到这个跳转任务后，会执行：

    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;
    r.packageInfo = getPackageInfoNoCheck(r.activityInfo.applicationInfo, r.compatInfo);
    handleLaunchActivity(r, null, "LAUNCH_ACTIVITY");

 其中getPackageInfoNoCheck方法获取到包名对应LoadedApk中的classloader，在该应用
 首次启动的时候，这个时候包名对应的LoadedApk为空，这个时候系统会去创建。。。
 handleLaunchActivity方法中回去调用performLaunchActivity方法。
 在 performLaunchActivity 方法中调用

    ContextImpl appContext = createBaseContextForActivity(r);
 创建activity的context,之后通过：

     java.lang.ClassLoader cl = appContext.getClassLoader();
     activity = mInstrumentation.newActivity(cl, component.getClassName(), r.intent);
 反射创建出activity。

    Application app = r.packageInfo.makeApplication(false, mInstrumentation);
 通过makeApplication方法获取LoadedApk的application信息。没有的话就去创建一个。
 这里就涉及到Application的创建过程了。之后调用：

    appContext.setOuterContext(activity);
    activity.attach(appContext, this, getInstrumentation(), r.token,
                            r.ident, app, r.intent, r.activityInfo, title, r.parent,
                            r.embeddedID, r.lastNonConfigurationInstances, config,
                            r.referrer, r.voiceInteractor, window, r.configCallback);
 在activity.attach()方法中回去调用activity的attachBaseContext方法，
 然后调用下面的方法执行activity的onCreate方法：

     mInstrumentation.callActivityOnCreate(activity, r.state);
     r.activity = activity;

然后调用activity的onStart()方法

     activity.performStart();

 然后调用activity的onRestoreInstanceState()方法和onPostCreate()方法

     mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state);
     ......
     mInstrumentation.callActivityOnPostCreate(activity, r.state);

 到这里performLaunchActivity就执行完了，回到了handleLaunchActivity中执行onResume()方法

     handleResumeActivity(r.token, false, r.isForward,!r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);
 之后判断前一个activity的状态并回调方法：

     if (!r.activity.mFinished && r.startsNotResumed) {
        performPauseActivityIfNeeded(r, reason);
        ......
     }
 判断了如果有上一个activity而且符合条件的话就去执行上一个activity的onPause()方法。
 onStop()方法应该是WMS取调用的：
 在activity中的方法：final void performStop(boolean preserveWindow)

##### Hook点：
  1. 在 IActivityManager 把跳转任务交给AMS之前，将intent里面的目标替换成代理
    的activity，绕过AMS的检测
  2. 在AMS检查完毕后交由Handler处理时候，把正在的跳转目标替换回来。
  3. 在创建activity的时候PMS中会去根据包名检测apk是否安装。需要hook到PMS让他跳过检测

##### 加载插件中的class：
 1. 将插件中的dex加载PathClassLoader中，缺点：占用内存大，加载类变慢，类重复问题等等。
 2. 将插件apk文件转换成系统的loadedApk，将loadedApk放到系统中，缺点：8.1系统上如果插件已经安装了加载的时候回报错















