# Java并发工具类
## Lock和Condition

Java SDK并发包通过Lock和Condition两个接口来实现管程,其中Lock用于解决互斥问题,Condition用于解决同步问题。

如何设计一个互斥锁:能够响应中断、支持超时、非阻塞地获取。

调用方是否需要等待结果,如果需要等待结果,就是同步;如果不需要等待结果,就是异步。

## ReadWriteLock实现一个缓存

针对读多写少的场景,Java SDK提供了读写锁--ReadWriteLock。

读写锁需要遵守三条基本原则:
* 允许多个线程同时读共享变量
* 只允许一个线程写共享变量
* 如果一个写线程正在执行写操作,此时禁止读线程读共享变量

读写锁与互斥锁的一个重要区别是读写锁允许多个线程同时读共享变量,而互斥锁不允许,这是读写锁在读多邪少的场景下优于互斥锁的关键。

## StampedLock
ReadWriteLock 支持两种模式:一种是读锁,一种是写锁。StampedLock支持三种模式:写锁、悲观读锁、乐观锁。

## CountDownLatch和CyclicBarrier让多线程步调一致
CountDownLatch主要用来解决一个线程等待多个线程的场景;CyclicBarrier的计数器是可以循环利用,
CyclicBarrier是一组线程之间互相等待。

## 并发容器
### List
CopyOnWrite就是写的时候会将共享变量新复制一份出来,这样做的好处就是读操作完全无锁。
CopyOnWriteArrayList内部维护了一个数组,成员变量array就指向这个内部数组,所有的读操作都是基于array进行的。
在遍历的时候，增加元素:CopyOnWriteArrayList会将array复制一份,然后在新复制处理的数组上执行增加元素的操作,执行完之后在将
array指向这个新的数组(写操作基于新的array)。
CopyOnWriteArrayList仅适用于写操作非常少的场景,CopyOnWriteArrayList迭代器只读的,不支持增删改,
迭代器的遍历仅仅是一个快照。

### Map
ConcurrentHashMap的key是无序的,而ConcurrentSkipListMap的key是有序的。他们的key和value都不能为空,因为并发时不知道这个key是否油🈯️。

|集合类|key|value|是否线程安全|
|----|----|----|----|
|HashMap|允许为null|允许为null|否|
|TreeMap|不允许为null|允许为null|否|
|HashTable|不允许为null|不允许为null|是|
|ConcurrentHashMap|不允许为null|不允许为null|是|
|ConcurrentSkipListMap|不允许为null|不允许为null|是|

 
## 高性能限流器

