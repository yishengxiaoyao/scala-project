# JDK 序列化
## 序列化和分序列化概念

### 什么是序列化和反序列化
Java序列化是指把Java对象转换为字节序列的过程,而Java反序列化是指把字节序列恢复为Java对象的过程;
对象序列化的最主要的用处就是在传递和保存对象的时候，保证对象的完整性和可传递性。序列化是把对象转换成有序字节流，以便在网络上传输或者保存在本地文件中。
序列化:序列化后的字节流保存了Java对象的状态以及相关的描述信息。序列化机制的核心作用就是对象状态的保存与重建。
反序列化:客户端从文件中或网络上获得序列化后的对象字节流后，根据字节流中所保存的对象状态及描述信息，通过反序列化重建对象。

### 序列化的好处
>* 永久性保存对象，保存对象的字节序列到本地文件或者数据库中；
>* 通过序列化以字节流的形式使对象在网络中进行传递和接收；
>* 通过序列化在进程间传递对象；

### 序列化算法一般步骤
>* 将对象实例相关的类元数据输出。
>* 递归地输出类的超类描述直到不再有超类。
>* 类元数据完了以后,开始从最顶层的超类开始输出对象实例的实际数据值。
>* 从上至下递归输出实例的数据。

## Java如何实现序列化和反序列化

ObjectOutputStream(对象输出流)的writeObject方法将对象进行序列化,把得到的字节序列写到一个目标输出流中;

ObjectInputStream(对象输入流)的readObject方法将输入流中读取字节序列,再把它们反序列化成为一个对象,并将其返回。

只有实现了Serializable或Externalizable(优先级高于Serializable)接口的类的对象才能被序列化，否则抛出异常！

## JDK序列化的缺点
### 无法跨语言
Java 序列化技术是 Java 语言内部的私有协议，其他语言并不支持。
### 序列化后的码流太大
采用 JDK 序列化机制编码后的二进制数组大小是通过缓冲区处理后的 4 倍。

### 序列化性能太低
Java 序列化的性能只有二进制编码的 11% 左右，可见原生序列化的性能很差。

## 总结
>* 序列化时,只对对象的状态进行保存,而不管对象的方法；
>* 当一个父类实现序列化,子类自动实现序列化,不需要显式实现Serializable接口,就是具有继承性;
>* 当一个对象的实例变量引用其他对象，序列化该对象时也把引用对象进行序列化;
>* 声明为static和transient类型的成员数据不能被序列化。因为static代表类的状态，transient代表对象的临时数据。
>* 实现序列化时，有一个序列化id,显式地定义serialVersionUID有两种用途:
>   * 希望类的不同版本对序列化兼容，因此需要确保类的不同版本具有相同的serialVersionUID;
>   * 不希望类的不同版本对序列化兼容，因此需要确保类的不同版本具有不同的serialVersionUID。
>* Java有很多基础类已经实现了serializable接口。
>* 如果一个对象的成员变量是一个对象，那么这个对象的数据成员也会被保存！这是能用序列化解决深拷贝的重要原因。

## 参考文献
[序列化和反序列化的底层实现原理是什么？](https://blog.csdn.net/xlgen157387/article/details/79840134)
[java 序列化 原理解析](https://blog.csdn.net/jijianshuai/article/details/79937589)
[Java 序列化 之 Serializable](https://www.jianshu.com/p/af2f0a4b03b5)
[Java 序列化之 Externalizable](https://www.jianshu.com/p/411e18ceaa55)
[Java 序列化 之 单例模式](https://www.jianshu.com/p/ca8336375a69)
[Java序列化的原理](https://blog.csdn.net/tomisaboy/article/details/51553550)
[JDK1.8 序列化机制浅析](https://www.jianshu.com/p/7dca302d890b)
[Java 序列化的缺点](https://blog.csdn.net/zhengzhaoyang122/article/details/100780222)


