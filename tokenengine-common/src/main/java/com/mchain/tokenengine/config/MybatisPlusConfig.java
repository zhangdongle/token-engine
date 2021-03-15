package com.mchain.tokenengine.config;

import com.baomidou.mybatisplus.MybatisConfiguration;
import com.baomidou.mybatisplus.entity.GlobalConfiguration;
import com.baomidou.mybatisplus.generator.config.rules.DbType;
import com.baomidou.mybatisplus.mapper.LogicSqlInjector;
import com.baomidou.mybatisplus.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.mchain.tokenengine.mybatis.MetaObjectHandlerConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

// import com.baomidou.mybatisplus.plugins.PerformanceInterceptor;

/** MybatisPlus配置 */
@Configuration
@MapperScan("com.mchain.*.mapper")
public class MybatisPlusConfig {

	@Value("${spring.profiles.active}")
	private String profile;

	@Bean("sqlSessionFactory")
	//    @Profile({"test","dev"})
	public SqlSessionFactory sqlSessionFactoryDev(@Qualifier("dataSource") DataSource dataSource,
			GlobalConfiguration globalConfiguration, MybatisConfiguration configuration,
			PaginationInterceptor paginationInterceptor,
			// PerformanceInterceptor performanceInterceptor,
			OptimisticLockerInterceptor optimisticLockerInterceptor) throws Exception {
		MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		// 扫描 mapper impl 文件
		Resource[] resources = new PathMatchingResourcePatternResolver()
				.getResources("classpath*:com/mchain/tokenengine/**/*.xml");
		bean.setMapperLocations(resources);
		// 注册别名, 注册后在 Mapper 对应的 XML 文件中可以直接使用类名
		bean.setTypeAliasesPackage("com.mchain.*.entity");
		// 枚举注入
		bean.setTypeEnumsPackage("com.mchain.tokenengine.**");

		bean.setGlobalConfig(globalConfiguration);
		bean.setConfiguration(configuration);

		// 添加插件
		if (StringUtils.equalsAny(profile, "test", "dev", "koc", "aladdin")) {
			bean.setPlugins(new Interceptor[]{
					// 分页插件
					paginationInterceptor,
					// SQL执行效率插件
					// performanceInterceptor,
					// 乐观锁插件
					optimisticLockerInterceptor});
		} else {
			bean.setPlugins(new Interceptor[]{
					// 分页插件
					paginationInterceptor,
					// 乐观锁插件
					optimisticLockerInterceptor});
		}

		return bean.getObject();
	}

	@Bean
	public MybatisConfiguration configuration() {
		MybatisConfiguration configuration = new MybatisConfiguration();
		//  配置的缓存的全局开关 默认true
		//        configuration.setCacheEnabled(false);
		// 延时加载的开关
		//        configuration.setLazyLoadingEnabled(true);
		// 打印 SQL
		if (StringUtils.equalsAny(profile, "test", "dev", "koc", "aladdin")) {
			configuration.setLogImpl(Slf4jImpl.class);
		} else {
			configuration.setLogImpl(StdOutImpl.class);
		}
		return configuration;
	}

	@Bean
	public GlobalConfiguration globalConfiguration() {
		GlobalConfiguration conf = new GlobalConfiguration(new LogicSqlInjector());
		// 主键类型  0:"数据库ID自增", 1:"用户输入ID",2:"全局唯一ID (数字类型唯一ID)", 3:"全局唯一ID UUID";
		conf.setIdType(0);
		// 刷新mapper 调试神器
		conf.setRefresh(true);
		// 表名、字段名、是否使用下划线命名
		conf.setDbColumnUnderline(false);
		// MyBatis Plus 公共字段自动填充
		conf.setMetaObjectHandler(new MetaObjectHandlerConfig());
		return conf;
	}

	/** mybatis-plus分页插件 */
	@Bean
	public PaginationInterceptor paginationInterceptor() {
		PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
		paginationInterceptor.setDialectType(DbType.MYSQL.getValue());
		return paginationInterceptor;
	}

	/** mybatis-plus SQL执行效率插件【生产环境可以关闭】 */
	// @Bean
	// @Profile({"dev", "test"})
	// public PerformanceInterceptor performanceInterceptor() {
	// 	return new PerformanceInterceptor();
	// }

	/** 乐观锁插件 */
	@Bean
	public OptimisticLockerInterceptor optimisticLockerInterceptor() {
		return new OptimisticLockerInterceptor();
	}
}
