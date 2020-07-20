# Netty

## IO模型
>* BIO方式适用于连接数小且固定的架构,对服务器资源要求高,并发局限与应用中。
>* NIO方式适用于连接数目多且连接比较短的架构。
>* AIO方式适用于连接数目多且连接比较长。

## Selector、Channel和Buffer的关系
* 每个channel都会对应一个Buffer
* Selector对应一个线程,一个线程对应多个Channel
* Selector会根据不同的事件,在哥哥通道上切换
* Buffer就是一个内存快,底层是一个数组
* NIO的Buffer是可以读也可以写,需要flip方法切换
* Channel是双向的,可以返回底层操作系统的情况。

## Buffer

