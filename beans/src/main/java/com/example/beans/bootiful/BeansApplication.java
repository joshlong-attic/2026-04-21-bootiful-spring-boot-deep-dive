package com.example.beans.bootiful;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.generate.MethodReference;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.util.Collection;

class MyBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        registry.registerBean(JdbcTransactionManager.class,
                s -> s.supplier(b -> new JdbcTransactionManager(b.bean(DataSource.class))));
//        registry.registerBean(JdbcClient.class, spec -> spec.supplier(beans -> JdbcClient.create(beans.bean(DataSource.class))));
    }
}

@Import(MyBeanRegistrar.class)
@ImportRuntimeHints(BeansApplication.MyHints.class)
@SpringBootApplication
public class BeansApplication {

    private static final Resource MESSAGE = new ClassPathResource("/message");

    @Bean
    static MyBeanFactoryInitializationAotProcessor myBeanFactoryInitializationAotProcessor() {
        return new MyBeanFactoryInitializationAotProcessor();
    }

    static class MyBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

        @Override
        public @Nullable BeanFactoryInitializationAotContribution processAheadOfTime(
                ConfigurableListableBeanFactory beanFactory) {



            return new BeanFactoryInitializationAotContribution() {
                @Override
                public void applyTo(GenerationContext context,
                                    BeanFactoryInitializationCode code) {
                    var hints = context.getRuntimeHints();
                    // todo register hints
                   code.getMethods().add("hello" ,
                           a -> a.addStatement("IO.println(\"hi\");"));

                }
            };
        }
    }

    static class MyHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            IO.println("running and registering hints!");
            hints.resources().registerResource(MESSAGE);
        }
    }

    @Bean
    ApplicationRunner messageRunner() {
        return a -> IO.println(
                MESSAGE.getContentAsString(Charset.defaultCharset()));
    }

    @Bean
    ApplicationRunner petShopRunner(PetShop petShop) {
        return a -> IO.println("dogs: " + petShop.dogs());
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BeansApplication.class, args);
    }
}

// reflection
// resource (.properties, favico, ...)
// jni
// serialization
// jdk proxies


record Dog(int id, String name) {
}

@Service
@Transactional
class PetShop {

    private final JdbcClient db;

    PetShop(JdbcClient jdbcClient) {
        this.db = jdbcClient;
    }

    public Collection<Dog> dogs() {
        return this.db.sql("select * from dogs")
                .query((rs, rowNum) -> new Dog(rs.getByte("id"),
                        rs.getString("name")))
                .list();
    }
}