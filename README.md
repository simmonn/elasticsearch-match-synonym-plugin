####该插件为增强版match,在matchquery的基础上增加了对同义词权重的调整,使得原词比同义词的分数高
query阶段通过tokenstream获取setting中的同义词配置,判断出分词出来的是否为同义词,若为同义词,则降权
```json
PUT /test_synonym
{
  "settings": {
    "index": {
      "analysis": {
        "analyzer": {
          "match_syn": {
            "tokenizer": "standard",
            "filter": [
              "synonym"
            ]
          }
        },
        "filter": {
          "synonym": {
            "type": "synonym_graph",
            "synonyms": [
              "China, cn, PRC"
            ]
          }
        }
      }
    }
  }
}

# title 添加了 synonym filter ，在index的时候加入 同义词
PUT test_synonym/_mapping
{
  "properties": {
    "title": {
      "type": "text",
      "analyzer": "match_syn"
    },
    "content" :{
      "type" :"text",
      "analyzer" :"standard" 
    }
  }
}


POST test_synonym/_doc
{
  "title":"China is the greatest country",
  "content":"China is the greatest country"
}

POST test_synonym/_doc
{
  "title":"PRC is the greatest country",
  "content":"PRC is the greatest country"
}


# 无论使用 term 还是 match，使用同一词字段可以全部召回
# 但是无法区分原始词，还是同义词召回，并且在评分上没有区别对待
GET test_synonym/_search
{
  "query": {
    "match": {
      "title": "China"
    }
  }
}

GET test_synonym_1/_search
{
  "query": {
    "term": {
      "title": {
        "value": "China"
      }
    }
  }
}


# 使用自定义的query 设置同义词产生的权重
# 通过调整synonym_boost参数来控制同义词权重,默认0.9 
# query 搜索内容
GET test_synonym/_search
{
  "query": {
    "match_deluxe": {
      "content": {
        "query": "this class",
        "analyzer": "match_syn",
        "synonym_boost" : 0.00001
      }
    }
  }
}

```