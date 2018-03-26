package cn.keking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
@ComponentScan(value = "cn.keking.*")
public class FilePreviewApplication {
	public static void main(String[] args) {
        Properties properties = System.getProperties();
        System.out.println(properties.get("user.dir"));
        SpringApplication.run(FilePreviewApplication.class, args);
	}
}

/*@SpringBootApplication  //- springboot项目,并配置扫描路径
@ComponentScan(value = "cn.keking.*")
@EnableAutoConfiguration //- springboot自动配置，这个注解可以根据你依赖的包自动生成相关配置
public class FilePreviewApplication extends SpringBootServletInitializer {
  public static void main(String[] args) {
    SpringApplication.run(FilePreviewApplication.class, args);
  }
  *//**
   * 需要把web项目打成war包部署到外部tomcat运行时需要改变启动方式
   *//*
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(FilePreviewApplication.class);
  }
}*/
