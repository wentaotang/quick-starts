package com.hgcode.rocketmq.config;

import cn.hutool.core.lang.UUID;
import com.hgcode.rocketmq.mq.Demo2Consumer;
import com.hgcode.rocketmq.mq.DemoConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
* @description: 采用这种方式，可以让消费者本身只需要专注于 消费者自身的逻辑
* @author: wentao_tang
* @create: 2020/6/11 18:52
**/
@Configuration
public class MqConsumer  {

    private static final Logger log = LoggerFactory.getLogger(MqConsumer.class);

    @Value("${apache.rocketmq.namesrvAddr}")
    private String namesrvAddr;

    @Resource
    private DemoConsumer demoConsumer;
    @Resource
    private Demo2Consumer demo2Consumer;

    /**
     * 消费者1 定义,具体的执行取决于 demoConsumer
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer demoConsumer() {
        return this.generateConsumer(demoConsumer,"group1","topic1", "*");
    }

    /**
     * 消费者2 定义，具体的执行取决于 demo2Consumer
     * @return
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer demo2Consumer() {
        return this.generateConsumer(demo2Consumer,"group2","topic2", "*");
    }

    /**
    * @description: 通用消费者
    * @author: wentao_tang
    * @create: 2020/6/11 18:40
    **/
    private DefaultMQPushConsumer generateConsumer(MessageListenerConcurrently messageListener, String groupName, String topic, String subExpression) {
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
            consumer.setInstanceName(UUID.fastUUID().toString(true));
            consumer.setNamesrvAddr(namesrvAddr);
            // 从消息队列尾开始消费
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
            // 集群消费模式
            consumer.setMessageModel(MessageModel.CLUSTERING);
            // 订阅主题
            consumer.subscribe(topic, subExpression);
            // 设置消息监听器
            consumer.setMessageListener(messageListener);
            return consumer;
        } catch (Exception e) {
            log.error("MQ Consumer消费端创建失败", e);
            return null;
        }
    }
}
