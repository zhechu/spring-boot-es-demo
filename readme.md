## 部署 Elasticsearch

启动容器
```shell script
$ cd docker-compose/6.2.2
$ docker-compose up
```

测试部署是否成功
```shell script
$ curl http://192.168.163.21:9200/_cat/nodes?pretty
```
返回结果类似如下信息则表示成功
```shell script
172.18.0.2 51 39 10 0.70 1.07 0.70 mdi * es622
```

## 手动部署

部署 es
```shell script
$ docker run --name es622 -d -p 9200:9200 -p 9300:9300 -e "node.name=node0" -e "cluster.name=es622" -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:6.2.2
```

安装 IK 分词器
```shell script
$ ./bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.2.2/elasticsearch-analysis-ik-6.2.2.zip
```

部署 kibana
```shell script
$ docker run --name kibana_6_2_2 -d --link es622:elasticsearch -p 5601:5601 docker.elastic.co/kibana/kibana:6.2.2
```

## 索引

新建索引
```shell script
PUT video
{
  "mappings": {
      "_doc": {
        "properties": {
          "address": {
            "type": "text",
            "analyzer": "ik_max_word",
            "search_analyzer": "ik_max_word",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "createTime": {
            "type": "long"
          },
          "id": {
            "type": "long"
          },
          "score": {
            "type": "long"
          },
          "title": {
            "type": "text",
            "analyzer": "ik_max_word",
            "search_analyzer": "ik_max_word",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          }
        }
      }
    },
    "settings": {
      "index": {
        "number_of_shards": "1",
        "number_of_replicas": "1"
      }
    }
}
```
