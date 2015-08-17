# spring-data-redis-tools
spring data redis 封装工具类
***
## Maven
```html
  <dependency>
			<groupId>redis.clients</groupId>
			<artifactId>jedis</artifactId>
			<version>2.6.2</version>
	</dependency>
	<dependency>
			<groupId>org.springframework.data</groupId>
			<artifactId>spring-data-redis</artifactId>
			<version>1.5.1.RELEASE</version>
	</dependency>
```
## applicationContext.xml
```html
<!-- Redis AND Pool -->
	<bean id="poolConfig" class="redis.clients.jedis.JedisPoolConfig">
		<property name="maxTotal" value="${redis.pool.maxTotal}"></property>
		<property name="maxIdle" value="${redis.pool.maxIdle}" />
		<property name="maxWaitMillis" value="${redis.pool.maxWait}" />
		<property name="testOnBorrow" value="${redis.pool.testOnBorrow}" />
	</bean>

	<bean id="jedisConnFactory"
		class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory"
		p:hostName="${redis.host}" p:port="${redis.port}" p:password="${redis.password}"
		p:poolConfig-ref="poolConfig" />
	<!-- redis template definition -->
	<bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate"
		p:connectionFactory-ref="jedisConnFactory" />
```
