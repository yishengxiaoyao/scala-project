# ACID的底层实现

## MySQL事务实现
BEGIN TRANSACTION -- 开启事务

COMMIT -- 将执行结果提交

ROLLBACK  -- 出现异常需要回滚

## ACID
A（Atomicity):原子性。原子性是指事务是一个不可分割的工作单位，事务中操作要么都发生，要么都不发生。

C（Consistency):一致性。事务前后的数据必须保持一致性。

I（Isolation):隔离性。事务的隔离性是多个用户并发访问数据库时，数据库为每一个用户开启的事务，不能被其他事务所干扰，多个并发事务之间要相互隔离。

D（Durability):持久性。持久性是指一个事务一旦被提交，它对数据库中的数据改变是永久性的，接下来即使数据库发生故障也不应该对其有任何影响。

## 事务的实现
MySQL中有undo log(用于回滚)和redo log(用于数据写入)

事务的原子性是通过undolog来实现的。
事务的持久性性是通过redolog来实现的。
事务的隔离性是通过(读写锁+MVCC)来实现的。
事务的一致性是通过原子性，持久性，隔离性来实现的。

### 原子性操作(undo log 实现回滚)
undo log记录的都是执行数据变更操作的逆向操作,在进行回滚的操作时候，可以从undo log文件中读取出来,然后执行，就可以将数据恢复回来。

MySQL的日志有很多种,如二进制日志、错误日志、查询日志、慢查询日志等,此外InnoDB存储引擎还提供了两种事务日志:
redo log(重做日志)和undo log(回滚日志)。其中redo log用于保证事务持久性;
undo log则是事务原子性和隔离性实现的基础。

每条数据变更的时候都伴随一条undo log记录生成,并且回滚日志必须先于数据持久化到磁盘。
undo log属于逻辑日志，它记录的是sql执行相关的信息。
所谓的回滚就是根据回滚日志做逆向操作，比如 delete 的逆向操作为 insert ，insert的逆向操作为delete，update的逆向为update等。

根据 undo log 进行回滚:
为了做到同时成功或者失败，当系统发生错误或者执行 rollback 操作时需要根据 undo log 进行回滚。
回滚操作就是要还原到原来的状态，undo log记录了数据被修改前的信息以及新增和被删除的数据信息，根据undo log生成回滚语句，比如：
>* 如果在回滚日志里有新增数据记录，则生成删除该条的语句
>* 如果在回滚日志里有删除数据记录，则生成生成该条的语句
>* 如果在回滚日志里有修改数据记录，则生成修改到原先数据的语句


### 持久性的实现(redo log,故障后恢复)
读数据：会首先从缓冲池中读取，如果缓冲池中没有，则从磁盘读取在放入缓冲池；
写数据：会首先写入缓冲池，缓冲池中的数据会定期同步到磁盘中(会产生脏读)；

于是 redo log就派上用场了。
既然redo log也需要存储，也涉及磁盘IO为啥还用它？
1.redo log 的存储是顺序存储，而缓存同步是随机操作。
2.缓存同步是以数据页为单位的，每次传输的数据大小大于redo log。


redo log采用的是WAL（Write-ahead logging，预写式日志），所有修改先写入日志，再更新到Buffer Pool，保证了数据不会因MySQL宕机而丢失，
从而满足了持久性要求。

既然redo log也需要在事务提交时将日志写入磁盘，为什么它比直接将Buffer Pool中修改的数据写入磁盘(即刷脏)要快呢？主要有以下两方面的原因：
a.刷脏是随机IO，因为每次修改的数据位置随机，但写redo log是追加操作，属于顺序IO。
b.刷脏是以数据页（Page）为单位的，MySQL默认页大小是16KB，一个Page上一个小修改都要整页写入；而redo log中只包含真正需要写入的部分，无效IO大大减少。

redo log 与binlog的区别:
a.作用不同:redo log是用于crash recovery的，保证MySQL宕机也不会影响持久性；binlog是用于point-in-time recovery的，保证服务器可以基于时间点恢复数据，此外binlog还用于主从复制。
b.层次不同:redo log是InnoDB存储引擎实现的，而binlog是MySQL的服务器层(可以参考文章前面对MySQL逻辑架构的介绍)实现的，同时支持InnoDB和其他存储引擎。
c.内容不同:redo log是物理日志，内容基于磁盘的Page；binlog的内容是二进制的，根据binlog_format参数的不同，可能基于sql语句、基于数据本身或者二者的混合。
d.写入时机不同:binlog在事务提交时写入；redo log的写入时机相对多元：
前面曾提到：当事务提交时会调用fsync对redo log进行刷盘；这是默认情况下的策略，修改innodb_flush_log_at_trx_commit参数可以改变该策略，但事务的持久性将无法保证。
除了事务提交时，还有其他刷盘时机：如master thread每秒刷盘一次redo log等，这样的好处是不一定要等到commit时刷盘，commit速度大大加快。

### 隔离性的实现(锁以及MVCC)

隔离性研究的是不同事务之间的相互影响。隔离性是指，事务内部的操作与其他事务是隔离的，并发执行的各个事务之间不能互相干扰。
严格的隔离性，对应了事务隔离级别中的Serializable (可串行化)，但实际应用中出于性能方面的考虑很少会使用可串行化。

Mysql隔离级别有以下四种(级别由低到高):READUNCOMMITED(未提交读)、READCOMMITED(提交读)、REPEATABLEREAD(可重复读)、SERIALIZABLE (可重复读)。

|隔离级别|脏读|可重复读|幻读|
|-----|-----|-----|----|
|READUNCOMMITED|可能|可能|可能|
|READCOMMITED|不可能|可能|可能|
|REPEATABLEREAD|不可能|不可能|可能|
|SERIALIZABLE|不可能|不可能|不可能|

(一个事务)写操作对(另一个事务)写操作的影响：锁机制保证隔离性。
(一个事务)写操作对(另一个事务)读操作的影响：MVCC保证隔离性。

脏读与不可重复读的区别在于：前者读到的是其他事务未提交的数据，后者读到的是其他事务已提交的数据。
不可重复读与幻读的区别可以通俗的理解为：前者是数据变了，后者是数据的行数变了。

MVCC最大的优点是读不加锁，因此读写不冲突，并发性能好。InnoDB实现MVCC，多个版本的数据可以共存，
主要是依靠数据的隐藏列(也可以称之为标记位)和undo log。其中数据的隐藏列包括了该行数据的版本号、删除时间、指向undo log的指针等等；
当读取数据时，MySQL可以通过隐藏列判断是否需要回滚并找到回滚需要的undo log，从而实现MVCC；


InnoDB实现的RR通过next-key lock机制避免了幻读现象。

next-key lock是行锁的一种，实现相当于record lock(记录锁) + gap lock(间隙锁)；其特点是不仅会锁住记录本身(record lock的功能)，
还会锁定一个范围(gap lock的功能)。
当然，这里我们讨论的是不加锁读：此时的next-key lock并不是真的加锁，只是为读取的数据增加了标记（标记内容包括数据的版本号等);
准确起见姑且称之为类next-key lock机制。


### 一致性的实现

数据库总是从一个一致性的状态转移到另一个一致性的状态。

实现一致性的措施包括：
保证原子性、持久性和隔离性，如果这些特性无法保证，事务的一致性也无法保证
数据库本身提供保障，例如不允许向整形列插入字符串值、字符串长度不能超过列的限制等
应用层面进行保障，例如如果转账操作只扣除转账者的余额，而没有增加接收者的余额，无论数据库实现的多么完美，也无法保证状态的一致

##参考文献
[深入学习MySQL事务：ACID特性的实现原理](https://www.cnblogs.com/kismetv/p/10331633.html)

**[MySQL 中 ACID 底层内部实现原理详解(重点推荐)](https://blog.csdn.net/qq_40884473/article/details/105213408)**

[[玩转MySQL之九]MySQL实现ACID机制之持久性](https://www.izhangchao.com/internet/internet_237675.html)