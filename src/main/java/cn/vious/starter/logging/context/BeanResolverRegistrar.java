package cn.vious.starter.logging.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * BeanResolverRegistrar
 *
 * @author lcok
 * @date 2022/8/4 17:55
 */
@Component
public class BeanResolverRegistrar implements BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void register(StandardEvaluationContext context) {
        context.setBeanResolver(new BeanFactoryResolver(this.beanFactory));
    }
}
