# Java并发编程实战

## 可见性、原子性和有序性
为了合理利用CPU的高性能,平衡CPU、内存、IO的速度差异,计算机体系机构、操作系统、编译程序都做出了贡献,主要体现为:
>* CPU增加了缓存,以均衡与内存的速度差异;
>* 操作系统增加了进程、线程,以分时复用CPU,进而均衡CPU与IO设备的速度差异;
>* 编译程序优化指令执行次序,使得缓存能够得到更加合理利用。

一个线程对共享变量的修改,另一个线程能够立刻看到,成为可见性。

把一个或者多个操作在CPU执行的过程中不被中断的特性成为原子性。

解决原子性的问题,是要保证中间状态对外不可见。

```
public class LazyMan {
    private LazyMan(){

    }
    private static LazyMan lazyMan;

    public static LazyMan getInstance(){
        if (lazyMan == null){
            synchronized (LazyMan.class){
                if (lazyMan == null){
                    lazyMan = new LazyMan();
                }
            }
        }
        return lazyMan;
    }
}
```
上面的执行流程为:
1.分配一块内存M;
2.在内存M上初始化 LazyMan 对象;
3.然后M的地址复制给lazyMan变量;

为了提高执行速度,做一些优化:编译器优化的重排序、指令级并行的重排序、内存系统的重排序。

优化后的路径为:
1.分配一块内存M;
2.然后M的地址复制给lazyMan变量;
3.在内存M上初始化 LazyMan 对象;

这样就会产生半初始化的对象的情况，为了解决这种问题，使用volatile来修饰变量，就会插入内存屏障，禁止冲排序。

## Java内存模型如何解决可见性和有序性问题

Java内存模型是个很复杂的规范,可以从不同的视角来解读,本质上是，Java内存模型规范了JVM如何提高按需金庸缓存和编译优化。

### happen-before原则
前面一个操作的结果对后续操作是可见的。

>* 程序的顺序性原则。
>* volatile变量原则。
>* 传递性。
>* 管程中锁的原则(synchronized)。
>* 线程start原则。
>* 线程join原则。

```
class Account{
    private int balance;
    void transfer(Account transfer,int amt){
        synchronized(Account.class){
            if(this.balance > amt){
                this.balance -= amt;
                target.balance = amt;
            }
        }
    }
}
```

原子性问题的源头是线程切换,操作系统做线程切换是依赖CPU中断的,所以禁止CPU发生中断就能够禁止线程切换。

###如何预防死锁
1.破坏占用切等待条件
```
public class Allocator {
    private List<Object> als = new ArrayList<>();

    /**
     * 一次性申请所有资源
     * @param from
     * @param to
     * @return
     */
    synchronized boolean apply(Object from,Object to){
        if (als.contains(from)||als.contains(to)){
            return false;
        }else {
            als.contains(from);
            als.contains(to);
        }
        return true;
    }

    /**
     * 归还资源
     * @param from
     * @param to
     */
    synchronized void free(Object from,Object to){
        als.remove(from);
        als.remove(to);
    }

}
class Account{
    private Allocator allocator;
    private int balance;

    /**
     * 转账
     * @param target
     * @param amt
     */
    void transfer(Account target,int amt){
        while (!allocator.apply(this,target)){

        }
        try {
            // 锁定转出账号
            synchronized (this){
                // 锁定转入账号
                synchronized (target){
                    if (this.balance > amt){
                        this.balance -= amt;
                        target.balance += amt;
                    }
                }
            }
        }finally {
            allocator.free(this,target);
        }
    }
}
```
2.破坏不可抢占条件
3.破坏循环等待条件
```
public class Account {
    private int id;
    private int balance;

    /**
     * 转账
     * @param target
     * @param amt
     */
    void transfer(Account target,int amt){
        Account left = this;
        Account right = target;
        if (this.id > target.id){
            left = target;
            right = this;
        }
        // 锁定序列号小的账号
        synchronized (left){
            // 锁定序号大的账号
            if (this.balance > amt){
                this.balance -= amt;
                target.balance += amt;
            }
        }
    }
}
```
### wait 与sleep的区别
1.原理不同:sleep 是Thread的静态方法,是线程用来控制自身流程的,将此线程暂停执行一段时间,而把执行机会让给其他线程;
wait是Object类的方法,用于线程间的通信,这个方法会使当前拥有该对象的进程等待,直到其他线程调用notify方法，才会醒来。

2.对锁的处理机制不同:sleep 不会释放锁;调用wait之后,线程会释放所占用的锁。

3.使用区域不同:wait方法必须放在同步控制方法或者同步语句块中使用;sleep方法可以放在任何地方使用。

4.sleep方法必须捕获异常,不释放锁标志,有可能会出现死锁问题;wait、notify、notifyAll不需要捕获异常。

### sleep 与yield的区别
1.sleep 方法给其他线程运行机会时不考虑线程的优先级,因此会给低优先级的线程以运行的机会;yield方法只会给相同优先级或者更高优先级的线程以运行的机会。
2.线程执行sleep方法后会进入阻塞状态,所以,执行sleep方法的线程在指定的时间内肯定不会被执行;yield方法只是使当前线程重新会到可执行状态,所以执行yield
方法的线程有可能在进入到可执行状态后马上又被执行。
3.sleep方法声明抛出InterruptedException;yield方法没有声明任何异常。
4.sleep方法比yield方法具有更好的移植性。


## 安全性、活跃性以及性能问题
### 安全性
如果访问共享资源,保证互斥,或者顺序执行,就不会存在安全性问题,否则就会出现安全性问题。

### 活跃性
有时线程虽然没有发生阻塞,但仍然会在执行不下去的情况,这就是所谓的活锁。解决活锁是设置随机的时间。

所谓饥饿指的是线程因无法访问所需资源而无法执行下去的情况。

解决饥饿的方法:保证资源充足;公平分配资源;避免持有锁的线程长时间执行。

### 性能问题
从方案层面说:
1.既然使用锁会带来性能问题,那最好的方案自然就是使用无锁的算法和数据结构。
例如:Thread Local Storage(TLS)、Copy-On-Write、乐观锁、AtomicXXX、Disruptor。
2.减少锁持有问题。可以减少锁的力度，例如ConcurrentHashMap 

性能方面的指标:
1.吞吐量:单位时间内能处理的请求数量。吞吐量越高,说明性能越好。
2.延迟:从发出请求到响应的时间。延迟越小,说明性能越好。
3.并发量:同时处理的请求数量。

## 管程
Java采用的是管程技术，synchronized关键字及notify、wait、notifyAll这个三个方法都是管程的组成部分。
管程和信号量是等价的,所谓的等价指的是用管程能够实现信号量,也能用信号量实现管程。

所谓管程,指的是管理共享变量以及对共享变量的操作过程,让他们支持并发。

管程解决互斥问题的思路很简单,就是将共享变量及其对共享变量的操作统一封装起来。

实现管程的方式:
1.synchronized + wait + notify/notifyAll。
2.Lock + condition

## Java线程的声明周期
### 通用的线程生命周期
1.初始状态:线程已经被创建,但是还不允许分配CPU执行。这个只是编程语言层面被创建,在操作系统层面,真正的线程还没有创建。
2.可运行状态:线程可以分配CPU执行。真正的操作系统线程已经被成功创建,可以分配CPU执行。
3.运行状态:当有空闲的CPU，操作系统会将期分配给一个处于可运行状态的线程,被分配到CPU的线程转换为运行状态。
4.休眠状态:运行状态调用一个阻塞的API或者等待事件,运行状态转换为休眠状态,释放CPU使用权,休眠状态永远没有机会获得CPU使用权。等待事件出现了，从休眠状态变成可运行状态。
5.终止状态:线程执行完成或者出现异常才会进入终止状态,终止状态不会切换到其他任何状态。

### Java中线程的生命周期
Java中线程的生命周期:NEW(初始化状态)、RUNNABLE(可运行/运行状态)、BLOCKED(阻塞状态)、WAITING(无限时等待)、TIMED_WAITING(有时限等待)、TERMINATED(终止状态)。

#### RUNNABLE与BLOCKED的状态转换
synchronized修饰的方法、代码块同一时刻只允许一个线程执行,其他线程只能等待,这种情况下,等待的线程会从RUNNABLE切换到BLOCKED状态。
等待的线程获取的synchronized隐士锁时,就会从BLOCKED转换到RUNNABLE状态。

#### RUNNABLE与WAITING的状态转换
分为三种方式:
1.获得synchronized隐士锁的线程,调用无参数的object.wait方法。
2.调用无参数的Thread.join()方法。
3.调用LockSupport.park()方法。

#### RUNNABLE与TIMED_WAITING的状态转换
1.调用带超时参数的Thread.sleep(long millis)方法;
2.获得synchronized隐士锁的线程,调用带有超时参数的Object.wait(long timeout)参数;
3.调用带有超时参数的Thread.join(long millis)方法;
4.LockSupport.parkNanos(Object blocker,long deadline);
5.LockSupport.parkUntil(long deadline);

#### 从NEW到RUNNABLE状态
1.创建的Thread,执行run方法
2.实现Runnable接口,重写run方法。

#### 从RUNNABLE到TERMINATED状态
调用stop()(不建议使用)、interrupt()方法。

stop与interrupt的区别:stop方法会真的杀死线程;interrupt方法是通知线程,线程有机会执行一些后续操作,同时可以无视这个通知,接收到通知的方式:一种是异常，另一种是主动检测。

## 创建多少个线程才是合适的
在并发变成领域中,提升性能本质上就是提升硬件的利用率,在具体点来说,就是提升IO的利用率和CPU的利用率。

对于CPU密集性的场景,线程数 = CPU核数。
对于IO密集性的场景，线程数 = 2*CPU核数。

对于多核情况，有一个公式: 最佳线程数 = CPU核数 * [1 + (I/O耗时 / CPU耗时)]


## 为什么局部变量是线程安全的
栈帧和方法是同生共死的。局部变量就是放到了调用栈里。每个线程都有自己独立的调用栈。
方法里的局部变量,不会有并发问题,仅在单线程内访问数据。

## 如何用面向对象思想写好并发程序？
### 封装共享变量
将共享变量作为对象属性封装在内部,对所有公共方法指定并发访问策略。对于这些不会发生变化的共享变量,建议使用final关键字来修饰。

### 识别共享变量间的约束条件
一定要识别出所有共享变量之间的约束条件,如果约束条件识别不足,很可能导致制定的并发访问策略。
要考虑具体场景的一些约束,例如:库存下限要小于库存上限。


## 查找问题
top -c 查找那个进程占用的资源CPU多
top -Hp 查找那个线程出现的问题
jstack 查看线程的栈信息


## 参考文献
[Java内存模型（JMM）总结](https://zhuanlan.zhihu.com/p/29881777)
