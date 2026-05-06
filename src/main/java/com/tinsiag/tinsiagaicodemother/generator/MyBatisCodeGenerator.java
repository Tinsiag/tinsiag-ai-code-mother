package com.tinsiag.tinsiagaicodemother.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class MyBatisCodeGenerator {

    //prsf快捷键
    //要生成的表
    private static final String[] TABLE_NAMES = {"user"};
    public static void main(String[] args) {
        //创建数据源
        Dict dict = YamlUtil.loadByPath("application.yaml");
        Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");
        String url =String.valueOf(dataSourceConfig.get("url"));
        String username =String.valueOf(dataSourceConfig.get("username"));
        String password =String.valueOf(dataSourceConfig.get("password"));

        //配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        //创建配置内容，两种风格都可以。
        GlobalConfig globalConfig = createGlobalConfig();

        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);

        //生成代码
        generator.generate();
    }

//详细配置见 https://mybatis-flex.com/zh/others/codegen.html
    public static GlobalConfig createGlobalConfig() {
        //创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();

        //1.先生成到一个临时目录下，生成完成后，将临时目录下的文件复制到指定目录下
        globalConfig.getPackageConfig()
                .setBasePackage("com.tinsiag.tinsiagaicodemother.genresult");

        //设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig()
                .setGenerateTable(TABLE_NAMES)
                //设置逻辑删除列
                .setLogicDeleteColumn("isDelete");

        //设置生成 entity 并启用 Lombok
        globalConfig.enableEntity()
                .setWithLombok(true)
                .setJdkVersion(21);

        //设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();
        //生成service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();
        //生成controller
        globalConfig.enableController();

        //生成注释
        globalConfig.getJavadocConfig()
                .setAuthor("tinsiag")
                .setSince("");
        return globalConfig;
    }
}