# MiniJS

学习编译原理编写的玩具语言

## lexer

词法分析

## parser

LR1 语法分析, 目前没有实现自举, 需要使用 Java 语言调用相关函数定义文法

## regexp

词法分析用到的正则表达式引擎, 将正则从 NFA 编译成 DFA

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
...


## vm

非常简陋的虚拟机

## usage


