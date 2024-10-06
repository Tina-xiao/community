# 第一章 开发社区首页

## 1.2搭建开发环境

创建maven项目

`mvn archetype:generate -DgroupId=com.mycompany.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false`

编译

`mvn compile`

重新编译

`mvn clean` //清理

`mvn compile`或

`mvn clean compile`

测试(test命令包含了compile，编译时包括test文件夹)

`mvn clean test`

重新构建/编译

`build project`或者 maven的lifecycle中点击

## 1.3 Spring入门

### Spring ioC

+ 控制反转，面向编程

+ web项目中，spring容器自动创建，扫描配置类（下列注解）及其子包下的类，放入容器装配bean，以下标注只有语义上的区别。每个bean都拥有名字，每个bean默认的名字是**类名首字母小写**，也可以自定义。bean是默认单例的，只被实例化一次。
  + @Controller（"a"） 处理请求
  + @Service 业务
  + @Component 通用
  + @Repository 数据库访问
  + @Configuration //和上面四个不同，需要结合@Bean进行装配

+ spring容器管理bean的作用范围

  ```java
  @Scope("prototype")//多实例，IOC容器启动创建的时候，并不会创建对象放在容器在容器当中，当你需要的时候，需要从容器当中取该对象的时候，就会创建。
  @Scope("singleton")//单实例 IOC容器启动的时候就会调用方法创建对象，以后每次获取都是从容器当中拿同一个对象（map当中）。
  @Scope("request")//同一个请求创建一个实例
  @Scope("session")//同一个session创建一个实例
  ```

+ 正式运行程序是运行**CommunityApplication**这个类(配置类，一运行以它为配置类来执行程序)，如果希望测试代码中也用这个配置类，也希望和正式环境用配置类相同，启用这个类作为配置类，只需要加上注解`@ContextConfiguration(classes = Application.class)`

+ 哪个类想得到容器，只需实现接口和方法如下。

  ```java
  @RunWith(SpringRunner.class)
  @SpringBootTest
  @ContextConfiguration(classes = CommunityApplication.class)
  public class ApplicationTests implements ApplicationContextAware{
      
  private ApplicationContext applicationContext;//Spring容器
      
  @Autowired    
  private User user;
      
  @Override
  public void setApplicationContext( ApplicationContext applicationContext) throws BeansException{
      this.applicationContext = applicationContext;
  }
      
  }
  ```

当实现类变化时，在希望的实现类bean上再加一个注解@Primary，此时被调用时这个bean拥有更高的优先级，会被优先装配，因为依赖于接口。

如果程序的某一块还想用低优先级的实现类，那怎么得到低实现类的bean呢？

```java
applicationContext.getBean(A.class);` //类型

applicationContext.getBean(name: "a", a.class); //指定类名和返回bean类型
```

测试类注解

```java
//表明Test测试类要使用注入的类，比如@Autowired注入的类，有了@RunWith(SpringRunner.class)这些类才能实例化到spring容器中，自动注入才能生效
@RunWith(SpringRunner.class)
//告诉Spring Boot启动一个完整的应用程序上下文，并加载所有的Spring Bean和配置。
@SpringBootTest
//启用这个类作为配置类
@ContextConfiguration(classes = CommunityApplication.class)
```

### 容器管理bean初始化和销毁的方法

![在这里插入图片描述](nowcoder.assets/b68291b3cab54e92abb2050f965c5b4a.png)



![!测试bean实例化](nowcoder.assets/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA55uP5oGv,size_20,color_FFFFFF,t_70,g_se,x_16-17254574079664.png)

### Spring容器管理第三方bean（别人写好的封装在jar包的类）

（以SimpleDateFormat为例）

`@Configuration`  在类级别上添加了 @Configuration 注解，表明这个类用于配置 Spring Bean 的创建和管理。配置类可以包含多个用于定义 Bean 的方法，这些方法使用 @Bean 注解来标记，并且返回一个对象，该对象会被 Spring IoC 容器管理。

配置类可以通过多种方式创建和组装 Bean，包括使用其他配置类的 Bean、通过依赖注入注入其他 Bean、通过条件注解实现 Bean 的条件化创建等。配置类可以在 XML 配置文件、JavaConfig 类、注解等方式中进行定义，使得 Spring 的配置更加灵活和方便。

`@bean 方法返回的对象将被装配到容器`

![在这里插入图片描述](nowcoder.assets/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA55uP5oGv,size_20,color_FFFFFF,t_70,g_se,x_16.png)

因为时间可能全局经常调用，这样就可以实例化一次把对象装配到容器，然后反复用。

### 依赖注入DI

`@AutoWired` 根据类型进行自动装配的

`@Qualifier`按名称进行装配,配合AutoWired使用

![在这里插入图片描述](nowcoder.assets/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA55uP5oGv,size_20,color_FFFFFF,t_70,g_se,x_16-17254573974402.png)

## 1.4 Spring MVC入门

### request和response

request是代表HTTP请求信息的对象，response是代表HTTP响应信息的对象。

当浏览器发请求访问服务器中的某一个Servlet时，服务器将会调用Servlet中的service方法来处理请求。在调用service方法之前会创建出request和response对象。

其中request对象中封装了浏览器发送给服务器的请求信息（请求行、请求头、请求实体等），response对象中将会封装服务器要发送给浏览器的响应信息（状态行、响应头、响应实体），在service方法执行完后，服务器再将response中的数据取出，按照HTTP协议的格式发送给浏览器。

每次浏览器访问服务器，服务器在调用service方法处理请求之前都会创建request和response对象。（即，服务器每次处理请求都会创建request和response对象）。在请求处理完，响应结束时，服务器会销毁request和response对象。

#### 获取请求参数
所谓的请求参数，就是浏览器发送给服务器的数据（不区分请求方式），例如：通过表单向服务器提交的用户名、密码等，或者在超链接后面通过问号提交的数据，都是请求参数。

`http://localhost/day10_res/RequestDemo1?user=李四&like=篮球&like=足球`
##### 如何获取请求参数？

浏览器向服务器传参有两种方式，一是在通过get请求，在路径后加问号携带参数，如/xxx?id=1。另一种是通过post请求，在request请求体中携带表单中的参数，这种参数在路径上是看不到的。这两种方式所传的参数，在服务端都可以通过request.getParameter(参数名)这样的方式来获取。而@RequestParam注解，就相当于是request.getParameter()，是从request对象中获取参数的。

1. request.getParameter(String paramName)

+ 根据请求参数的名字获取对应的参数值，返回值是一个字符串；
+ 如果一个参数有多个值，该方法只会返回第一个值。
+ 如果获取的是一个不存在的参数，返回值为null

2. request.getParameterValues(String paramName)

+ 根据请求参数的名字获取该名字对应的**所有参数值组成的数组**，返回值是一个字符串数组，其中包含了这个参数名对应的所有参数
+ 如果获取的是一个不存在的参数，返回值为null

### 数据请求类型

当请求接口时，常见的数据请求类型有以下几种：

GET请求：用于从服务器获取数据。GET请求将参数附加在URL中，以便将请求发送到服务器，并返回相应的数据。这是最常见的请求类型，适用于读取资源和查询数据。

POST请求：用于向服务器提交数据。POST请求将参数包含在请求主体中，适合用于创建新资源、提交表单数据或执行某些操作。

PUT请求：用于向服务器更新资源。PUT请求将更新的数据发送到特定URL，用于替换指定资源的全部内容。

DELETE请求：用于从服务器删除资源。DELETE请求将删除指定URL上的资源。

PATCH请求：用于对资源进行局部更新。PATCH请求类似于PUT请求，但只更新资源的一部分。

OPTIONS请求：用于获取服务器支持的HTTP方法和资源的相关信息。OPTIONS请求可用于客户端与服务器之间的握手过程，了解服务器所支持的方法和功能。

HEAD请求：类似于GET请求，但只返回响应头，不返回实体内容。HEAD请求通常用于获取资源的元信息，如文件大小、修改日期等。

TRACE请求：用于回显服务器收到的请求。TRACE请求可用于测试和诊断，以检查请求在服务器端的处理情况。

CONNECT请求：用于建立与代理服务器的隧道连接，通常用于进行安全的SSL/TLS加密通信。

##### GET请求

1. 参数拼在?后

```java
//参数拼在?后
//   student?name="张三"&age=23
    @RequestMapping(path = "student",method = RequestMethod.GET)
    @ResponseBody //需要返回值
//可以不使用@@RequestParam,只需要参数名完全一致，如果不一致，可以使用该标注指定，requied=false非必需(如果没有可以不传),default默认值
     public void getStudent(@RequestParam(name="name",required = false,defaultValue = "李四") String name,int age){}

}
```

2. 参数隐藏在路径里如何获取

```java
//    参数隐藏在路径里如何获取
//    student/107
    @RequestMapping(path="student/{id}",method = RequestMethod.GET)
    @ResponseBody
    public void getStudentId(@PathVariable("id") int id){
        System.out.println(id);
}
```

在html中，通常把form标签称为表单
表单不会对页面样式产生任何影响，但它会影响页面的行为
它可以将表单中的数据，使用指定的请求方式（get或post），提交到指定的服务器地址

##### Post请求

提交数据一般不用get用post

下图的action是数据提交的路径,一定要加 / ，不加就是相对地址，会另当前的地址+相对地址，从而找不到报错。

![](nowcoder.assets/image-20231112151308396.png)

```java
    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    //一般参数名称对应上即可，当然也可以用@RequestParam注解标注别名
    public String saveStudent(@RequestParam(name="name")String name1,int age){
        return "已保存"+name+age+"岁";
    }
```

#### Spring MVC 路径前加斜杠(/)和不加斜杠的区别？

 请求路径分为**前端路径和后端路径**，在jsp页面中的路径都是前端路径，如果在.xml 中的路径可以说是后端路径。

请求路径分为相对路径和绝对路径：

比如：  http://localhost:8080/springmvc/test/hello 就是一个绝对路径(完整路径)，能够准确的定位一个资源

比如：/test/hello  或者  test/hello 为相对路径，他们会依赖一个其他路径最为参考路径

你仔细看的话会发现： 同样是相对路径为什么前面是否有斜杠(/)

如果路径解析在前台，根路径为http://localhost:8080/
如果路径解析在后台，根路径为http://localhost:8080/项目名/

```java
//配置项目访问路径
server.servlet.context-path=/community
```

相对路径不带斜杠时，开头的路径表示该路径为**当前资源路径**的一个子路径

相对路径带斜杠时，开头路径表示该路径为**根路径**的一个子路径

例:前端路径

```java
<form method="post" action="/community/post"> //localhost:8080/community/post
<form method="post" action="community/post"> //localhost:8080/community/community/post
//当前资源路径为localhost:8080/community + community/post，所以出现错误
```

#### html响应

不加@ResponseBody默认返回html对象，@ResponseBody注解可以用于同步请求和异步请求中。当你在控制器（Controller）方法上使用@ResponseBody注解时，Spring MVC会将方法的返回值作为响应体（Response Body）直接写入HTTP响应中，而不会通过视图解析器（View Resolver）解析成视图（View）。

1.  封装到ModelAndView对象

在Spring MVC中，要想跳转页面，需要使用ModelAndView对象，来实现【数据和页面的绑定】

ModelAndView的addObject()方法：

          ● 该方法用于设置【前端页面要显示的数据是什么】；
    
          ● 该方法的参数：可以是任何一个有效的Java对象；
    
          ● 该方法默认把对象，存放在当前请求中的；

```java
//响应html数据
@RequestMapping(path = "/teacher", method = RequestMethod.GET)
public ModelAndView getTeacher(){
    ModelAndView mav = new ModelAndView();
    //模型
    mav.addObject("name","林正顺");
    mav.addObject("age",30);
    //视图
    mav.setViewName("/view");
    //setViewName(“/view.html”)方法来确定跳转的页面；
    return mav;
}
```

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Teacher</title>
</head>
<body>
    <!--$符号取值-->
    <p th:text="${name}"></p>
    <p th:text="${age}"></p>
</body>
</html>
```

[Teacher](http://localhost:8080/community/A/teacher)

2. 拆开Model和View(更便捷)

```java
@RequestMapping( path = "/school",method = RequestMethod.GET)
//如果返回类型是String，那么返回的是View路径
//Model是bean,dispacherSevlet可以用
public String getSchool(Model model){
//model为DispacherServlet检测到代码中的Model后自动创建
    model.addAttribute("name","张三");
    return "/view";
}
```

[School](http://localhost:8080/community/A/school)

**响应json数据（异步请求）**

```java
//响应json数据
//Java对象->JOSN字符串->JS对象
@RequestMapping(path = "/emp",method = RequestMethod.GET)
@ResponseBody
public Map<String, Object> getEmp(){
    Map<String,Object> emp = new HashMap<>();
    emp.put("name","张三");
    emp.put("age",23);
    emp.put("phone", 112121);
    return emp;
}
```

[localhost:8080/community/A/emp](http://localhost:8080/community/A/emp)

```json
{
    "phone": 112121,
    "name": "张三",
    "age": 23
}
```



返回多个员工

```java
@RequestMapping(path = "/emps",method = RequestMethod.GET)
@ResponseBody
public List<Map<String, Object>> getEmps(){
    List<Map<String,Object>> list = new ArrayList();
    Map<String,Object> emp = new HashMap<>();
    emp.put("name","张三");
    emp.put("age",23);
    emp.put("phone", 112121);
    list.add(emp);

    emp.clear();
    emp.put("name","李四");
    emp.put("age",25);
    emp.put("phone", 121565521);
    list.add(emp);

    return list;
}
```

[localhost:8080/community/A/emps](http://localhost:8080/community/A/emps)

```json
[
    {
        "phone": 121565521,
        "name": "李四",
        "age": 25
    },
    {
        "phone": 121565521,
        "name": "李四",
        "age": 25
    }
]
```

## 1.5  MyBatis入门

![image-20231113164511235](nowcoder.assets/image-20231113164511235.png)

只需要写DAO接口，不需要写实现类，底层自动实现，前提是把所依赖的sql写好。

导入maven依赖以及配置信息

```properties
#配置是清除缓存，实现热部署。也就是修改了html后不用重启，刷新页面就能看到效果。
spring.thymeleaf.cache=false

#DataSourceProperties
# mysql
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/ 你的数据库名字 ?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone = GMT
spring.datasource.username=数据库账号
spring.datasource.password=数据库密码
#连接池
spring.datasource.type=com.zaxxer.hikari.HikariDataSource
spring.datasource.hikari.maximum-pool-size=15
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=30000
#MyBatisProperties
#mapper-locations是一个定义mapper接口位置的属性，在xxx.yml或xxx.properties下配置，作用是实现mapper接口配置
#当mapper接口和mapper接口对应的配置文件在命名上相同 、所在的路径相同，则mapper-locations可以不用配置，配置也不会生效UserMapper.java -> UserMapper.xml
mybatis.mapper-locations=classpath:mapper/*.xml
#声明实体类所对应的包，配置好mybatis后，我们在mapper.xml映射文件中不需要写出实体类的完整路径，只需要写出类名即可
mybatis.type-aliases-package=com.nju.community.entity
#自动生成主键，id自增长
mybatis.configuration.use-generated-keys=true
#让表user_name和驼峰命名userName自动匹配
mybatis.configuration.map-underscore-to-camel-case=true
#logger
#把com.nju.community这个包的级别设为debug，这样控制台可以看到预编译的sql语句
logging.level.com.nju.community=debug
```

###### 什么是classpath？

​	**classpath**，顾名思义，就是指 .class 文件所在的路径。.class 文件由 .java 文件编译后产生，我们用 java 命令执行.class 文件时，需要指定 jvm 去哪搜索目标 class 文件，而指定去哪搜索就是 classpath 的含义，即：**`classpath`是JVM用到的一个环境变量，它用来指示JVM如何搜索`class`。**

![image-20231113171526492](nowcoder.assets/image-20231113171526492.png)

项目编译之后，classes所在位置就是classpath。

### mybatis.mapper-locations和@MapperScan的区别

1. mybatis.mapper-locations在SpringBoot配置文件中使用，作用是扫描Mapper接口对应的XML文件。Mapper.java->Mapper.xml

2. @MapperScan写在SpringBoot的启动类上（xxxApplication.java），作用是扫描Mapper接口类，并生成对应的实现类。启动类->Mapper

+ 实体类建立和表的映射。

![image-20231113185209657](nowcoder.assets/image-20231113185209657.png)

+ 书写mapper接口

```java
@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

}
```

![image-20231113185917514](nowcoder.assets/image-20231113185917514.png)

+ 配置相应的xml文件

全限定类名（Fully Qualified Class Name）是指一个Java类的完全限定名，包括包名和类名。

在Java中，类名必须是唯一的，而包可以有相同的名称。因此，使用全限定类名可以确保唯一地识别一个类。

namespace用来对应mapper和mapper.xml，这里要使用全限定类名。 

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.nju.community.dao.UserMapper">
    <!--id是函数名，返回值类型和参数类型如果不是Java自带类型那么识别不了，需要声明-->
    <select id="selectById" resultType="User" >
        select * from user where id = #{id}
    </select>
   <!--KeyProperty设置主键是哪个属性,user后面对应表的列名，value后面对应entity属性名--> 
    <insert id="insertUser" parameterType="User" KeyProperty="id">
        insert into user (<include refid="fullField"></include>)
        values(#{id},#{name},#{status},#{type}...)
    </insert>
    
    <update id="updateStatus">
        update user set status= #{status} where id= #{id}
    </update>
    
    
</mapper>
```
	###### 在Mapper.xml中写复杂的动态SQL语句

+ 条件查询

```xml
<!--表的全字段名，便于引用-->
<sql id="fullField">id, name, status, type, date</sql>

<!--条件查询-->
<select id="selectEssay" resultType="结果需要封装的javaBean对象的全类名，如文章对象">
    select  <!--id, name, status, type, date-->
        <include refid="fullField"/>
    from 表名
    <where>
    
        <if test="status != null">
            and status=1
        </if>

        <if test="type != null">
            and type=#{type}
        </if>

        <if test="dateStart != null and dateEnd != null">
            and date bewteen #{dateStart}, #{dateEnd}
        </if>

    </where>
</select>
```

+ 批量添加

```xml
<!--批量添加学生-->
<insert id="insertStudents">
    insert into 学生表名(teacher_id,name,age...) values
    <foreach collection="students" separator="," item="s">
        (#{s.teacher_id},#{s.name},#{s.age}...)
    </foreach>
</insert>
```

+ 批量删除

```xml
<!--根据ID批量删除文章-->
<delete id="deleteEssaysById">
    delete from 表名 where id in
    <foreach collection="ids" item="id" separator="," open="(" close=")">
        #{id}
    </foreach>
</delete>
```

+ 更新数据

```xml
<!--更新文章-->
<update id="updateEssay">
    update 表名
    <set>
        <if test="name != null">name = #{name},</if>
        <if test="type != null">type = #{type},</if>
        <if test="status != null">status = #{status},</if>
        ....
    </set>
    where id = #{id}
</update>
```
## 1.6 开发社区首页

```xml
    <sql id="selectFields">
        id,user_id,title,content,type,status,create_time,comment_count,score
    </sql>
	<select id="selectDiscussPosts" resultType="com.nju.community.entity.DiscussPost">
         select <include refid="selectFields"></include>
         from discuss_post
         where status!=2
         <if test="userId!=0">
             and user_id =#{userId}
         </if>
        <!--按type,create_time降序排序-->
        order by type desc, create_time desc 
        <!--为了后续分页需求，limit用于返回固定数量的结果，offset是初始偏移量，limit是每页信息数-->
        limit #{offset},#{limit}
  	 </select>
```

##### Thymeleaf简单运用

```html
<link rel="stylesheet" thref="/css/global.css" />    相对路径容易有歧义,改进成下面这行。绝对路径不用改。
<link rel="stylesheet" th:href="@{/css/global.css}" /> 引用文件需要通过thymeleaf模板去找，去static下找

<!--每次foreach循环得到discussPosts之一 ，名字叫map-->
<li class="media pb-3 pt-3 mb-3 border-bottom" th:each="map:${discussPosts}">
```

+ th:action

定义后台控制器的路径，类似<form>标签的 action 属性，主要结合 URL 表达式,获取动态变量

```html
<form id="login" th:action="@{/login}" th:method="post">......</form>
```

+ th:method

设置请求方法

```html
<form id="login" th:action="@{/login}" th:method="post">......</form>
```

+ th:href

定义超链接，主要结合 URL 表达式,获取动态变量

```html
<div style="margin-left: 350px">
 <p>链接表达式</p>
 <p>链接到绝对地址</p>
 <a th:href="@{http://www.baidu.com}">百度</a>
 <br/>
 <br/>
 <p>链接到相对地址</p>
 <a th:href="@{/query/student}">相对地址没有传参数</a>
 <br/>
 <br/>
 <p>链接到相对地址,传参数方式 1</p>
 <a th:href="@{'/query/student?id='+${stuId}}">相对地址传参数方式1</a>
 <br/>
 <br/>
 <p>链接到相对地址,传参数方式 2,推荐方式</p>
 <!-- /find/school?id=1&name=lisi -->
 <a th:href="@{/find/school(id=${stuId},name='lisi')}">相对地址传参数方式 2</a>
</div>
<!-- href="#"是一种临时链接的写法，这样写就是说这个链接目前不可用，点击了也不会有作用，还是会跳转到本页，当#被有效链接替换才会起作用 -->
```

+ th:src

用于外部资源引入，比如<script>标签的 src 属性，<img>标签的 src 属性，常与@{}表达式
结合使用，在 SpringBoot 项目的静态资源都放到 resources 的 static 目录下，放到 static 路径
下的内容，写路径时不需要写上 static

```html
<script type="text/javascript" th:src="@{/js/jquery-3.4.1.js}"></script>
<!--写的·1,实际上调用的get方法,实际上为map.getUser(),user.getHeaderUrl()-->
<img th:src="${map.user.headerUrl}" class="mr-4 rounded-circle" alt="用户头像" style="width:50px;height:50px;">
```

+ th:text

用于文本的显示，该属性显示的文本在标签体中，如果是文本框，数据会在文本框外显示，
要想显示在文本框内，使用 th:value。th:utext可以显示转义字符，例如&lt显示成<.

```html
<input type="text" id="realName" name="reaName" th:text="${realName}">
```

+ th:style

设置样式
```html
<a th:onclick="'fun1('+${user.id}+')'" th:style="'color:red'">点击我</a>
```

+ th:each

这个属性非常常用，比如从后台传来一个对象集合那么就可以使用此属性遍历输出，它与JSTL 中的<c: forEach>类似，此属性既可以循环遍历集合，也可以循环遍历数组及 Map.

```html
<!--每次foreach循环得到discussPosts之一 ，名字叫map-->
<li class="media pb-3 pt-3 mb-3 border-bottom" th:each="map:${discussPosts}">
```

## 1.7项目调试技巧

Logger

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTests {

    private static final Logger logger = LoggerFactory.getLogger(LoggerTests.class);

    @Test
    public void testLogger(){
        System.out.println(logger.getName());
        logger.debug("debug log");
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");
    }

}
```

```properties
#logger简单配置
#高级别屏蔽低级别logger
logging.level.com.nju.community=warn 
logging.file.name= D:/JetBrains/IntelliJ IDEA 2022.1.4/workspace/community/logger/community.log
```

如果想复杂配置logger，比如warn级别的存放在一个文件夹，debug级别存放在一个文件夹，那么用以下方法，xml配置。需起名logback-spring.xml , 并放在resources目录下，自动识别。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <contextName>community</contextName>
    <!--文件存放目录-->
    <property name="LOG_PATH" value="D:/work/data"/>
    <!--子项目名-->
    <property name="APPDIR" value="community"/>

    <!-- error file -->
    <appender name="FILE_ERROR" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APPDIR}/log_error.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APPDIR}/error/log-error-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <!--一个文件最多存5MB-->
                <maxFileSize>5MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <!--最多存储30天-->
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <!--追加存储非覆盖-->
        <append>true</append>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
         <!--日志输出格式，日期 级别 线程 类 文件：行数 内容 换行-->
            <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
        <!--过滤器，什么级别的日志能被该组件处理，只处理error级别-->
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>error</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- warn file -->
    <appender name="FILE_WARN" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APPDIR}/log_warn.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APPDIR}/warn/log-warn-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>5MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <append>true</append>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>warn</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- info file -->
    <appender name="FILE_INFO" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/${APPDIR}/log_info.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/${APPDIR}/info/log-info-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>5MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <append>true</append>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>info</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
    </appender>

    <!-- console -->
    <!--打印到控制台-->
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d %level [%thread] %logger{10} [%file:%line] %msg%n</pattern>
            <charset>utf-8</charset>
        </encoder>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>debug</level>
        </filter>
    </appender>

    <logger name="com.nowcoder.community" level="debug"/>

    <root level="info">
        <appender-ref ref="FILE_ERROR"/>
        <appender-ref ref="FILE_WARN"/>
        <appender-ref ref="FILE_INFO"/>
        <appender-ref ref="STDOUT"/>
    </root>

</configuration>
```

## 1.8版本控制

##### 1.8.1git原理

![image-20231122172200796](nowcoder.assets/image-20231122172200796.png)

![image-20231122173437956](assets\image-20231122173437956.png)

![image-20231122173531364](nowcoder.assets/image-20231122173531364.png)

![image-20231122173642006](nowcoder.assets/image-20231122173642006.png)

git add . 其中.代表把所有添加进去，也可以用*替代

![image-20231122173714274](nowcoder.assets/image-20231122173714274.png)

##### 1.8.2 git项目搭建

![image-20231122174007417](nowcoder.assets/image-20231122174007417.png)

git bash here-> git init 创建本地仓库 或者 git clone [url] http://...克隆远程仓库

##### 1.8.3 git 文件操作

文件四种状态

![image-20231122174539428](nowcoder.assets/image-20231122174539428.png)

![image-20231122174901100](assets\image-20231122174901100.png)



###### 忽略文件

```.gitignore
*.class
*.log
*.lock

# Package Files #
*.jar
*.war
*.ear
target/

# idea
.idea/
*.iml/

*velocity.log*

### STS ###
.apt_generated
.factorypath
.springBeans

### IntelliJ IDEA ###
*.iml
*.ipr
*.iws
.idea
.classpath
.project
.settings/
bin/

*.log
tem/

#rebel
*rebel.xml*
```

![image-20231122175135085](nowcoder.assets/image-20231122175135085.png)

##### 1.8.4 github设置

设置本机绑定ssh公钥，实现免密登录（否则每次push到远程仓库都要输密码）

```bash 
#进入 C:\Users\lenovo\.ssh 目录
#生成公钥
ssh-kengen
#使用ras加密算法生成公钥
ssh -keygen -t -rsa 
```

如下生成一个公钥一个私钥。

![image-20231122191538101](nowcoder.assets/image-20231122191538101.png)

将public key添加到github账户，将github账户与本机绑定。

![image-20231122192011889](nowcoder.assets/image-20231122192011889.png)

使用github创建仓库，克隆到本地。

###### 解决用ssh-key后仍须输入密码的问题

1. 使用ssh方式克隆

```bash
git clone git@gitee.com:Name/project.git
git clone git@github.com:用户名/仓库名.git
```

就是说，在项目克隆/下载处，选择ssh方式的下载地址

2. 如果你已经用https方式克隆了仓库，不必删除仓库重新克隆，只需将当前项目中的 .git/config文件中的

```bash
url = https://gitee.com/Name/project.git
修改为
url = git@gitee.com:Name/project.git
```

##### 1.8.5 git操作

1. 新建项目，绑定git

   绑定方法1：将clone到本地的所有文件移动到当前项目下，自动绑定git![image-20231122194701109](nowcoder.assets/image-20231122194701109.png)

   绑定方法2：

   ```bash
   # 关联远程仓库
   git remote add origin [ssh]复制的仓库地址
   ```

2. 加入暂存区

   ```bash
   #给代码某个版本打标签,方便查找
   git tag v1.0.0
   
   #新建仓库
   git init
   git init [本地仓库名]
   
   #加入暂存区,这里的*是指把所有changes加入暂存区，而不是项目的所有文件
   git add .
   git add [file]
   
   #删除工作区文件，并将这次删除放入暂存区
   git  rm [file1] [file2]
   
   #停止追踪指定文件，但该文件保留在工作区
   git rm --cached [file]
   
   #改名文件，并将改名放入暂存区
   git mv [file-original] [file-renamed]
   ```

   

3. 提交

```bash
#提交暂存区到仓库区,一定要加-m，否则会弹出vim
git commit -m "提交信息"

#提交指定文件到仓库区
git commit [file1] [file2] ... -m "提交信息"

#提交工作区自上次commit之后的变化，直接到仓库区
git commit -a

#提交时显示所有differ信息
git commit -v
```

4. push到远程仓库

   ```bash
   #查看本地仓库所对应的远程仓库别名和地址
   git remote -v
   
   #把本地仓库的main分支推送给远程仓库的main分支，如果相同，可以写一个
   git push -u origin master
   git push -u origin master main:main
   git push -u origin master main
   
   git push <远程仓库别名> <本地分支名>:<远程分支名>
   git pull <远程仓库别名> <远程分支名>:<本地分支名>
   #即是将本地的master分支推送到远程仓库origin上的对应master分支。origin 是远程仓库别名（默认都是这个），第一个master是本地分支名，第二个master是远程分支名。
   git push origin master:master
   git pull origin master:master
   
   #如果远程分支被省略，则表示将本地分支推送到与之存在追踪关系的远程分支（通常两者同名），如果该远程分支不存在，则会被新建
   git push origin master（常用！）
   
   #如果当前分支只有一个远程分支，那么主机名都可以省略，形如 git push，可以使用git branch -r ，查看远程的分支名
   git push
   
   #链接远程分支
   git remote add origin [ssh]git@github.com:你的github用户名/你的github仓库名.git
   
   ```

5. 回退

   ```bash
   #查看提交记录
   git log
   
   #查看简洁提交记录，只有id和名字
   git log --online
   
   #回退到某一版本，保存工作区和暂存区内容
   git reset --soft id
   #丢弃工作区和暂存区内容
   git reset --hard id
   #只保留工作区内容，为默认参数
   git reset --mixed id
   ```

##### 1.8.6 git分支

master 主分支

dev 开发用

后面括号的蓝色字是当前分支

![image-20231122201947310](nowcoder.assets/image-20231122201947310.png)

```bash
#列出所有本地分支
git branch

#列出所有远程分支
git branch -r

#列出所有分支
git branch -a

#删除远程分支
git push origin --delete [branch-name]

#新建一个本地分支，但仍停留在当前分支,（分支名称必须和远程分支的名称相同）
git branch [branch-name]

#新建一个本地分支，并切换到该分支
git checkout -b [branch-name]

#切换到这个分支
git switch [branch-name]
git checkout [branch-name]

#本地与远程分支同步 ,origin代表远程主机，若没有同名远程分支自动创建远程分支
git pull origin [branch-name]

#合并指定分支branch到当前分支
git merge [branch]

#删除分支
git branch -d [branch-name]

#只有当该分支的修改已经合并到其他分支时，才能被安全地删除。如果分支的修改尚未合并，可以使用强制删除的命令：
git branch -D [branch-name]

#删除远程分支
git push origin --delete [branch-name]
git branch -dr [remote/branch]

#远程分支拉到本地，localbranchname是本地分支的名称，origin是远程仓库的名称，remotebranchname是远程分支的名称。
git checkout -b localbranchname origin/remotebranchname

#将本地分支推送到远程仓库
git push origin localbranchname:remotebranchname
```

多个分支并行执行，不冲突，同时存在多个版本

![image-20231122203149104](nowcoder.assets/image-20231122203149104.png)

![image-20231122203455625](nowcoder.assets/image-20231122203455625.png)

问题:This branch is 1 commit ahead, 1 commit behind main.

+ 你本地commit了代码，没有push，就是超前。
  remote上有更新，本地没有pull，就是落后。

问题：git fatal: destination path ‘/test’ already exists and is not an empty directory.解决
把本地文件上传到GitHub上时，出现git fatal: destination path ‘/test’ already exists and is not an empty directory错误。网上说的需要删除本地文件.git,或者删除本地文件夹。
其实只需要
1.进入文件目录
2.初始化 git init
3.添加远程仓库地址 git remote add origin (address)
4.添加本地代码 git add 项目名
5.提交本地代码 git commit -m"描述"
6.提交到远程库 git push origin master

![image-20231115163332129](nowcoder.assets/image-20231115163332129.png)

# 第二章 开发社区登录模块

## 2.1 发送邮件

![image-20231115163737937](nowcoder.assets/image-20231115163737937.png)

##### 切屏快捷键

ctrl+tab 来回切换桌面

alt+tab 来回切换程序

ctrl+win+左/右 切换桌面

##### SpringEL表达式中的 ${}和#{}的区别

在预编译中的处理是不一样的。优先使用#{}.

#{} 在预处理时，会把参数部分用一个占位符 ? 代替，变成如下的 sql 语句：select * from a where name = ? ，安全迅速，**转义字符**

而 ${} 则只是简单的字符串替换，在动态解析阶段，该 sql 语句会被解析成：select * from a where name = 'zhangsan' ，**不转义字符**串，有风险，同时存在**sql注入**，一般设置固定变量，例如字段名。

+ sql注入
  + 什么是 SQL 注入呢？比如 select * from user where id = ${value}
    value 应该是一个数值吧。然后如果对方传过来的是 001 and name = tom。这样不就相当于多加了一个条件嘛？把SQL语句直接写进来了。如果是攻击性的语句呢？001；drop table user，直接把表给删了。

1. #{}

#{} 功能比 ${} 功能更强大，强调的是把内容赋值给属性，示例:

+ 表示常量: #{‘1’} ,#{’ This is a Constant Str’}
+ 使用java代码new/instance of: 此方法只能是java.lang 下的类才可以省略包名 #{“new Spring(‘Hello World’)”}
+ 使用T(Type): 使用“T(Type)”来表示java.lang.Class实例，同样，只有java.lang 下的类才可以省略包名。此方法一般用来引用常量或静态方法 ,#{“T(Integer).MAX_VALUE”}
+ 变量: 使用“#bean_id”来获取,#{“beanId.field”}
+ 方法调用: #{“#abc.substring(0,1)”}
+ 运算符表达式: 算数表达式,比较表达式,逻辑表达式,赋值表达式,三目表达式,正则表达式
  判断空: #{“name?:’other’”}

@Value("#{}") 通常用来获取bean的属性，或者调用bean的某个方法。当然还有可以表示常量。

```java
@RestController 
@RequestMapping("/login") 
@Component 
public class LoginController { 
    
@Value("#{1}") 
private int number; //获取数字 1 

@Value("#{'Spring Expression Language'}") //获取字符串常量 
private String str; 

@Value("#{dataSource.url}") //获取bean的属性 
 private String jdbcUrl; 

@Autowired 
private DataSourceTransactionManager transactionManager; 

@RequestMapping("login") 
public String login(String name,String password) throws FileNotFoundException{ 
    System.out.println(number); 
    System.out.println(str); 
    System.out.println(jdbcUrl); 
    return "login"; 
  } 
}
```

2. ${}

**${}** 用于加载外部文件指定的Key值，常用在[xml](https://so.csdn.net/so/search?q=xml&spm=1001.2101.3001.7020)中，@Value(" ${key_value}")。

通过@Value("${}") 可以获取对应属性文件中定义的属性值。

### Spring Email

SpringBoot org.springframework.mail 包下提供了SimpleMailMessage、MimeMessageHelper和JavaMailSender API，借助这些API我们可以轻松实现邮件发送功能。

```java
SimpleMailMessage：用来发送简单的文本邮件；

MimeMessage：用来发送html邮件、带附件的邮件、有静态资源（图片）的邮件。
```

```properties
#MailProperties
#注意！password是smtp授权码不是登录密码
#spring.mail.host=smtp.qq.com
#spring.mail.port=465
#spring.mail.username=1597872231@qq.com
#spring.mail.password=tonjtgrquknxbabb
spring.mail.host=smtp.163.com
spring.mail.port=465
spring.mail.username=15863360234@163.com
spring.mail.password=DRDCAQNUJIONAIIF
spring.mail.protocol=smtps
spring.mail.properties.mail.smtp.ssl.enable=true
```

```java
@Component
public class MailClient {
    //logger以当前类命名
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            messageHelper.setFrom(from);
            messageHelper.setTo(to);
            //主题
            messageHelper.setSubject(subject);
//            支持html文本
            messageHelper.setText(content, true);
            javaMailSender.send(messageHelper.getMimeMessage());

        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
            System.out.println("发送邮件失败"+e.getMessage());
        }

    }

}
```

### 使用Thymeleaf模板引擎发送html邮件

##### thymeleaf介绍

使用模板的要点：
  页面主体结构固定，具体参数可变，尽可能让参数动态化，才能提高模板的复用性

简单说， Thymeleaf 是一个跟 Velocity、FreeMarker 类似的模板引擎，它可以完全替代 JSP 。相较与其他的模板引擎，它有如下三个极吸引人的特点：

1.Thymeleaf 在有网络和无网络的环境下皆可运行，即它可以让美工在浏览器查看页面的静态效果，也可以让程序员在服务器查看带数据的动态页面效果。这是由于它支持 html 原型，然后在 html 标签里增加额外的属性来达到模板+数据的展示方式。浏览器解释 html 时会忽略未定义的标签属性，所以 thymeleaf 的模板可以静态地运行；当有数据返回到页面时，Thymeleaf 标签会动态地替换掉静态内容，使页面动态显示。

2.Thymeleaf 开箱即用的特性。它提供标准和spring标准两种方言，可以直接套用模板实现JSTL、 OGNL表达式效果，避免每天套模板、该jstl、改标签的困扰。同时开发人员也可以扩展和创建自定义的方言。

3.Thymeleaf 提供spring标准方言和一个与 SpringMVC 完美集成的可选模块，可以快速的实现表单绑定、属性编辑器、国际化等功能。

##### 常用th标签都有那些？
![image-20231121234716940](assets\image-20231121234716940.png)
![image-20231121234735513](assets\image-20231121234735513.png)

##### 数据访问模式

1. 变量表达式

变量表达式即OGNL表达式或Spring EL表达式(在Spring术语中也叫model attributes)。如下所示：
`${session.user.name}`

```html
<span th:text="${book.author.name}">  
<li th:each="book : ${books}">  
```

2. 选择或星号表达式

选择表达式很像变量表达式，不过它们用一个预先选择的对象来代替上下文变量容器(map)来执行，如下：
`*{customer.name}`

被指定的object由th:object属性定义：

```html
 	<div th:object="${book}">  
      ...  
      <span th:text="*{title}">...</span>  
      ...  
    </div>  
```

3. 文字国际化表达式

文字国际化表达式允许我们从一个**外部文件**获取区域文字信息**(.properties)**，用Key索引Value，还可以提供一组参数(可选)

```
    #{main.title}  
    #{message.entrycreated(${entryId})}  
```

```html
 
    <table>  
      ...  
      <th th:text="#{header.address.city}">...</th>  
      <th th:text="#{header.address.country}">...</th>  
      ...  
    </table>  
```

4. URL表达式

URL表达式指的是把一个有用的上下文或回话信息添加到URL，这个过程经常被叫做URL重写。
`@{/order/list}`
URL还可以设置参数：
`@{/order/details(id=${orderId})}`
相对路径：
`@{../documents/report}`

```html
 <form th:action="@{/createOrder}">  
 <a href="main.html" th:href="@{/main}">
```

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>邮件示例</title>
</head>
<body>
    <p>
        欢迎你，<span style="color: red" th:text="${username}"></span>!
    </p>
</body>
</html>
```
```java
package com.nju.community;
import com.nju.community.util.MailClient;
import org.apache.naming.ContextAccessController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;


    @Autowired
    private TemplateEngine engine;

    @Test
    public void testHtmlMail(){
        Context context = new Context();
        context.setVariable("username","Tina");

        //模板引擎的作用是生成动态网页
        String content = engine.process("/mail/demo",context);
        System.out.println(content);
        mailClient.sendMail("1597872231@qq.com","html",content);
    }

}

```

## 2.2开发注册功能

#### 2.2.1 访问注册页面

**thymeleaf如何复用一段标签**

编写controller访问register.html

```java
@Controller
public class LoginController {
    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }
```

**处理页面静态资源的路径**

index.html的头部的代码在多个页面都有，直接利用进行复用，复用步骤如下：

（1）起名：用th:fragment="header"给index.html的头部起名为header

​	给index.html的头部起名为header


（2）复用：用th:replace="index::header"在register.html的头部用index的header进行替换


​	在register.html的头部用index的header进行替换

```html
<!--  复用头部这段代码，命名为header-->
<header class="bg-dark sticky-top" th:fragment="header">
 <!-- 头部 -->
<header class="bg-dark sticky-top" th:replace="index::header">
```

### 2.2.2 提交注册数据
导包commons-lang3——用于判断字符串、集合等常用数据空值的情况；

 ```xml
  <dependency>
             <groupId>org.apache.commons</groupId>
             <artifactId>commons-lang3</artifactId>
             <version>3.9</version>
         </dependency>
 ```

在application.properties配置网站域名，因为没有域名这里就是配置本机的ip地址，注册的过程要发邮件，邮件里要带激活链接，激活链接链接到配置的网站，链接在开发测试上线阶段不同所以要做成可配的。

community.path.domain是自定义的内容

community.path.domain=http://localhost:8080

写一个工具类，提供两个静态方法方便注册时使用，其一是生成随机字符串，其二是MD5加密
MD5算法加密：对于同一密码MD5加密后的结果相同、如果密码不加盐，对于简单密码，破解密码的人可直接在其数据库中查简单密码对应的加密结果。

编写service，因为注册时要发邮件，所有把MailClient和TemplatesEngine注入，同时注入域名和项目名；方法的返回值是map用于封装错误信息。

```java
public class UserService {
    @Autowired
    UserMapper userMapper;

    @Autowired
    MailClient mailClient;

    @Autowired
    TemplateEngine templateEngine;

    @Value("${pycommunity.path.domain}")
    String domain;
    @Value("${server.servlet.context-path}")
    String contextPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }
    public Map<String,Object> register(User user) {
        Map<String,Object> map = new HashMap<>();
        //空值处理
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","用户名不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwoMsg","密码不能为空!");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空!");
            return map;
        }
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null) {
            map.put("usernameMsg","该账号已存在!");
            return map;
        }
        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg","该邮箱已被注册!");
            return map;
        }
        //注册用户
        user.setSalt(PycommunityUtil.generateUUID().substring(0,5));
        user.setPassword(PycommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);//0表示普通用户
        user.setStatus(0);//0表示没有激活
        //设置激活码
        user.setActivationCode(PycommunityUtil.generateUUID());
        //nextInt(1001)产生[0，1000]的整数,设置头像路径
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1001)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);
        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //指定url为http://localhost:8080/项目路径/功能路径/用户id/激活码
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",process);
        return map;
    }

```

激活页面

```html
<!doctype html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <link rel="icon" href="https://static.nowcoder.com/images/logo_87_87.png"/>
    <title>牛客网-激活账号</title>
</head>
<body>
	<div>
		<p>
			<b th:text=""${email}>xxx@xxx.com</b>, 您好!
		</p>
		<p>
			您正在注册牛客网, 这是一封激活邮件, 请点击 
			<a th:href="${url}">此链接</a>,
			激活您的牛客账号!
		</p>
	</div>
</body>
</html>
```

编写controller，逻辑是注册成功跳转到首页，注册失败回到注册页面，激活成功跳转到登录页面

```java
  @Autowired
    private UserService userService;
  @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user) {
        //方法调用前，springmvc会自动实例化Model和User，同时把User注入到Model
        Map<String, Object> map = userService.register(user);
        if(map.isEmpty()) {
            model.addAttribute("msg","您已成功注册，请尽快去邮箱进行激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

```

确认密码是前端的逻辑，不用传到后台
如果注册失败回到注册页面，希望在表单项中仍保留有填过的内容，可以用th:value="${user==null?‘’:user.username}"完成
input框的class属性决定了错误信息是否显示	 

class="form-control is-invalid" //显示后面的报错样式，没有is-invalid不显示报错信息，有没有该样式得动态判断

th:class="|form-control ${usernameMsg!=null?'is-invalid':''}|"  ：两条竖线||表示form-control是固定的，后面拼接上动态的值。

```html

		<!-- 内容 -->
		<div class="main">
			<div class="container pl-5 pr-5 pt-3 pb-3 mt-3 mb-3">
				<h3 class="text-center text-info border-bottom pb-3">注&nbsp;&nbsp;册</h3>
				<form class="mt-5" method="post" th:action="@{/register}">
					<div class="form-group row">
						<label for="username" class="col-sm-2 col-form-label text-right">账号:</label>
						<div class="col-sm-10">
                            <!--id是前端css调用的标签，name是后端调用的标签-->
							<input type="text"
								   th:class="|form-control ${usernameMsg!=null?'is-invalid':''}|"
								   id="username" name="username"
								   th:value="${user==null?'':user.username}"
								   placeholder="请输入您的账号!" required>
							<div class="invalid-feedback" th:text="${usernameMsg}">
								该账号已存在!
							</div>
						</div>
					</div>
					<div class="form-group row mt-4">
						<label for="password" class="col-sm-2 col-form-label text-right">密码:</label>
						<div class="col-sm-10">
							<input type="password"
								   th:class="|form-control ${passwordMsg!=null?'is-invalid':''}|"
								   id="password" name="password"
								   th:value="${user==null?'':user.password}"
								   placeholder="请输入您的密码!" required>
							<div class="invalid-feedback" th:text="${passwordMsg}">
								密码长度不能小于8位!
							</div>							
						</div>
					</div>
					<div class="form-group row mt-4">
						<label for="confirm-password" class="col-sm-2 col-form-label text-right">确认密码:</label>
						<div class="col-sm-10">
							<input type="password" class="form-control" id="confirm-password"
								   th:value="${user==null?'':user.password}"
								   placeholder="请再次输入密码!" required>
							<div class="invalid-feedback">
								两次输入的密码不一致!
							</div>
						</div>
					</div>
					<div class="form-group row">
						<label for="email" class="col-sm-2 col-form-label text-right">邮箱:</label>
						<div class="col-sm-10">
							<input type="email"
								   th:class="|form-control ${emailMsg!=null?'is-invalid':''}|"
								   id="email" name="email"
								   th:value="${user==null?'':user.email}"
								   placeholder="请输入您的邮箱!" required>
							<div class="invalid-feedback" th:text="${emailMsg}">
								该邮箱已注册!
							</div>
						</div>
					</div>
					<div class="form-group row mt-4">
						<div class="col-sm-2"></div>
						<div class="col-sm-10 text-center">
							<button type="submit" class="btn btn-info text-white form-control">立即注册</button>
						</div>
					</div>
				</form>				
			</div>
		</div>

```

#### Tymeleaf的context和Controller中的model都可用来向前端传值，区别？

thymeleaf 的context，即提供数据的地方，基于web的context，即WebContext相对context增加 param,session,application变量，并且自动将request atttributes添加到context variable map，可以在模板直接访问。用于页面提供动态数据，且仅给th:标签使用。例：

```java
    Context context = new Context();
    context.setVariable("email",user.getEmail());
    //指定url为http://localhost:8080/项目路径/功能路径/用户id/激活码
    String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
    context.setVariable("url", url);
//将封装的context发送到此页面
    String process = templateEngine.process("/mail/activation", context);
```

```html
			<b th:text="${email}">xxx@xxx.com</b>, 您好!
			<a th:href="${url}">此链接</a>
```

model用来给页面name:标签对应的属性赋值。

```java
model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
model.addAttribute("target","/index");
```

```html
<p class="lead" th:text="${msg}">您的账号已经激活成功,可以正常使用了!</p>
   您也可以点此 <a id="target" th:href="@{${target}}" class="text-primary">链接</a>, 手动跳转!
```

执行顺序：

1. 前端填写注册信息之后点击button提交post请求,表单提交的地址为/register

   ```html 
   <form class="mt-5" method="post" th:action="@{/register}">
       <a class="nav-link" th:href="@{/register}">注册</a>
   ```

2. 调用了controller层的register函数,根据填写信息跳转到不同页面并携带错误信息model.

   ```java
      @RequestMapping(value = "/register",method = RequestMethod.POST)
       public String register(Model model,User user){
           Map<String,Object> map = userService.register(user);
           if(map==null|| map.isEmpty()){
               model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
               model.addAttribute("target","/index");
               return "/site/operate-result";
           } else {
               model.addAttribute("usernameMsg",map.get("usernameMsg"));
               model.addAttribute("passwordMsg",map.get("passwordMsg"));
               model.addAttribute("emailMsg",map.get("emailMsg"));
               return "/site/register";
           }
       }
   ```

   激活注册账号
   
   点击邮件中的链接，访问服务端的激活服务；
   激活的时候可能会出现三种状态：激活成功、激活失败、重复激活，可以把这三种状态写在一个接口中做一个常量声明，方变复用。谁用谁implements。
   
   ```java
   public interface PycommunityConstant {
       //激活成功
       int ACTIVATION_SUCCESS = 0;
       //重复激活
       int ACTIVATION_REPEAT = 1;
       //激活失败
       int ACTIVATION_FAILURE = 2;
   }
   ```
   
   编写service层代码
   
   ```java
   public int activation(int userId, String code) {
           User user = userMapper.selectById(userId);
           if(user.getStatus() == 1) {
               return ACTIVATION_REPEAT;
           }else if(!user.getActivationCode().equals(code)) {
               return ACTIVATION_FAILURE;
           }else {
               userMapper.updateStatus(userId,1);
               return ACTIVATION_SUCCESS;
           }
       }
   
   ```
   
   编写controller层代码
   
   ```java
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
       public String activation(Model model,
                                @PathVariable("userId") int id,
                                @PathVariable("code") String code) {
           int result = userService.activation(id, code);
           if(result == ACTIVATION_SUCCESS) {
               model.addAttribute("msg","激活成功，可以正常使用了");
               model.addAttribute("target","/login");
           }else if(result == ACTIVATION_REPEAT) {
               model.addAttribute("msg","重复激活！");
               model.addAttribute("target","/index");
           }else {
               model.addAttribute("msg","激活失败，提供的激活码不正确");
               model.addAttribute("target","/index");
           }
           return "/site/operate-result";
       }
   ```
   
   
   
## 2.3会话管理

   ### 2.3.1会话管理

http是简单的、可扩展的、无状态、有会话的。
HTTP 是无状态的：在同一个连接中，两个执行成功的请求之间是没有关系的。这就带来了一个问题，用户没有办法在同一个网站中进行连续的交互，比如在一个电商网站里，用户把某个商品加入到购物车，切换一个页面后再次添加了商品，这两次添加商品的请求之间没有关联，浏览器无法知道用户最终选择了哪些商品。而使用 HTTP 的头部扩展，HTTP Cookies 就可以解决这个问题。把 Cookies 添加到头部中，创建一个会话让每次请求都能共享相同的上下文信息，达成相同的状态。
注意，HTTP 本质是无状态的，使用 Cookies 可以创建有状态的会话。
**Cookie**是服务器发送到用户浏览器并保存在浏览器本地的一小块数据，它会在浏览器下次向同一服务器再发起请求时被携带并发送到服务器上。通常，它用于告知服务端两个请求是否来自同一浏览器，如保持用户的登录状态。Cookie 使基于无状态的 HTTP 协议记录稳定的状态信息成为了可能。但是cookie存在两大致命缺陷：cookie数据保存至浏览器端，安全性不高；每次发送请求会携带cookie数据，长此以往发送的cookie数据越来越多，影响性能。
![在这里插入图片描述](nowcoder.assets/b50c4a44ab0c430eba1e193cc43013be.png)

cookie使用步骤：创建cookie、设置cookie生效范围，仅在生效路径及其子路径携带cookie、设置cookie生存时间，默认关闭浏览器cookie消失；

```java
 package com.nju.community.controller;

import com.nju.community.util.CommunityUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/cookie")
public class CookieController {

    //浏览器第一次访问服务器，服务器保存在浏览器端的cookie
    @RequestMapping(value = "/set",method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie,一个cookie只能存一对key-value，且只能存字符串
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie请求范围
        cookie.setPath("/community/cookie");
        //设置生存时间,默认关闭浏览器cookie消失
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);
        return "set cookie";
    }

    //浏览器下次访问该服务器时，自动携带该cookie,浏览器的检查可以看到cookie信息
    @RequestMapping(path = "get",method = RequestMethod.GET)
    @ResponseBody
    //将key为code的cookie值赋给code
    public String getCookie(@CookieValue("code") String code){
         System.out.println(code);
        return "get cookie";
    }

}
```

session使用步骤：由springmvc自动创建session；往session中存数据；

![在这里插入图片描述](nowcoder.assets/d08787c196884fa1b57cb218ccb9b741.png)

```java
package com.nju.community.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.SecureRandom;

@Controller
@RequestMapping(path = "/session",method = RequestMethod.GET)
public class SessionController {

    //session示例
    @RequestMapping(path = "/set",method = RequestMethod.GET)
    @ResponseBody
    //session不像cookie需要手动创建，Spring mvc自动注入，类似于model，什么数据都能存储
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        return "set Session";
    }

    @RequestMapping(path = "/get",method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){

        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get Session";
    }
}
```

**cookie vs session：**

- **springmvc可以自动创建session并注入，cookie需要手动创建；**
- **cookie只能存字符串，session可以存任何类型的数据；**
- **cookie只能存少量数据，session可以存大量数据**

## 2.4生成验证码

用kaptcha在服务器端内存生成验证码图片
生成验证码步骤：（1）导入jar包；

```xml
<dependency>
    <groupId>com.github.penggle</groupId>
    <artifactId>kaptcha</artifactId>
    <version>2.3.2</version>
</dependency>
```

（2）编写kaptcha配置类，进行相关配置，将该配置类加载到spring容器中，由spring容器对其进行初始化

```java
@Configuration
public class KaptchaConfig {
    @Bean
    public Producer kaptchaProducer() {
        //config依赖于一个properties对象
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        //字符颜色黑色
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        //随机字符的范围
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        //生成四个随机字符
        properties.setProperty("kaptcha.textproducer.char.length","4");
        //生成的图片的噪声类是nonoise即不加噪声
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        //创建config对象，封装配置kaptcha的参数
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
```

（3）生成随机字符，根据字符生成图片。在controller中编写相关内容，由于Producer kaptchaProducer由spring容器初始化，这里进行自动注入，验证码文本需要跨请求（进入登录页的请求和点击登录按钮登录的请求）使用，因此将生成的验证码文本保存在session中，方便后续登录使用

```java
private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
 
@Autowired
private Producer kaptchaProducer;
@RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
public void getKaptcha(HttpSession session, HttpServletResponse response) {
   //生成验证码和图片
   String text = kaptchaProducer.createText();
   BufferedImage image = kaptchaProducer.createImage(text);
   //将验证码存入session
   session.setAttribute("kaptcha",text);
   //把图片输出给浏览器
   response.setContentType("image/png");
   try {
       ServletOutputStream outputStream = response.getOutputStream();
       ImageIO.write(image,"png",outputStream);
   } catch (IOException e) {
       logger.error("响应验证码失败" + e.getMessage());
   }
}

```

（4）修改login.html中的相关内容，编写js代码，实现点击刷新验证码，验证码自动刷新。
由于每个文件都引用了/js/global.js文件，为了减少硬编码， CONTEXT_PATH是定义在/js/global.js中的常量，为了使每次点击刷新验证码的路径变化，给路径加上随机参数p，p没有任何意义只是为了改变路径。

```html
	<script th:src="@{/js/global.js}"></script>
<!--	//为了防止写死路径，配置到全局的global.js文件里-->
	<script>
		function refresh_kaptcha(){
			var path = CONTEXT_PATH + "/kaptcha?p="+ Math.random();
			//点击时刷新图片的src属性，重新获得一次验证码
			document.getElementById("kaptcha").onclick = function() {
				this.src = "/community/kaptcha?time=" + new Date().getTime();
			}
			//$("#kaptcha").attr("src",path);
		}
	</script>
```

## 2.5 开发登录、退出功能

### 转发和重定向的区别

转发：是指服务器接收到一个请求后，将请求转发给另一个资源进行处理，并将该资源的处理结果返回给客户端。在这个过程中，转发后的资源**对客户端是不可见的**，客户端只知道自己访问了一个资源，而不知道这个资源是被转发到的。

重定向：是指服务器接收到一个请求后，发现该请求需要访问另一个资源才能得到响应，于是**告诉客户端重新发送一个请求**，访问另一个资源。在这个过程中，客户端会重新发送一个请求，访问另一个资源，因此客户端会知道自己访问了两个资源。

转发：是通过**服务器内部的转发机制**实现的。当服务器接收到一个请求后，根据请求的URL地址找到对应的资源，然后将该请求转发给另一个资源进行处理，最终将该资源的处理结果返回给客户端。在这个过程中，客户端只知道自己访问了一个资源，而不知道这个资源是被转发到的。全程是一个请求，最终是B响应客户端。

![img](nowcoder.assets/3ac79f3df8dcd100f453c7370e36a81bb8122f25.jpeg@f_auto)

重定向：是通过HTTP响应头中的Location字段实现的。当服务器接收到一个请求后，发现该请求需要访问另一个资源才能得到响应，于是在HTTP响应头中设置Location字段，**告诉客户端重新发送一个请求**，访问另一个资源。当客户端收到这个响应后，会重新发送一个请求，访问另一个资源，因此客户端会知道自己访问了两个资源。两个独立组件，没有什么关系适合重定向，比如request要访问服务器A组件的删除功能，完成后跳转到B的页面，而AB之间无关系，这时就适合用重定向。A会返回一个建议给web客户端，web客户端会自己去访问B，这样A和B之间解耦。但是这样A就不方便给B传参数。



![img](nowcoder.assets/6159252dd42a2834e7731648270826e117cebfe1.jpeg@f_auto)。L:                                                                                                                                                      

转发可以访问相对路径和绝对路径的资源，而重定向只能访问绝对路径的资源。因为转发是在服务器内部进行的，可以访问相对路径和绝对路径的资源。而重定向是在客户端进行的，只能访问绝对路径的资源。

### 页面中各种标签中链接地址(href src action)的区别

```html
<a href="/demo/index.jsp">跳转到首页</a>
<form action="/demo/test" method="post">
    <input type="submit" value="submit">
</form>
<img src="http://www.baidu.com/1.jpg" />
```

1. href是a标签的链接，表示点击a标签需要跳转到哪里，是静态资源路径

2. action是form表单的地址，表示表单需要提交到哪个地址(web路径)。

3. src是用在其它标签内的地址，是指向物件的来源，表示拿取。

1.   href与Action的区别


href只能get参数，action能get参数又能post参数

href一般用于单个连接，可以带参数（URL重写），是采用get方式请求的，在地址栏中可以看到所有的参数；

action一样用于表单的提交（如：注册）等，他可以提交大量和比较复杂的参数，可通过post和get两种方式提交。如果选择post方式 则在地址栏中看不到提交的信息。
**简单讲：单独连接到某个地址，用href;提交和注册信息，用action**

### URL路径里面的*，**和？的使用详解

注意：通配符不能包括分隔符"/"

？：匹配一个字符

```tex
/admin?可以匹配/admin1，但是不能匹配/admin或/admin/
```

*：匹配0个或多个字符串 

 ```tex
   /admin* 可以匹配/admin，/admin123，但是不能匹配/admin/1
   /test/* ，在test文件下一级目录，可以访问/test/a但就不允许访问/test/a/a
 ```

**：匹配0或多个路径

  ```tex
   /admin/** 将匹配/admin/a或者/admin/a/b
   /test/** , 所有子目录都可以访问
  ```

### return "redirect:/index" 和 return "/index"区别

 redirect:/index 后的"/index"是指的web访问路径，也就是RequestMapping中的path

 return "/index" 后的"/index"是指的classthpath:index.html,是静态资源路径

### 为什么访问图片要用图片的web路径，而不是使用静态资源路径去访问

图片是保存在服务器上的，localhost那么本机就是服务器，如果别的电脑访问服务器想要读取图片，是不能知道服务器的静态资源访问路径的，且用户输入的资源路径也是别的电脑的，并非服务器的。

### request返回页面的response所主动包含的参数

 ```java
  @RequestMapping(path = "/login",method = RequestMethod.POST)
 public String login(String code,boolean rememberme,Model model,User user,HttpSession session,HttpServletResponse response){
         ...
             response.addCookie(cookie);
     }
 ```

```html
th:value="${param.code}"
th:value="${param.rememberme}"
th:value="${user.id}"
```

如果参数不是普通参数，而是user等实体类，springmvc就会自动把参数封装在model里，model包含在response里，response把参数返回给页面。普通参数要获取要么主动获取，要么手动j加入到model。

response里的参数前端可以直接${名称}调用，但是不包含在response里的参数需要加param前缀来获取，即${param.参数}

### SpringMvc 会自动将容器里的类初始化一个对象

所以@Autowired 下的对象 无需new。

### sql中的paramterType和resultType什么时候可以不写？

+ **resultType**

1. 对于resultType的情况是比较简单的，一般来说增删改中mapper.xml文件中对应的resultType是不需要写的，因为增删改方法的返回值都是影响行数的int，mybatis自行处理，是**不需要写**的。

例如：![img](nowcoder.assets/9c20bc4cff03434f8d31b7bcc6a26381.png)

此处的delete标签返回值是影响行数的int，此时的resultType是不需要写的，图中也可以看出：使用idea编写时，也没有提示resultType的属性。所以第一种情况就是当增删改标签中的返回值是不需要写的。

2. 返回值是集合的泛型也就是List<Class>时，resultType必须写而且是泛型的类型。

例如:![img](nowcoder.assets/a374f6cbdd17440ab162d292f1e4096d.png)

3. 其他情况返回值是什么，resultType对应的填即可。

- **paramterType**

1.  当参数≥2个的时候，一般使用@Param（“参数名”）进行注解，标签中是可以通过#{参数名}获取到参数的，所以，此时的parameterType**不用写**。这里的参数名和xml的sql语句的参数名一一对应。

   ![img](nowcoder.assets/715a2fc5eef94fb085b58f284d01f4a5.png)

2. 当入参参数是集合的时候如List，map等，parameterType的书写是可以省略的。

   ![img](nowcoder.assets/344e8b6946034c11a54d03b6a4a97c9d.png)

   此处的insert标签中的返回值是影响行数的int，resultType不需要写，而入参是list，也是直接省略不写。（即若mapper接口的返回值和参数类型都和对应的sql标签一样，则不需要书写。）

   3. 其他情况下，parameterType均正常填写。

### 关于util工具类的思考

1. 常量类定义为接口，用的时候implements

   ![image-20231211162640257](nowcoder.assets/image-20231211162640257.png)

2. 方法定义为static，使用时import类，直接类名.方法名()![image-20231211162703024](nowcoder.assets/image-20231211162703024.png)

3. 添加@Component注解，使用时@Autowired导入

   ![image-20231211162736866](nowcoder.assets/image-20231211162736866.png)

### @Responsebody 一般用于异步获取数据

这个注解表示该方法的返回结果直接写入HTTP response body中，一般在异步获取数据时使用。

在使用@RequestMapping后，返回值通常解析为跳转路径。加上@responsebody后，返回结果直接写入HTTP response body中，不会被解析为跳转路径。比如异步请求，希望响应的结果是json数据，那么加上@responsebody后，就会直接返回json数据。服务器返回给客户端。

### @Bean声明的bean的名称是什么

默认情况下，使用 @Bean声明一个bean，bean的名称由**方法名**决定。

此外，可以通过@Bean注解里面的name属性主动设置bean的名称。

```java
@Bean
public Queue queue1(){ ... }

//默认为方法名
@Autowired
private Queue queue1;

//指定Bean名称
@Bean(name="queue-test")
public Queue queue1(){ ... }

//注入指定名称的bean
@Autowired
@Qualifier("queue-test")
private Queue queue;
```


