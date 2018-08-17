package com.sunxy.plugin.core.hook;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;

import com.sunxy.plugin.core.model.PluginInfo;
import com.sunxy.plugin.core.utils.ReflexUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/15 0015.
 */
public class HookManager {

    private static HookManager manager;
//    private Context context;
    private Application context;

    public static HookManager manager(){
        if (manager == null){
            synchronized (HookManager.class){
                if (manager == null){
                    manager = new HookManager();
                }
            }
        }
        return manager;
    }

    private HookManager(){}

    public void init(Application application){
        this.context = application;
    }

    /**
     * hook AMS 实现跳转时候伪装
     */
    public void hookAms() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Object IActivityManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Class<?> forName = Class.forName("android.app.ActivityManager");
            Field defaultField =  ReflexUtils.findField(forName, "IActivityManagerSingleton");
            IActivityManager = defaultField.get(null);
        }else{
            Class<?> forName = Class.forName("android.app.ActivityManagerNative");
            Field defaultField = ReflexUtils.findField(forName, "gDefault");
            //gDefault的变量值（IActivityManager）
            IActivityManager = defaultField.get(null);
        }

        //反射Singleton
        Class<?> forName2 = Class.forName("android.util.Singleton");
        Field instanceField = ReflexUtils.findField(forName2, "mInstance");
        //系统的iActivityManager对象
        Object iActivityManagerObject = instanceField.get(IActivityManager);
        //钩子
        Class<?> iActivityManagerIntercept = Class.forName("android.app.IActivityManager");
        //动态代理
        AmsInvocationHandler handler = new AmsInvocationHandler(context, iActivityManagerObject);
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{iActivityManagerIntercept},handler);

        instanceField.set(IActivityManager, proxy);
    }


    /**
     * hook ActivityThread 获取 mh 实现真正的跳转到目标。
     */
    public void hookActivityThread() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
        Field currentActivityThreadField = ReflexUtils.findField(ActivityThread, "sCurrentActivityThread");
        Object activityThreadObject = currentActivityThreadField.get(null);
        Field handlerField =ReflexUtils.findField(ActivityThread, "mH");
        //mH的变量
        Handler handlerObject = (Handler) handlerField.get(activityThreadObject);
        Field callbackField =ReflexUtils.findField(Handler.class, "mCallback");
        callbackField.set(handlerObject, new ActivityThreadHandlerCallback(handlerObject));
    }


    /**
     * 加载apk
     * @param apkPath apk文件路径
     */
    public PluginInfo loadApk(String apkPath) throws NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {

        PluginInfo pluginInfo = new PluginInfo();
        pluginInfo.setApkPath(apkPath);

        PackageInfo packageInfo = context.getPackageManager()
                .getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        pluginInfo.setPackageInfo(packageInfo);

        String cachePath = context.getExternalCacheDir().getAbsolutePath() + "/plugin/" +
                packageInfo.packageName + "/"+ packageInfo.versionCode;

        //方式一：通过合并插件apk文件中的dex到运行程序中，实现插件化的效果。缺点：内存溢出，加载class耗时。
//        mergeDexElement(pluginInfo, cachePath);

        //方式二：通过加载loadedApk的方式、
        addLoadApkFromPackageMap(pluginInfo, cachePath);

        //加载插件中的静态广播。
        loadApkReceiver(pluginInfo);

        //加载插件中的资源，
        loadApkResource(pluginInfo);

        return pluginInfo;
    }


    /**
     * 加载apk的广播
     */
    private void loadApkReceiver(String apkPath) throws ClassNotFoundException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, NoSuchFieldException, InvocationTargetException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageArchiveInfo(apkPath, PackageManager.GET_RECEIVERS);

        ActivityInfo[] receiverInfos = packageInfo.receivers;
        if (receiverInfos == null || receiverInfos.length == 0){
            return;
        }

        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Object packageParser = packageParserClass.newInstance();
        //该方法需要做兼容处理
        Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
        Object packageObj = parsePackageMethod.invoke(packageParser, new File(apkPath), 0);

        //这里的 packageObj 就是 PackageParser.Package 里面装载着清单文件中的信息。。。
        //
        //public final ArrayList<Permission> permissions = new ArrayList<Permission>(0);
        //public final ArrayList<PermissionGroup> permissionGroups = new ArrayList<PermissionGroup>(0);
        //public final ArrayList<Activity> activities = new ArrayList<Activity>(0);
        //public final ArrayList<Activity> receivers = new ArrayList<Activity>(0);
        //public final ArrayList<Provider> providers = new ArrayList<Provider>(0);
        //public final ArrayList<Service> services = new ArrayList<Service>(0);

        Field receiversField = packageObj.getClass().getDeclaredField("receivers");

        //获取 ArrayList<Activity> receivers  这里的Activity不是常用的，是一个保存解析消息的内部类
        List receivers = (List) receiversField.get(packageObj);

        //获取 Activity 中的 intents 属性/ <? extends IntentFilter>
        Class<?> componentClass = Class.forName("android.content.pm.PackageParser$Component");
        Field intentsField = componentClass.getDeclaredField("intents");
        Field className = componentClass.getDeclaredField("className");

        for (Object activity : receivers) {
            String broadcastReceiverName = (String) className.get(activity);
            BroadcastReceiver broadcastReceiver = (BroadcastReceiver) Class.forName(broadcastReceiverName).newInstance();
            List<? extends IntentFilter> intents = (List<? extends IntentFilter>) intentsField.get(activity);
            for (IntentFilter intentFilter : intents) {
                context.registerReceiver(broadcastReceiver, intentFilter);
            }
        }
    }

    private void loadApkReceiver(PluginInfo pluginInfo) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Field receiversField = ReflexUtils.findField(pluginInfo.getPackageObj(), "receivers");

        //获取 ArrayList<Activity> receivers  这里的Activity不是常用的，是一个保存解析消息的内部类
        List receivers = (List) receiversField.get(pluginInfo.getPackageObj());

        //获取 Activity 中的 intents 属性/ <? extends IntentFilter>
        Class<?> componentClass = Class.forName("android.content.pm.PackageParser$Component");
        Field intentsField = componentClass.getDeclaredField("intents");
        Field className = componentClass.getDeclaredField("className");

        for (Object activity : receivers) {
            String broadcastReceiverName = (String) className.get(activity);
            BroadcastReceiver broadcastReceiver = (BroadcastReceiver) pluginInfo.getClassLoader().loadClass(broadcastReceiverName).newInstance();
            List<? extends IntentFilter> intents = (List<? extends IntentFilter>) intentsField.get(activity);
            for (IntentFilter intentFilter : intents) {
                context.registerReceiver(broadcastReceiver, intentFilter);
            }
        }
    }


    /**
     * 加载apk的资源
     */
    public void loadApkResource(PluginInfo info) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        AssetManager assetManager = AssetManager.class.newInstance();
        Method addAssetPathMethod = ReflexUtils.findMethod(assetManager, "addAssetPath", String.class);
        addAssetPathMethod.invoke(assetManager, info.getApkPath());

        Method ensureStringBlocks = ReflexUtils.findMethod(assetManager, "ensureStringBlocks");
        ensureStringBlocks.invoke(assetManager);

        Resources supResources = context.getResources();
        Resources resources = new Resources(assetManager, supResources.getDisplayMetrics(), supResources.getConfiguration());

        info.setAssetManager(assetManager);
        info.setResources(resources);
    }


    /**
     * 通过合并dex数组 的方式实现插件化。
     */
    private void mergeDexElement(PluginInfo pluginInfo, String cachePath) throws NoSuchFieldException, IllegalAccessException {
        ClassLoader classLoader = context.getClassLoader();

        //1. 找到插件文件的dex数组
        DexClassLoader dexClassLoader = new DexClassLoader(pluginInfo.getApkPath(), createFolder(cachePath, "odex"),
                createFolder(cachePath, "lib"), classLoader);

        Field pathListField = ReflexUtils.findField(dexClassLoader, "pathList");
        Object myPathListObject = pathListField.get(dexClassLoader);

        //拿到baseClassLoader中的dexElements属性
        Field dexElementsFiled = ReflexUtils.findField(myPathListObject, "dexElements");

        //拿到插件apk中的element数组
        Object[] pluginElements = (Object[]) dexElementsFiled.get(myPathListObject);

        //2. 找到运行程序的element数组
        Field systemPathListField = ReflexUtils.findField(classLoader, "pathList");
        Object systemPathList = systemPathListField.get(classLoader);
        //取出pathList中的dexElement
        Object[] systemElements = (Object[]) dexElementsFiled.get(systemPathList);

        //3. 合并俩个数组
        Object[] newElements = (Object[]) Array.newInstance(systemElements.getClass().getComponentType(),
                systemElements.length + pluginElements.length);

        //先放插件中的，查找的时候就是先从插件中的dex进行查找。
        //System.arraycopy(pluginElements, 0, newElements, 0, pluginElements.length);
        //System.arraycopy(systemElements, 0, newElements, pluginElements.length, systemElements.length);
        //先放宿主中的，查找的时候就是先从宿主中的dex进行查找。
        System.arraycopy(systemElements, 0, newElements, 0, systemElements.length);
        System.arraycopy(pluginElements, 0, newElements, systemElements.length, pluginElements.length);

        //4. 替换
        dexElementsFiled.set(systemPathList, newElements);
    }

    /**
     * 通过往ActivityThread的 mPackages 里添加loadApk的方式实现插件化。。
     */
    private void addLoadApkFromPackageMap(PluginInfo pluginInfo, String cachePath)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException, InstantiationException {

        //1. 得到activityThread对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = ReflexUtils.findMethod(activityThreadClass, "currentActivityThread");
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);


        //2. 获取activityThread对象中的mPackages对象
        //  final ArrayMap<String, WeakReference<LoadedApk>> mPackages = new ArrayMap<>();
        Field mPackagesField = ReflexUtils.findField(activityThreadClass, "mPackages");
        Map mPackages = (Map) mPackagesField.get(currentActivityThread);


        //3. 将apk加载成loadedApk，添加到mPackages中
        // 。
        // 3.1 拿到ActivityThread中的getPackageInfoNoCheck方法。反射时优先拿public的方法。
        // public final LoadedApk getPackageInfoNoCheck(ApplicationInfo ai, CompatibilityInfo compatInfo)
        Class<?> compatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
        Method getPackageInfoNoCheckMethod = ReflexUtils.findMethod(activityThreadClass, "getPackageInfoNoCheck",
                ApplicationInfo.class, compatibilityInfoClass);

        // 3.2 获取3.1方法中需要的CompatibilityInfo对象
        Field defaultCompatibilityInfoField = ReflexUtils.findField(compatibilityInfoClass,
                "DEFAULT_COMPATIBILITY_INFO");
        Object defaultCompatibilityInfo = defaultCompatibilityInfoField.get(null);

        // 3.3 获取3.1方法中需要的 ApplicationInfo 对象
        ApplicationInfo applicationInfo = getApplicationInfo(pluginInfo);

        // 3.4 生成loadApk文件
        Object loadedApk = getPackageInfoNoCheckMethod.invoke(currentActivityThread, applicationInfo, defaultCompatibilityInfo);

        //4. 加工处理下loadedApk   最终目的  是要替换ClassLoader  不是替换LoaderApk
        Field mClassLoaderField = ReflexUtils.findField(loadedApk, "mClassLoader");
        DexClassLoader newClassLoader = new DexClassLoader(pluginInfo.getApkPath(),
                createFolder(cachePath, "odex"),
                createFolder(cachePath, "lib"), context.getClassLoader());
        mClassLoaderField.set(loadedApk, newClassLoader);
        //保存newClassLoader
        pluginInfo.setClassLoader(newClassLoader);

        //5. 把loadedApk放到map中
        WeakReference weakReference = new WeakReference<>(loadedApk);
        mPackages.put(applicationInfo.packageName, weakReference);

        //  这里还要hook PMS， 因为在PMS中会去根据包名检测apk是否安装。这里需要hook到PMS让他跳过检测
        hootPMS(currentActivityThread);

    }

    /**
     * 获取apk文件的 ApplicationInfo
     */
    private ApplicationInfo getApplicationInfo(PluginInfo pluginInfo) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        Object packageParser = packageParserClass.newInstance();
        //该方法需要做兼容处理
        Method parsePackageMethod = ReflexUtils.findMethod(packageParserClass, "parsePackage", File.class, int.class);
        Object packageObj = parsePackageMethod.invoke(packageParser, new File(pluginInfo.getApkPath()), 0);
        //保存 package 对象。这个很重要
        pluginInfo.setPackageObj(packageObj);

        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        Object defaultUserState = packageUserStateClass.newInstance();

        Class<?> userHandler = Class.forName("android.os.UserHandle");
        Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
        int userId = (int) getCallingUserIdMethod.invoke(null);

        // TODO 该方法需要做兼容处理
        //  ApplicationInfo generateApplicationInfo(Package p, int flags,PackageUserState state, int userId)
        Method generateApplicationInfoMethod = ReflexUtils.findMethod(packageParserClass, "generateApplicationInfo",
                packageObj.getClass(), int.class, packageUserStateClass, int.class );
        ApplicationInfo apkApplicationInfo = (ApplicationInfo) generateApplicationInfoMethod
                .invoke(packageParser, packageObj, 0, defaultUserState, context.getApplicationInfo().uid );
        apkApplicationInfo.sourceDir = pluginInfo.getApkPath();
        apkApplicationInfo.publicSourceDir = pluginInfo.getApkPath();

        return apkApplicationInfo;
    }


    /**
     * hook pms
     */
    private void hootPMS(Object currentActivityThread) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Field sPackageManagerField = ReflexUtils.findField(currentActivityThread, "sPackageManager");
        //sPackageManager 为IPackageManager类型，负责和pms直接交互
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        //创建代理，实现hook功能
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class[]{iPackageManagerInterface}, new IPackageManagerHandler(sPackageManager));

        sPackageManagerField.set(currentActivityThread, proxy);

    }


    /**
     * 创建文件夹
     * @param path          路径
     * @param folderName    文件夹名字
     * @return 文件夹路径
     */
    private String createFolder(String path, String folderName){
        String allPath = path + "/" + folderName;
        File file = new File(allPath);
        if (!file.exists()){
            file.mkdirs();
        }
        return allPath;
    }
}
