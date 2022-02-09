#### 该插件为增强版match,在matchquery的基础上增加了对同义词权重的控制（synonym_boost设置同义词权重值，默认为0.9），以解决检索到的同义词可能会排在检索词前面的问题
#### query阶段通过tokenstream获取setting中的同义词配置,判断出分词出来的是否为同义词,若为同义词,则降权

### 注意：不要在query阶段和index阶段同时配置同义词分析器，因为该插件只是对query阶段进行改造，同时设置会导致降权失败！！！！
官方也不建议同时启用
> 
Using the same synonym token filter at both index time and search time is redundant. If, at index time, we replace English with the two terms english and british, then at search time we need to search for only one of those terms. Alternatively, if we don’t use synonyms at index time, then at search time, we would need to convert a query for English into a query for english OR british

```
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


# 无论使用 term 还是 match，使用同一词字段可以全部召回,但是无法区分原始词，还是同义词召回，并且在评分上没有区别对待
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
