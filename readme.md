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

安装 Pinyin 分词器
```shell script
$ /usr/share/elasticsearch/bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-pinyin/releases/download/v6.2.2/elasticsearch-analysis-pinyin-6.2.2.zip
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
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              },
              "pinyin": {
                  "type": "text",
                  "store": false,
                  "term_vector": "with_offsets",
                  "analyzer": "pinyin_analyzer"
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
      },
      "analysis" : {
            "analyzer" : {
                "pinyin_analyzer" : {
                    "tokenizer" : "whitespace",
                    "filter" : "pinyin_first_letter_and_full_pinyin_filter"
                }
            },
            "filter" : {
                "pinyin_first_letter_and_full_pinyin_filter" : {
                    "type" : "pinyin",
                    "keep_first_letter" : true,
                    "keep_full_pinyin" : true,
                    "keep_none_chinese" : true,
                    "keep_original" : false,
                    "limit_first_letter_length" : 32,
                    "lowercase" : true,
                    "trim_whitespace" : true,
                    "keep_none_chinese_in_first_letter" : true
                }
            }
        }
    }
}
```

新建索引 tb_video
```shell script
PUT tb_video
{
  "mappings": {
      "doc": {
        "properties": {
          "id": {
            "type": "long"
          },
         "videoTitle": {
           "type": "text",
           "analyzer": "ik_max_word",
           "fields": {
             "keyword": {
               "type": "keyword",
               "ignore_above": 256
             }
           }
         },
          "score": {
            "type": "long"
          },
          "createTime": {
            "type": "date"
          },
          "lastUpdateTime": {
            "type": "date"
          },
         "gif": {
           "type": "keyword"
         }
        }
      }
    },
    "settings": {
      "index": {
        "number_of_shards": "2",
        "number_of_replicas": "1"
      },
      "analysis" : {
            "analyzer" : {
                "pinyin_analyzer" : {
                    "tokenizer" : "whitespace",
                    "filter" : "pinyin_first_letter_and_full_pinyin_filter"
                }
            },
            "filter" : {
                "pinyin_first_letter_and_full_pinyin_filter" : {
                    "type" : "pinyin",
                    "keep_first_letter" : true,
                    "keep_full_pinyin" : true,
                    "keep_none_chinese" : true,
                    "keep_original" : false,
                    "limit_first_letter_length" : 32,
                    "lowercase" : true,
                    "trim_whitespace" : true,
                    "keep_none_chinese_in_first_letter" : true
                }
            }
        }
    }
}
```

## 在线重建索引

官方文档

<https://www.elastic.co/guide/en/elasticsearch/reference/6.2/docs-reindex.html>

查看索引设置
```shell script
GET employees
```

创建临时索引
```shell script
PUT employees_temp
{
    "settings": {
      "index": {
        "number_of_shards": "1",
        "number_of_replicas": "1"
      }
    }
}
```

将数据导入临时索引
```shell script
POST _reindex
{
  "source": {
    "index": "employees"
  },
  "dest": {
    "index": "employees_temp"
  }
}
```
> TIPS：需注意导入后原索引的数据变更，这可能会导致数据丢失。

将原索引的别名设置到临时索引上
```shell script
POST /_aliases
{
    "actions": [
        { "remove": { "index": "employees", "alias": "employeesalias" }},
        { "add":    { "index": "employees_temp", "alias": "employeesalias" }}
    ]
}
```

重新设置原索引
```shell script
DELETE employees
PUT employees
{
    "settings": {
      "index": {
        "number_of_shards": "1",
        "number_of_replicas": "1"
      }
    }
}
```

将临时索引的数据导入原索引
```shell script
POST _reindex?refresh
{
  "source": {
    "index": "employees_temp"
  },
  "dest": {
    "index": "employees"
  }
}
```
> TIPS：需注意导入后临时索引的数据变更，这可能会导致数据丢失。

将临时索引的别名设置到原索引上
```shell script
POST /_aliases
{
    "actions": [
        { "remove": { "index": "employees_temp", "alias": "employeesalias" }},
        { "add":    { "index": "employees", "alias": "employeesalias" }}
    ]
}
```

删除临时索引
```shell script
DELETE employees_temp
```

> TIPS：为字段新增⼦字段、字段更换分词器器或更新分词器器词库可以使用`Update By Query`。

## logstash

logstash-input-jdbc 插件官方文档

<https://www.elastic.co/guide/en/logstash/current/plugins-inputs-jdbc.html#plugins-inputs-jdbc-options>

安装 jdbc 和 elasticsearch 插件
```shell script
$ ./bin/logstash-plugin install --no-verify logstash-input-jdbc
$ ./bin/logstash-plugin install --no-verify logstash-output-elasticsearch
```

logstash-input-jdbc 配置
```shell script
input {
  jdbc {
    jdbc_driver_library => "/usr/share/logstash/mysql-connector-java-5.1.47.jar"
    jdbc_driver_class => "com.mysql.jdbc.Driver"
    jdbc_connection_string => "jdbc:mysql://10.113.248.204:3306/qiaoku_video?characterEncoding=utf8&useUnicode=true&useSSL=false&serverTimezone=GMT%2B8"
    jdbc_user => "root"
    jdbc_password => "didong1904"
    schedule => "*/5 * * * * *"
    lowercase_column_names => false
    statement => "SELECT id, video_title as videoTitle, score, create_time as createTime, last_update_time as lastUpdateTime FROM tb_video WHERE last_update_time >= :sql_last_value"
    use_column_value => true
    tracking_column_type => "timestamp"
    tracking_column => "lastUpdateTime"
    last_run_metadata_path => "syncpoint_table"
  }
}
```

logstash-output-elasticsearch 配置
```shell script
output {
  elasticsearch {
    hosts => ["192.168.163.21"]
    index => "tb_video"
    document_id => "%{id}"
    document_type => "doc"
  }
}
```

启动
```shell script
$ ./bin/logstash -f config/sync_table.cfg
```

