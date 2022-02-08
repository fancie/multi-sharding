package com.hidiu.sharding.config;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * @author fancie
 * @title: DataSourceConfig
 * @projectName multi-sharding
 * @description: TODO
 * @date 2022/2/7 上午11:15
 */
@Configuration
public class DataSourceConfig {

    @Autowired
    private Database0Config database0Config;

    @Autowired
    private Database1Config database1Config;

    @Autowired
    private PreciseAlgorithm preciseAlgorithm;

    @Value("${spring.sharding.tables.goods.logicTable}")
    private String logicTable;

    @Value("${spring.sharding.tables.goods.tableStrategy.standard.shardingColumn}")
    private String shardingColumn;


    @Value("${spring.sharding.tables.goods.actualDataNodes}")
    private String actualDataNodes;

    @Bean
    public DataSource getDataSource() throws SQLException {
        return buildDataSource();
    }

    //分库设置，如果不设置分库就只需put一个
    private Map<String, DataSource> getDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2);
        //添加两个数据库database0和database1
        dataSourceMap.put(database0Config.getDatabaseName(), database0Config.createDataSource());
        //多个库的时候可以才用分库方案
        dataSourceMap.put(database1Config.getDatabaseName(), database1Config.createDataSource());
        return dataSourceMap;
    }

    private TableRuleConfiguration getTableRuleConfiguration() {
        TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(logicTable, actualDataNodes);
        tableRuleConfiguration.setDatabaseShardingStrategyConfig(new StandardShardingStrategyConfiguration("goods_type", preciseAlgorithm));
        tableRuleConfiguration.setTableShardingStrategyConfig(new StandardShardingStrategyConfiguration(shardingColumn, preciseAlgorithm));
        return tableRuleConfiguration;
    }

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

    private DataSource buildDataSource() throws SQLException {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getBindingTableGroups().add(logicTable);
        //配置goods表规则
        shardingRuleConfig.getTableRuleConfigs().add(getTableRuleConfiguration());
        shardingRuleConfig.setDefaultDataSourceName("database1");
        return ShardingDataSourceFactory.createDataSource(getDataSourceMap(), shardingRuleConfig, new Properties());
    }

}
