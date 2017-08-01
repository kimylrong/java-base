package com.qiusuo.java.classloader;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @auth lirong
 * @date 2017/8/1
 */
public class CompilerAndClassLoader extends ClassLoader{
    // 来源于DemoApp的源码
    private static final String source_Code="package com.qiusuo.java.classloader;public class DemoApp {public void run() {System.out.println( \"just run, and run\" );}}";

    public CompilerAndClassLoader(){
        // 以下代码非常重要， class loader chain的保持
        super(CompilerAndClassLoader.class.getClassLoader());
    }

    public byte[] buildClassByte() throws IOException {
        // 创建临时文件
        String tempDirPath = System.getProperty("user.dir") + "/temp";
        File tempDir = new File(tempDirPath);
        if(!tempDir.exists()){
            tempDir.mkdir();
        }

        // java文件输出
        File javaFile = new File(tempDir, "DemoApp.java");
        FileWriter fileWriter = new FileWriter(javaFile);
        fileWriter.write(source_Code);
        fileWriter.flush();
        fileWriter.close();

        // 编译
        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager javaFileManager = javaCompiler.getStandardFileManager(null,
                null, null);
        Iterable<? extends JavaFileObject> iter = javaFileManager.getJavaFileObjects(javaFile);
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, javaFileManager, null,
                Arrays.asList("-d", "./temp"),
                null, iter);
        task.call();
        javaFileManager.close();

        // 输入class文件
        File classFile = new File(tempDir, "com/qiusuo/java/classloader/DemoApp.class");
        FileInputStream classFileReader = new FileInputStream(classFile);
        int size = classFileReader.available();
        byte[] buffer = new byte[size];
        classFileReader.read(buffer);
        return buffer;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = buildClassByte();
            return defineClass(name, data, 0, data.length);
        } catch (IOException e) {
            throw new ClassNotFoundException();
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        CompilerAndClassLoader loader = new CompilerAndClassLoader();
        Class clazz = loader.findClass("com.qiusuo.java.classloader.DemoApp");
        Method method = clazz.getMethod("run", null);
        Object god = clazz.newInstance();
        method.invoke(god, null);
        System.out.println("Perfect");
    }
}
