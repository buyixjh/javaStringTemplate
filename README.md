# string_template
java字符串模板
使用方法:

1. 引入依赖
依赖在阿里云私服,github的依赖挂了,头痛中,所以现在没法用
```
<dependency>
    <groupId>com.wurenzhi</groupId>
    <artifactId>string_template</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```

2. 设置编译选项
idea:
Settings --> Build, Execution, Deployment --> Compiler --> Shared build process VM options: 
```
-Djps.track.ap.dependencies=false
```