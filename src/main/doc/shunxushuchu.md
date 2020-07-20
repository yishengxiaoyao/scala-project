# 数字字母顺序执行
问题:用两个线程,一个输出字母，一个输出数字，交替输出。1A2B3C4D5E6F7G。
## LockSupport
```
public class ParkAndUnPark {
    static Thread t1 = null,t2 = null;
    public static void main(String[] args) {
        char[] numbers = "1234567".toCharArray();
        char[] characters = "ABCDEFG".toCharArray();
        t1 = new Thread(()->{
            for (char ch:numbers){
                // 先输出一个数字
                System.out.println(ch);
                // 唤醒t2线程,t2执行
                LockSupport.unpark(t2);
                // 把当前线程挂起
                LockSupport.park();
            }
        },"t1");

        t2 = new Thread(()->{
            for (char ch:characters){
                // 将当前线程挂起
                LockSupport.park();
                // 输出字母
                System.out.println(ch);
                // 唤醒t1线程
                LockSupport.unpark(t1);

            }
        },"t2");
        t1.start();
        t2.start();
    }
}
```
LockSupport park/unpark底层实现为Unsafe park/unpark。

在Linux系统下，是用的Posix线程库pthread中的mutex（互斥量），condition（条件变量）来实现的。
mutex和condition保护了一个_counter的变量，当park时，这个变量被设置为0，当unpark时，这个变量被设置为1。
### park 的过程
1.当调用park时,先尝试能否直接拿到“许可”,即_counter>0时,如果成功,则把_counter设置为0,并返回;
2.如果不成功,则构造一个ThreadBlockInVM,然后检查_counter是不是>0,如果是,则把_counter设置为0,unlock mutex并返回;
3.否则，再判断等待的时间，然后再调用pthread_cond_wait函数等待，如果等待返回，则把_counter设置为0，unlock mutex并返回;

### unpark 的过程
当unpark时,则简单多了,直接设置_counter为1,再unlock mutex返回。如果_counter之前的值是0,则还要调用pthread_cond_signal唤醒在park中等待的线程;


## Spin Lock
使用自旋的方式,不断的尝试,知道获取这个锁。在JVM中,可以开启自旋锁,默认值为10;
如果第一次可以获取到锁,第二次就可以循环10次以上,就可以获取到锁。
```
public class SpinLock {
    static Thread t1 = null, t2 = null;

    enum ReadyToRun {T1, T2}

    static ReadyToRun r = ReadyToRun.T1;

    public static void main(String[] args) {
        char[] numbers = "1234567".toCharArray();
        char[] characters = "ABCDEFG".toCharArray();
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (char c : numbers) {
                    // 消耗CPU,不断判断
                    while (r != ReadyToRun.T1) {
                    }
                    System.out.print(c);
                    r = ReadyToRun.T2;
                }

            }
        });
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (char c : characters) {
                    while (r != ReadyToRun.T2) {
                    }
                    System.out.print(c);
                    r = ReadyToRun.T1;
                }

            }
        });
        t1.start();
        t2.start();
    }
}
```

## CAS 
Atomicxxx底层实现为CAS。
```
public class CASOrder {
    static Thread t1 = null, t2 = null;
    static AtomicInteger a = new AtomicInteger(1);
    public static void main(String[] args) {
        char[] numbers = "1234567".toCharArray();
        char[] characters = "ABCDEFG".toCharArray();
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (char c : numbers) {
                    while (a.get() != 1) {
                    }
                    System.out.print(c);
                    a.set(2);
                }

            }
        });
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (char c : characters) {
                    while (a.get() != 2) {
                    }
                    System.out.print(c);
                    a.set(1);
                }
            }
        });
        t1.start();
        t2.start();
    }
}
```

## BlockingQueue
使用两个不同的阻塞队列,可以实现顺序。如果是两个线程顺序执行的话(固定的内容),可以使用BlockingQueue或者ReentrantLock和Condition。
实现多生产者和消费者需要使用ReentrantLock和Condition，可以考虑使用两个Lock实现，可以借鉴LinkedBlockingQueue。
如果只是实现顺序执行的话，可以使用join()、CountDownLatch、CyclicBarrier。
```
public class BlockingLock {
    static Thread t1 = null, t2 = null;
    static BlockingQueue<String> bq1 = new ArrayBlockingQueue<>(1);
    static BlockingQueue<String> bq2 = new ArrayBlockingQueue<>(1);
    public static void main(String[] args) {
        char[] numbers = "1234567".toCharArray();
        char[] characters = "ABCDEFG".toCharArray();
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (char c : numbers) {
                    System.out.print(c);
                    try {
                        bq1.put("ok");
                        bq2.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (char c : characters) {
                    try {
                        bq1.take();
                        System.out.print(c);
                        bq2.put("ok");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        t2.start();
        t1.start();
    }
}
```

## CountDownLatch
```
public class CountDownLock {
    static Thread t1 = null, t2 = null;
    static CountDownLatch latch = new CountDownLatch(1);
    // 自旋占用CPU 速度特别快，执行时间特别短,线程特别多
    // 重量级锁 线程特别多,只有部分执行时间较长，需要向内核态申请资源
    public static void main(String[] args) {
        char[] numbers = "1234567".toCharArray();
        char[] characters = "ABCDEFG".toCharArray();
        final Object o = new Object();
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                // 不能使用this,必须要保证两个线程是使用的同一个锁
                synchronized (o) {
                    for (char c : numbers) {
                        System.out.print(c);
                        latch.countDown();
                        try {
                            o.notify(); // 获取其中一个线程
                            o.wait(); //进入waitset
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    o.notify(); //如果没有这一步就会造成不会退出
                }
            }
        });
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (o) {
                    for (char c : characters) {
                        System.out.print(c);
                        try {
                            o.notify(); // 获取其中一个线程
                            o.wait(); //进入waitset
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    o.notify();  //如果没有这一步就会造成不会退出
                }
            }
        });
        t1.start();
        t2.start();
    }
}
```
## Synchronized
```
public class SynchronizedLock {
    static Thread t1 = null, t2 = null;
    public static void main(String[] args) {
        char[] numbers = "1234567".toCharArray();
        char[] characters = "ABCDEFG".toCharArray();
        Lock lock = new ReentrantLock();
        //一把锁两种条件
        Condition conditionT1 = lock.newCondition();
        Condition conditionT2 = lock.newCondition();
        CountDownLatch latch = new CountDownLatch(1);
        t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lock.lock();
                    for (char c : numbers) {
                        System.out.print(c);
                        latch.countDown();
                        conditionT2.signal();
                        conditionT1.await();
                    }
                    conditionT2.signal();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
        t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();
                    lock.lock();
                    for (char c : characters) {
                        System.out.print(c);
                        conditionT1.signal();
                        conditionT2.await();
                    }
                    conditionT1.signal();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        });
        t1.start();
        t2.start();
    }
}
```

## 参考文献
[LockSupport（park/unpark）源码分析](https://www.jianshu.com/p/e3afe8ab8364)
[面试 LockSupport.park()会释放锁资源吗？](https://www.cnblogs.com/tong-yuan/p/11768904.html)
[LockSupport park和unpark](https://blog.csdn.net/fenglllle/article/details/83049251)
[两个线程,输出数字和字母](https://www.jianshu.com/p/b9080fe1a284)
[面试题。用两个线程](https://blog.csdn.net/fulq1234/article/details/104016790)