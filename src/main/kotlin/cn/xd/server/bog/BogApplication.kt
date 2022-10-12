package cn.xd.server.bog

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.TransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.sql.DataSource

@SpringBootApplication
@EnableTransactionManagement
@EnableWebMvc
class BogApplication: WebMvcConfigurer{

    @Bean
    fun transactionManager(@Autowired datasource: DataSource): TransactionManager{
        return DataSourceTransactionManager(datasource)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/")
    }
}

/**
 * springboot 运行时全局实例
 */
private lateinit var application: ConfigurableApplicationContext

/**
 * 关闭运行时实例
 */
fun exitApplication(){
    application.close()
}

fun main(args: Array<String>) {
    application = runApplication<BogApplication>(*args)
}