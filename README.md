#使用JPA+Shardingsphere进行分库分表的DEMO
-------------------

项目支持任意多个库+任意多个表

使用两种写法

1.Groovy表达式配置表路由规则
```
    //Groovy表达式配置表路由规则
    private ShardingRuleConfiguration getShardingRuleConfiguration(){
        TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration(logicTable, actualDataNodes);

        // 配置分库策略（Groovy表达式配置db规则）
        orderTableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("goods_type", "database${goods_type % 2}"));

        // 配置分表策略（Groovy表达式配置表路由规则）
        orderTableRuleConfig.setTableShardingStrategyConfig(new InlineShardingStrategyConfiguration("goods_id", "goods_${goods_id % 2}"));

        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
        return shardingRuleConfig;
    }
```

2.实现路由算法
```
    private TableRuleConfiguration getTableRuleConfiguration() {
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(logicTable, actualDataNodes);
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("goods_type", preciseAlgorithm));
        tableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(shardingColumn, preciseAlgorithm));
        return tableRuleConfiguration;
    }
```
