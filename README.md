# easy-config
自己实现springboot自动配置刷新
详细说明请参见:https://www.cnblogs.com/rongdi/p/11569778.html
本地测试的11.txt其实就是一个普通的properties文件内容如下
test.name=test12231
person.name=lisi
food.name=apple1
测试请启动sample模块，修改本地配置文件，然后访问如下地址观察各个对象的属性变化
http://localhost:8080/getBean?beanName=cat
http://localhost:8080/getBean?beanName=props
http://localhost:8080/getBean?beanName=props1
http://localhost:8080/getBean?beanName=person
http://localhost:8080/getBean?beanName=dog
