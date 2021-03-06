# 性能设计
## 缓存
select是出现性能问题最大的地方,join、group、order、like等丰富的语义,非常消耗性能;大多数应用是读多写少。

缓存的三种模式:
>* Cache Aside更新模式
>   * 失效:应用程序从Cache获取数据，如果没有得到，从数据库中获取，成功后，放到混存中。
>   * 命中:应用程序从Cache中获取数据，取到后返回。
>   * 更新:先把数据存到数据库中，成功后，再让缓存失效。
>   * 这样操作会出现脏读，但是概率不大。可以通过2PC或者Paxos、为缓存设置过期时间来处理。
>   * Cache Aside是由调用方负责把数据载入缓存。
>* Read/Write Through 更新模式
>   * Read/Write Through套路是把更新数据的操作由缓存自己代理。
>   * Read Through在查询操作中更新缓存，用缓存服务自己来加载，从而对应用方是透明的。
>   * Write Through:在数据更新的时候,如果没有成功，直接更新数据库，然后返回，如果命中，则更新缓存，由cache自己更新数据库。
>* Write Behind Caching 更新模式
>   * 更新数据的时候，只更新缓存，不更新数据库，缓存会异步批量更新数据库。
>   * 带来的问题，数据不是抢一致性的，而且可以会丢失。

缓存设计的重点

使用Redis的缓存的原因:1.Redis的数据结构比较丰富;
2.不能把Service内放localcache,因为内存不够大,service由多个实例,
负载均衡会把请求随机到不同的实例。

## 异步处理

Push模式，是把任务派发给相关的人去处理(需要直到下游工作节点的情况,如果扩容,Push节点都需要直到,会增加系统复杂度);
Pull模式，则由处理的人来拉取任务处理(不用关心下游节点的状态)。

事件溯源:时间不可变，并且可使用只追加操作进行存储。

## 数据库扩展

读写分离的优点:1.比较容易实现;2.可以很好的把业务隔离开;3.很好地分担数据库的读负载。

读写分离的缺点:1.写库有单点故障;2.数据库同步不同时，需要抢一致性的读写操作还是需要落在写库上。
读写分离主要是为了减少读操作的压力。

CQRS(Command and Query Repository Segregation):
>* Command不会返回结果数据，只会返回执行状态，会改变数据。
>* Query只会返回结果数据，不会改变数据。
>* CQRS的好处:分工明确、业务上的命令和查询的职责分离、逻辑清晰、从数据驱动转到驱动。

影响数据库最大的性能问题:1.对数据库的操作;2.数据库中数据的大小。

分片策略:
>* 按多租户的方式。用租户ID来分。
>* 按数据的种类来分。
>* 通过范围来分。
>* 通过哈希散列算法来分。降低形成热点的可能性(不要使用这种方法)。

垂直分片:把经常修改的字段和不经常修改的字段分离开。

分片注意事项:
>* 随着数据库中数据的变化,需要定期重新平衡分片,以保证均匀分布并降低形成热点的可能性。
>* 分片是静态的,数据的访问则是不可预期的,可能需要经常性调整我们的的分片。
>* 可以并行从各个分片中提取此数据。
>* 要保证引用完整性和一致性。
>* 配置和管理大量分片。

业务层只有两阶段提交，数据层上只有Paxos。

## 秒杀
将服务部署到CDH上，然后统计在线数，然后确定要传递概率值。

## 边缘计算

数据量越来越大，分析结果的速度需要越来越快。
