package org.jbnu.jdevops.jcodeportallogin.config

import RoutingDataSource
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import javax.sql.DataSource


@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@Configuration
class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master.hikari")
    fun masterDataSource(): DataSource {
        return DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica.hikari")
    fun replicaDataSource(): DataSource {
        return DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    fun routingDataSource(): DataSource {
        val master = masterDataSource()
        val replica = replicaDataSource()

        // replica DataSource를 생성자 파라미터로 전달하여, 장애 시 fallback 로직을 사용할 수 있게 함.
        val routingDataSource = RoutingDataSource(replica)
        val dataSourceMap = HashMap<Any, Any>()
        dataSourceMap[DataSourceKey.MASTER] = master
        dataSourceMap[DataSourceKey.REPLICA] = replica

        routingDataSource.setTargetDataSources(dataSourceMap)
        // 기본 데이터 소스는 master로 설정
        routingDataSource.setDefaultTargetDataSource(master)
        routingDataSource.afterPropertiesSet()

        return routingDataSource
    }

    @Bean
    fun entityManagerDataSource(): DataSource {
        // 실제 RoutingDataSource를 반환하여 Hibernate가 필요한 메타데이터를 읽을 수 있도록 함
        return routingDataSource()
    }

    @Primary
    @Bean("dataSource")
    fun dataSource(routingDataSource: DataSource): DataSource {
        return LazyConnectionDataSourceProxy(routingDataSource)
    }

}
