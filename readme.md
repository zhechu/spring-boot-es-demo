## Elasticsearch with Spring Boot  [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

Detailed description can be found here: [Elasticsearch with Spring Boot](https://piotrminkowski.wordpress.com/2019/03/29/elsticsearch-with-spring-boot/)

## 部署 Elasticsearch

启动容器
```shell script
$ cd docker-compose/6.2.2
$ docker-compose up
```

测试部署是否成功
```shell script
$ curl http://192.168.163.41:9200/_cat/nodes?pretty
```
返回结果类似如下信息则表示成功
```shell script
172.18.0.2 51 39 10 0.70 1.07 0.70 mdi * es622
```

