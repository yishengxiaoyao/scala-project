# B+ Tree VS B- Tree
>* 1、B+树的层级更少：相较于B树B+每个非叶子节点存储的关键字数更多，树的层级更少所以查询数据更快；
>* 2、B+树查询速度更稳定：B+所有关键字数据地址都存在叶子节点上，所以每次查找的次数都相同所以查询速度要比B树更稳定;
>* 3、B+树天然具备排序功能：B+树所有的叶子节点数据构成了一个有序链表，在查询大小区间的数据时候更方便，数据紧密性很高，缓存的命中率也会比B树高。
>* 4、B+树全节点遍历更快：B+树遍历整棵树只需要遍历所有的叶子节点即可，，而不需要像B树一样需要对每一层进行遍历，这有利于数据库做全表扫描。

B树相对于B+树的优点是，如果经常访问的数据离根节点很近，而B树的非叶子节点本身存有关键字其数据的地址，所以这种数据检索的时候会要比B+树快。


B树的特点：
（1）所有键值分布在整个树中
（2）任何关键字出现且只出现在一个节点中
（3）搜索有可能在非叶子节点结束
（4）在关键字全集内做一次查找，性能逼近二分查找算法

B+树与B树的不同在于：
（1）所有关键字存储在叶子节点，非叶子节点不存储真正的data
（2）为所有叶子节点增加了一个链指针


## B+ Tree
非叶子节点不存储在data,只存储索引,可以放更多的索引
叶子节点包含所有索引字段
叶子节点用指针链接,提高区间访问性能
叶子节点的数据都是递增的，用链表连接起来

InnoDB的存储文件为:test_innodb.frm(数据表结构)、test_innodb.idb(数据表的索引和数据)
表索引文件本身就是按照B+Tree组织的一个索引结构文件。
聚集索引--叶子结点包含完整的数据记录。

插入数据都是直接在后面添加

InnoDB必须要有主键,并且推荐使用自增？
InnoDDB的索引和数据都放到一块,如果没有索引的话,相当于全表扫描,需要比对全部的数据。如果索引不是整型,效率慢。如果不是自增,树的增删该查效率慢。


数据库中的B+Tree索引可以分为聚集索引(clustered index)和辅助索引(secondary index)。上面的B+Tree示例图在数据库中的实现即为聚集索引，
聚集索引的B+Tree中的叶子节点存放的是整张表的行记录数据。辅助索引与聚集索引的区别在于辅助索引的叶子节点并不包含行记录的全部数据，
而是存储相应行数据的聚集索引键,即主键。当通过辅助索引来查询数据时,InnoDB存储引擎会遍历辅助索引找到主键,
然后再通过主键在聚集索引中找到完整的行记录数据。




## B- Tree
叶子结点具有相同的深度,叶子结点的指针为空
 
所用的索引元素不重复
节点中的数据索引从左到右递增排列

## MyISAM
MyISMA创建的数据表会存储在三个文件:test_myisam.frm(数据表的结构)、test_myisam.myd(数据文件)、test_myisam.myi(数据索引)。

查找数据操作:
先去test_myisam.myi文件里面去读取索引，找到数据索引指向的数据,然后去test_myisam.myd文件里面去读取数据。

两次IO操作。


## Hash
Hash碰撞冲突,不支持范围查询。

## MVCC
MVCC是被Mysql中 事务型存储引擎InnoDB 所支持的;

应对高并发事务, MVCC比单纯的加锁更高效;

MVCC只在 READ COMMITTED 和 REPEATABLE READ 两个隔离级别下工作;

MVCC可以使用 乐观(optimistic)锁 和 悲观(pessimistic)锁来实现;

各数据库中MVCC实现并不统一。