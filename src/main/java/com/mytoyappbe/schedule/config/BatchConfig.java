package com.mytoyappbe.schedule.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Spring Batch 설정을 위한 Configuration 클래스입니다.
 * {@link EnableBatchProcessing}을 통해 Spring Batch 기능을 활성화하고,
 * Batch 메타데이터 테이블이 데이터베이스에 자동으로 생성되도록 {@link DataSourceInitializer}를 구성합니다.
 */
@Configuration
@EnableBatchProcessing // Spring Batch 기능을 활성화합니다.
public class BatchConfig {

    /**
     * Spring Batch 메타데이터 테이블 생성을 위한 SQL 스크립트 리소스입니다.
     * `classpath:/org/springframework/batch/core/schema-mysql.sql` 경로에서 스크립트를 로드합니다.
     */
    @Value("classpath:/org/springframework/batch/core/schema-mysql.sql")
    private Resource batchSchemaScript;

    /**
     * Spring Batch 메타데이터 테이블을 초기화하는 {@link DataSourceInitializer} 빈을 구성합니다.
     * 이 이니셜라이저는 애플리케이션 시작 시 {@code batchSchemaScript}에 지정된 SQL 스크립트를 실행하여
     * Batch 관련 테이블을 생성합니다.
     *
     * @param dataSource 애플리케이션의 주 데이터 소스
     * @return 구성된 {@link DataSourceInitializer} 인스턴스
     */
    @Bean
    public DataSourceInitializer batchDataSourceInitializer(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(batchSchemaScript);
        populator.setContinueOnError(true); // 스크립트 실행 중 오류가 발생해도 계속 진행 (이미 테이블이 존재할 경우 등)

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
