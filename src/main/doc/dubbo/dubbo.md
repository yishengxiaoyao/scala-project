# Dubbo 与 Spring Boot整合
## 定义接口类
### UserService
```java
public interface UserService {
	/**
	 * 按照用户id返回所有的收货地址
	 * @param userId
	 * @return
	 */
	public List<UserAddress> getUserAddressList(String userId);
}
```
### OrderService
```java
public interface OrderService {
	/**
	 * 初始化订单
	 * @param userId
	 * @return
	 */
	public List<UserAddress> initOrder(String userId);
}
```

### USerAddress
```java
public class UserAddress implements Serializable {
	private Integer id;
    private String userAddress; //用户地址
    private String userId; //用户id
    private String consignee; //收货人
    private String phoneNum; //电话号码
    private String isDefault; //是否为默认地址    Y-是     N-否  
    // get/set方法、构造函数省略
}
```

## Provider
### 添加依赖
```xml
<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter</artifactId>
			<version>2.1.13.RELEASE</version>
		</dependency>

		<dependency>
			<groupId>com.atguigu.gmall</groupId>
			<artifactId>gmall-interface</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>

		<dependency>
			<groupId>org.apache.dubbo</groupId>
			<artifactId>dubbo</artifactId>
			<version>2.7.6</version>
		</dependency>
		<dependency>
			<groupId>org.apache.dubbo</groupId>
			<artifactId>dubbo-dependencies-zookeeper</artifactId>
			<version>2.7.6</version>
			<type>pom</type>
		</dependency>
		<dependency>
			<groupId>org.apache.dubbo</groupId>
			<artifactId>dubbo-spring-boot-starter</artifactId>
			<version>2.7.6</version>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<!-- Spring Boot -->
			<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-dependencies</artifactId>
				<version>2.1.13.RELEASE</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<!-- Apache Dubbo  -->
			<dependency>
				<groupId>org.apache.dubbo</groupId>
				<artifactId>dubbo-dependencies-bom</artifactId>
				<version>2.7.6</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	<repositories>
		<repository>
			<id>apache.snapshots.https</id>
			<name>Apache Development Snapshot Repository</name>
			<url>https://repository.apache.org/content/repositories/snapshots</url>
			<releases>
				<enabled>false</enabled>
			</releases>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
	</repositories>
```

### 添加配置文件
```yaml
dubbo:
  application:
    name: user-service-provider
  registry:
    address: 127.0.0.1:2181
    protocol: zookeeper
  protocol:
    name: dubbo
    port: 20881
  monitor:
    protocol: registry
  scan:
    base-packages: com.atguigu.gmall
```

### 具体实现类
```java
package com.atguigu.gmall.service.impl;
import java.util.Arrays;
import java.util.List;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.stereotype.Component;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
@Service(version = "1.0.0")
@Component
public class UserServiceImpl implements UserService {
	@Override
	public List<UserAddress> getUserAddressList(String userId) {
		System.out.println("UserServiceImpl..3.....");
		UserAddress address1 = new UserAddress(1, "北京市昌平区宏福科技园综合楼3层", "1", "李老师", "010-56253825", "Y");
		UserAddress address2 = new UserAddress(2, "深圳市宝安区西部硅谷大厦B座3层（深圳分校）", "1", "王老师", "010-56253825", "N");
		return Arrays.asList(address1,address2);
	}
}
```
在启动类上面添加@EnableDubbo注解

## Consumer
### 添加依赖
```xml
    <properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>com.atguigu.gmall</groupId>
			<artifactId>gmall-interface</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>org.apache.dubbo</groupId>
			<artifactId>dubbo</artifactId>
			<version>2.7.6</version>
		</dependency>
		<dependency>
			<groupId>org.apache.dubbo</groupId>
			<artifactId>dubbo-dependencies-zookeeper</artifactId>
			<version>2.7.6</version>
			<type>pom</type>
		</dependency>
		<dependency>
			<groupId>org.apache.dubbo</groupId>
			<artifactId>dubbo-spring-boot-starter</artifactId>
			<version>2.7.6</version>
		</dependency>
	</dependencies>
	<dependencyManagement>
		<dependencies>
			<!-- Spring Boot -->
			<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-dependencies</artifactId>
				<version>2.1.13.RELEASE</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
			<!-- Apache Dubbo  -->
			<dependency>
				<groupId>org.apache.dubbo</groupId>
				<artifactId>dubbo-dependencies-bom</artifactId>
				<version>2.7.6</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	<repositories>
		<repository>
			<id>apache.snapshots.https</id>
			<name>Apache Development Snapshot Repository</name>
			<url>https://repository.apache.org/content/repositories/snapshots</url>
			<releases>
				<enabled>false</enabled>
			</releases>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
	</repositories>
```
### 配置文件
```yaml
server:
  port: 8081
dubbo:
  application:
    name: boot-order-service-consumer
  registry:
    address: zookeeper://127.0.0.1:2181
  monitor:
    protocol: registry
```
## 具体的实现类
```java
package com.atguigu.gmall.service.impl;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import java.util.Arrays;
import java.util.List;
@Service(version = "1.0.0")
public class OrderServiceImpl implements OrderService {
    @Reference(version = "1.0.0")
    UserService userService;
    @Override
    public List<UserAddress> initOrder(String userId) {
        System.out.println("用户id：" + userId);
        List<UserAddress> addressList = userService.getUserAddressList(userId);
        return addressList;
    }
    public List<UserAddress> hello(String userId) {
        return Arrays.asList(new UserAddress(10, "测试地址", "1", "测试", "测试", "Y"));
    }
}
```
```java
package com.atguigu.gmall.controller;
import java.util.List;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.OrderService;
@Controller
public class OrderController {
	@Reference(version = "1.0.0")
	OrderService orderService;
	@ResponseBody
	@RequestMapping("/initOrder")
	public List<UserAddress> initOrder(@RequestParam("uid")String userId) {
		return orderService.initOrder(userId);
	}
}
```
在启动类上添加@EnableDubbo注解

## 其他

### 配置优先级
配置文件由高到低:
>* JVM -D参数，当你部署或者启动应用时，它可以轻易地重写配置，比如，改变dubbo协议端口；
>* XML, XML中的当前配置会重写dubbo.properties中的；
>* Properties，默认配置，仅仅作用于以上两者没有配置时。

### 启动时检查
检查相应的服务、消费者、注册中心是否启动成功，如果没有成功，抛出异常，如果成功，程序继续启动
```properties
java -Ddubbo.reference.com.foo.BarService.check=false # 判断这个接口是否可用
java -Ddubbo.reference.check=false # 判断引用的服务是否可用
java -Ddubbo.consumer.check=false  # 判断消费者是否可用
java -Ddubbo.registry.check=false # 判断注册中心是否可用
```
### 超时设置
```
@Reference(version = "1.0.0",timeout = 1000)
OrderService orderService;
```
这个参数是可选的,默认为1000ms，也可以自己设置,可以在一个接口或者个整个服务上设置。
不同粒度配置的覆盖关系:
>* 方法级优先，接口级次之，全局配置再次之。
>* 如果级别一样，则消费方优先，提供方次之。

### 重试次数
```
@Reference(version = "1.0.0",timeout = 1000,retries = 2)
OrderService orderService;
```
默认的重试次数为2,不包含第一次尝试。幂等性接口可以设置重试，如果不是，就不要设置重试。

### 多版本
```
@Reference(version = "1.0.0",timeout = 1000,retries = 2)
OrderService orderService;
```
在不同的服务之间，指定不同的版本。

### 本地存根
先对参数进行校验,如果不符合要求的话,就直接返回空值
```
@Reference(version = "1.0.0",stub = "com.atguigu.gmall.service.impl.UserServiceStub",retries = 2,timeout = 1000)
UserService userService;
```
```java
package com.atguigu.gmall.service.impl;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.util.StringUtils;
import java.util.List;
public class UserServiceStub implements UserService {
    private final UserService userService;
    public UserServiceStub(UserService userService) {
        this.userService = userService;
    }
    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        if (!StringUtils.isEmpty(userId)){
            return userService.getUserAddressList(userId);
        }
        return null;
    }
}
```
### 高可用
>* 监控中心宕机不影响使用,只是丢失部分采样数据
>* 数据库宕掉之后,注册中心能通过缓存提供服务列表查询,但不能注册新服务
>* 注册中心对等集群,任意一台宕机之后,将自动切换到另一台
>* 注册中心全部宕机之后,服务提供者和消费者仍能通过本地缓存通讯
>* 服务提供者无状态,任意一台宕机之后,不影响使用
>* 服务提供者全部宕机之后,服务消费者将无法使用,并无限次重试等待服务提供者恢复


### 负载均衡
dubbo的负载均衡策略有四个:Random LoadBalance、RoundRobin LoadBalance、LeastActive LoadBalance、ConsistentHash LoadBalance
#### Random LoadBalance
* 随机，按权重设置随机概率。
* 在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。
#### RoundRobin LoadBalance
* 轮询，按公约后的权重设置轮询比率。
* 存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。

#### LeastActive LoadBalance
* 最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
* 使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。

#### ConsistentHash LoadBalance
* 一致性 Hash，相同参数的请求总是发到同一提供者。
* 当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。
* 缺省只对第一个参数 Hash，如果要修改，请配置 <dubbo:parameter key="hash.arguments" value="0,1" />
* 缺省用 160 份虚拟节点，如果要修改，请配置 <dubbo:parameter key="hash.nodes" value="320" />

可以在服务端、客户端、服务端方法、客户端方法设置负载均衡的策略。

如果使用spring cloud也可以使用hystrix、resilence4j。

### 集群容错
#### Failover Cluster
失败自动切换，当出现失败，重试其它服务器 [1]。通常用于读操作，但重试会带来更长延迟。可通过 retries="2" 来设置重试次数(不含第一次)。

#### Failfast Cluster
快速失败，只发起一次调用，失败立即报错。通常用于非幂等性的写操作，比如新增记录。

#### Failsafe Cluster
失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。

#### Failback Cluster
失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。

#### Forking Cluster
并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。

#### Broadcast Cluster
广播调用所有提供者，逐个调用，任意一台报错则报错 [2]。通常用于通知所有提供者更新缓存或日志等本地资源信息。

## 参考文献
[尚硅谷Dubbo教程(dubbo经典之作)](https://www.bilibili.com/video/BV1ns411c7jV)
[属性配置](http://dubbo.apache.org/zh-cn/docs/user/configuration/properties.html)
[注解配置](http://dubbo.apache.org/zh-cn/docs/user/configuration/annotation.html)
[负载均衡](http://dubbo.apache.org/zh-cn/docs/user/demos/loadbalance.html)
[本地存根](http://dubbo.apache.org/zh-cn/docs/user/demos/local-stub.html)
[多版本](http://dubbo.apache.org/zh-cn/docs/user/demos/multi-versions.html)
[集群容错](http://dubbo.apache.org/zh-cn/docs/user/demos/fault-tolerent-strategy.html)