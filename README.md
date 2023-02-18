# MiniJS

学习编译原理编写的玩具语言

## lexer

基于自写的正则表达式引擎进行词法分析

## parser

自写的 LR1 语法分析, 目前没有实现自举, 需要使用 Java 调用相关函数定义文法

## regexp

词法分析用到的正则表达式引擎, 将正则编译到 NFA, 再转换为 DFA

## syntax

MiniJS 的文法, 为 Javascript 的一个子集, 仅有少部分功能可以使用  
例如:  

* 没有变量提升
* 没有匿名函数
* 没有 let / const
* 没有原型链
* 没有异常处理
* 受限制的 new 语法
* 必须以分号结束语句
* 内置函数目前只有 console.log
* ...

## vm

非常简陋的虚拟机, 直接运行在语法制导后的 AST

## usage

编译
```sh
mvn package -DskipTests
```

运行 (需要 Java 版本大于等于 17)
```sh
java -jar target/minijs-1.0-SNAPSHOT-jar-with-dependencies.jar src/test/minijs/simple.js
```

## example

```js
function fibonacci(n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}

var result = fibonacci(32);
console.log(result);
```

输出 2178309
