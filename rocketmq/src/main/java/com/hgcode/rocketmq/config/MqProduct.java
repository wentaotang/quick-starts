package com.hgcode.rocketmq.config;

import cn.hutool.core.lang.UUID;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
* @description: 一个mq集群应该就只有一个 生产者
 *
* @author: wentao_tang
* @create: 2020/6/11 18:55
**/
@Configuration
public class MqProduct {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqProduct.class);

    @Value("${apache.rocketmq.namesrvAddr}")
    private String namesrvAddr;

    private DefaultMQProducer producer = null;

    /**
     * 初始化
     */
    @PostConstruct
    public DefaultMQProducer start() {
        try {
            LOGGER.info("开始启动MqProduct生产者");
            producer = new DefaultMQProducer();
            producer.setNamesrvAddr(namesrvAddr);
            //存在多个集群的话 一定要配置instanceName
            producer.setInstanceName(UUID.fastUUID().toString(true));
            producer.start();
            LOGGER.info("MqProduct生产者启动成功,ip:{}",namesrvAddr);
        } catch (MQClientException e) {
            LOGGER.error("MqProduct生产者启动失败：{}-{}", e.getResponseCode(), e.getErrorMessage(),e);
            throw new RuntimeException(e.getMessage(), e);
        }catch (Exception e){
            LOGGER.error("MQ DtpProduct启动失败", e.getMessage(),e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return producer;
    }

    @PreDestroy
    public void stop() {
        if (producer != null) {
            producer.shutdown();
            LOGGER.info("MQ：执行方法MqProduct#stop关闭生产者");
        }
    }

     /**
       * @Description 发送异步消息
       * @Date 2020/5/16 22:04
       * @Author wentao
       * @param
      **/
    public Boolean sendMessageAsync(Message message) {
        LOGGER.info("开始发送异步消息#sendMessageAsync，入参为{}", message);
        boolean flag=true;
        try {
            producer.send(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    LOGGER.info("MQ: 异步发送消息成功{}", sendResult);
                }
                @Override
                public void onException(Throwable throwable) {
                    LOGGER.error(throwable.getMessage(), throwable);
                }
            });
        } catch (Exception e) {
            LOGGER.error("sendMessageAsync方法发生异常，异常信息为", e);
            flag=false;
        }
        return flag;
    }

    /**
     * @Desc 发送同步消息
     * @Date 2020/5/16 22:28
     * @Author wentao
    **/
    public SendResult sendMessageSync(Message message) {
        LOGGER.info("开始发送同步消息#sendMessageSync，入参为{}", message);
        SendResult sendResult=null;
        try {
            sendResult=  producer.send(message);
        } catch (Exception e) {
            LOGGER.error("sendMessageAsync方法发生异常，异常信息为", e);
        }
        return sendResult;
    }
}
