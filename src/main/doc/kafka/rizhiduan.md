# 日志段:保存消息文件的对象
Kafka日志对象有多个日志段对象组成,而每个日志段对象会在磁盘上创建一组文件,包括消息日志文件(.log),唯一索引文件(.index),
时间戳索引文件(.timeindex)、以及终止(Aborted)事物的所有文件(.txnindex)。
如果没有使用Kafka事物,已终止事物的索引文件是不会被创建出来的。

## 日志段代码解析(LogSegment)
日志段是Kafka保存消息的最小载体。
LogSegement.scala 中包含LogSegement class、LogSegement object、LogFlushStatus object。

日志段有两个组件:log和index文件。log文件存储具体的信息,index文件记录逻辑offset到物理文件位置映射。

LogSegement的构造函数组成:消息日志文件、位移索引文件、时间戳索引文件、已终止事物索引文件。


### append执行步骤
1.首先调用records.sizeInBytes方法来判断日志段是否为空,如果为空的话,Kafka需要记录写入消息集合的最大时间戳,并将其作为后面新增日志段倒计时的依据。

2.判断ensureOffsetInRange方法确保输入参数最大谓一致是否为合法？就是看它与日志段其实位置的差值是否在整数范围内,
即largestOffset-baseOffset的值是否介于[0,Int.MAXVALUE]之间。

3.调用FileRecords的append方法执行真正的写入。

4.更新日志段的最大时间戳以及最大时间戳所属的逶迤指属性。每个日志段都要保存当前最大时间戳信息和所属消息的位移信息。

5.append方法的最后一步就是更新索引和写入的字节数。

### read方法
read方法的四个参数：
* startOffset:读取的第一条消息的唯一
* maxSize:能读取的最大字节数
* maxPosition:能读到的最大文件位置
* minOneMessage:是否允许在消息体过大是至少返回第一条消息,这样可以确保不会出现消费饿死的情况。

执行步骤:
1. 查找索引要确定读取物理文件位置:调用translateOffset方法定位要读取的其实文件位置(startPosition)。

2.计算要读取的粽子节数

3.读取消息

### recover方法
* 1.清空所有索引文件
* 2.遍历日志段中所有消息集合
    * 2.1 校验消息集合
    * 2.2 保存最大时间戳和所属消息位移
    * 2.3 更新索引项
    * 2.4 更新总消息字节数
    * 2.5 更新事务Producer状态和Leader Epoch缓存
* 3.执行消息日志索引文件截断

### truncateTo方法
清空大于给定制的offset的索引和数据。

根据情况选择新的索引:
* offset <= 最大的index, 删除所有的数据
* 确定的offset,删除索引值大于这个offset的数据
* 如果没有该偏移量的条目，则删除所有大于倒数第二个的内容


## 日志
### 如何加载日志段
日志是日志段的容器,里面定义了很多管理日志段的操作。

LogAppendInfo(Class):保存了一组待写入消息的各种元数据信息。

LogAppendInfo(Object):创建LogAppendInfo类的工厂

RollParams(Class): 定义用于控制日志段是否切分的数据结构。

LogMetricNames:定义Log对象的监控指标。

LogOffsetSnapShot:封装分区所有位移元数据的容器类。

LogReadInfo:封装读取日志返回的数据及其元数据。

ComplexTxn:记录完成事务的元数据,主要用于构建事务索引。

### Log(Object)元素解析
* .snapshot是Kafka为幂等型或事务行Producer所做的快照文件。
* .deleted是删除日志段操作创建的文件。删除操作是异步操作,Broker端把日志段文件从.log后缀修改为.delete后缀。
* .cleanup和.swap都是Compaction操作的产物。
* -delete则是应用于文件夹的。
* -future是用于更新主题分区文件加地址的。

nextOffsetMetaData:封装下一条待插入的位移值

highWatermarkMetadata:分区日志高水位值

segments:Log类中最重要额属性,保存了分区日志下所有的日志段信息


Log类的初始化逻辑:
* 1.创建分区日志路径 
* 2.初始化Leader Epoch Cache
    * 2.1创建Leader Epoch检查点文件
    * 2.2生成Leader Epoch Cache对象  
* 3.加载日志段对象
    * 3.1执行removeTempFilesAndCollectSwapFiles(删除临时文件,创建.swap文件)
    * 3.2源码开始清空已有日志端集合,并重新创建日志段文件
    * 3.3处理第一步返回的有效.swap文件集合
    * 3.4recoverLog操作
* 4.更新nextOffsetMetaData和logStartOffset
* 5.更新Leader Epoch Cache,清除无效数据

 