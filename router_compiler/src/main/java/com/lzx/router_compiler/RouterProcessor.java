package com.lzx.router_compiler;

import com.google.auto.service.AutoService;
import com.lzx.router_annotation.Router;
import com.lzx.router_annotation.bean.RouterBean;
import com.lzx.router_compiler.utils.ProcessorConfig;
import com.lzx.router_compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConfig.ROUTER_PACKAGE}) // 允许/支持的注解类型，让注解处理器处理
@SupportedSourceVersion(SourceVersion.RELEASE_8) // 指定JDK编译版本
@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE}) // 注解处理器接收的参数
public class RouterProcessor extends AbstractProcessor {


    private Elements elementTool; // 操作Element的工具类（类，函数，属性，其实都是Element）
    private Types typeTool; // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Messager messager;  // Message用来打印 日志相关信息
    private Filer filer;// 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private String options; // 各个模块传递过来的模块名
    private String aptPackage; // 各个模块传递过来的目录,存放apt生成的文件

    // Path缓存
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();

    // Group缓存
    // Map<"order", "Router$$Path$$order.class">
    private Map<String, String> mAllGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        elementTool = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeTool = processingEnv.getTypeUtils();

        options = processingEnv.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnv.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);

    }

    /**
     * @param annotations      支持处理注解的节点集合
     * @param roundEnv 当前或是之前的运行环境,可以通过该对象查找的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "NO @Router use");
            return false;
        }

        // 获取所有被@Router注解的元素集合
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Router.class);

        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE, "activityType：" + activityType);
        TypeMirror activityMirror = activityType.asType(); //Mirror 镜子 自描述 包含类信息

        for (Element element : elements) {
            String className =element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "@Router class：" + className);
            Router router = element.getAnnotation(Router.class);

            RouterBean routerBean = new RouterBean.Builder()
                    .addElement(element)
                    .addGroup(router.group())
                    .addPath(router.path())
                    .build();

            TypeMirror elementMirror = element.asType();
            if (typeTool.isSubtype(elementMirror, activityMirror)) {
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else {
                throw new RuntimeException("@Router use at class is not activity");
            }

            if (checkRouterPath(routerBean)){
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + routerBean.toString());
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());
                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@Router parameter error");
            }
        } //循环结束,path缓存RouterBean的path和group信息完善

        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.ROUTER_API_GROUP);
        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.ROUTER_API_PATH);

        try {
            createPathFile(pathType); // 生成 Path类
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "create path class error e:" + e.getMessage());
        }

        try {
            createGroupFile(groupType, pathType); // 生成 Group类
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "create group class error e:" + e.getMessage());
        }


        return false;
    }


    private void createPathFile(TypeElement pathType) throws IOException{
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return; //不用干活
        }

        //使用JavaPoet生成java文件
        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),         // Map
                ClassName.get(String.class),      // Map<String,
                ClassName.get(RouterBean.class)   // Map<String, RouterBean>
        );

        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
            //方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class) // @Override注解
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .returns(methodReturn); // 返回值
            // Map<String, RouterBean> pathMap = new HashMap<>();

            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),           // Map
                    ClassName.get(String.class),        // Map<String,
                    ClassName.get(RouterBean.class),    // Map<String, RouterBean>
                    ProcessorConfig.PATH_VAR1,          // Map<String, RouterBean> pathMap
                    ClassName.get(HashMap.class));     // Map<String, RouterBean> pathMap = new HashMap<>();

            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1, // pathMap.put
                        bean.getPath(), // "/app/MainActivity"
                        ClassName.get(RouterBean.class), // RouterBean
                        ClassName.get(RouterBean.TypeEnum.class), // RouterBean.Type
                        bean.getTypeEnum(), // 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) bean.getElement()), // MainActivity.class Main2Activity.class
                        bean.getPath(), // 路径
                        bean.getGroup() // 组名
                );
            }

            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey(); //Router$$Path$$xxxxxxx
            messager.printMessage(Diagnostic.Kind.NOTE, "test："+pathType);

            JavaFile.builder(aptPackage, // APT生成文件存放的路径
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(pathType)) // 实现RouterLoadPath接口  implements RouterPath==pathType
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                            .build() // JavaFile构建完成
                            .writeTo(filer); // 文件生成器开始生成类文件
            mAllGroupMap.put(entry.getKey(), finalClassName);
        }
    }


    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException{
        //缓存二 判断是否有需要生成的类文件
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) return;

        // 返回值 这一段 Map<String, Class<? extends ARouterPath>>
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),        // Map
                ClassName.get(String.class),    // Map<String,

                // Class<? extends ARouterPath>>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        // ? extends ARouterPath
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))) // ? extends ARouterLoadPath

                // 最终的：Map<String, Class<? extends ARouterPath>>
        );

        // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME) // 方法名
                .addAnnotation(Override.class) // 重写注解 @Override
                .addModifiers(Modifier.PUBLIC) // public修饰符
                .returns(methodReturns); // 方法返回值

        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),

                // Class<? extends ARouterPath>
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))), // ? extends ARouterPath
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        //	groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(), // order ,app
                    ClassName.get(aptPackage, entry.getValue()));
        }

        // return groupMap;
        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        // ARouter$$Group$$ + xxxxxxxxx
        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        // 生成类文件：ARouter$$Group$$app
        JavaFile.builder(aptPackage, // 包名
                TypeSpec.classBuilder(finalClassName) // 类名
                        .addSuperinterface(ClassName.get(groupType)) // 实现ARouterLoadGroup接口 implements ARouterGroup
                        .addModifiers(Modifier.PUBLIC) // public修饰符
                        .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                        .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件
    }

    /**
     * 校验@Router注解的值，如果group未填写就从必填项path中截取数据
     * @param bean 路由节点信息类
     */
    private final boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "path must be start with /");
            return false;
        }
        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "path must like /app/MainActivity");
            return false;
        }
        // 截取path中的group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "group are different");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }

        return true;
    }
}
