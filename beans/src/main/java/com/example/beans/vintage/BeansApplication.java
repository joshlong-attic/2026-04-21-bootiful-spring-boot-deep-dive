package com.example.beans.vintage;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;

class MyBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        registry.registerBean(DataSource.class,
                s -> new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build());
        registry.registerBean(JdbcTransactionManager.class,
                s -> s.supplier(b -> new JdbcTransactionManager(b.bean(DataSource.class))));
        registry.registerBean(MyJavaConfiguration.TxBeanPostProcessor.class);
        registry.registerBean(JdbcClient.class,
                spec -> spec.supplier(beans -> JdbcClient.create(beans.bean(DataSource.class))));
    }
}

@Import(MyBeanRegistrar.class)
@Configuration
@ComponentScan
class MyJavaConfiguration {

    @Bean
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    DataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource) {
        var settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:schema.sql"));
        return new DataSourceScriptDatabaseInitializer(dataSource, settings);
    }

    @Bean
    static TxBeanFactoryPostProcessor txBeanFactoryPostProcessor() {
        return new TxBeanFactoryPostProcessor();
    }

    static class TxBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

            for (var beanName : beanFactory.getBeanDefinitionNames()) {
                var beanDefinition = beanFactory.getBeanDefinition(beanName);
                var clazz = beanFactory.getType(beanName);
                IO.println("bean factory post processor for " + beanName + ":" +
                        clazz.getName());
            }


        }
    }

    static class TxBeanPostProcessor implements BeanPostProcessor {
        @Override
        public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof Tx tx) {
                IO.println("inspecting " +
                        beanName + " for " + Tx.class.getName());
                return BeansApplication.cglibTransactionalProxy(bean);
            }
            return bean;
        }
    }

}

// 0. xml, java config, component scanning, BeanRegistrar, etc.
// 1. BeanDefinitions
// 1.5 BeanFactoryPostProcessor
// 2. beans
// 2.1 BeanPostProcessor#beforeInitialization
// 2.2 (afterPropertiesSet, @PostConstruct)
// 2.3 BeanPostProcessor#afterInitialization
//

interface Tx {
}

//@SpringBootApplication
public class BeansApplication {


    /*public static void main(String[] args) throws Exception {
        var ac = new AnnotationConfigApplicationContext(MyJavaConfiguration.class);
        var petShop = ac.getBean(PetShop.class);
        System.out.println(petShop.dogs());
    } */

    static Object cglibTransactionalProxy(Object delegate/*PlatformTransactionManager tx*/) {
        var interfaces = delegate.getClass().getInterfaces();
        for (var i : interfaces) {
            if (Tx.class.isAssignableFrom(i)) {
                IO.println("found tx on the class " + delegate.getClass().getName());
                var pfb = new ProxyFactoryBean();
                pfb.setTarget(delegate);
                pfb.setProxyTargetClass(true);
                pfb.addAdvice(new MethodInterceptor() {
                    @Override
                    public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
                        return doTransactionalWork(invocation.getMethod(), delegate, invocation.getArguments());
                    }
                });
                return pfb.getObject();

            }
        }
        return null;
    }

    static PetShop jdkTransactionalProxy(PetShop delegate, PlatformTransactionManager tx) {
        var p = Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                new Class<?>[]{PetShop.class}, (proxy, method, args) -> doTransactionalWork(method, delegate, args));
        return (PetShop) p;
    }

    private static Object doTransactionalWork(Method method, Object delegate,
                                              Object[] args)
            throws Exception {
        IO.println("before " + method.getName());
        var result = method.invoke(delegate, args);
        IO.println("after " + method.getName());
        return result;
    }

}

// xml, component scanning, java config, beanRegistrar (new in 7!)


record Dog(int id, String name) {
}

// 0. dependency injection
// 1. portable service abstractions
// 2. AOP
// 3. autoconfig

@Service
class PetShop
        implements Tx {

    private final JdbcClient db;

    PetShop(JdbcClient jdbcClient) {
        this.db = jdbcClient;
    }

    //    @Override
    public Collection<Dog> dogs() {
        return this.db.sql("select * from dogs")
                .query((rs, rowNum) -> new Dog(rs.getByte("id"),
                        rs.getString("name")))
                .list();
    }
}

//class TxPetShopService implements PetShop {
//
//    private final PetShop petShop;
//    private final PlatformTransactionManager tx;
//
//    TxPetShopService(PetShop petShop, PlatformTransactionManager tx) {
//        this.petShop = petShop;
//        this.tx = tx;
//    }
//
//
//    @Override
//    public Collection<Dog> customers() {
//        var tx = this.tx.getTransaction(TransactionDefinition.withDefaults());
//        try {
//            var l = this.petShop.customers();
//            this.tx.commit(tx);
//            return l;
//        } catch (Throwable throwable) {
//            this.tx.rollback(tx);
//            throw new RuntimeException(throwable);
//        }
//    }
//}


//interface PetShop {
//    Collection<Dog> customers();
//}

// 10:40 - 11:10