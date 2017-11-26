package com.akkaec2.utils;

import static com.akkaec2.utils.SpringExtension.SPRING_EXTENSION_PROVIDER;
import static com.typesafe.config.ConfigFactory.load;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.typesafe.config.Config;

import akka.actor.ActorSystem;

@Configuration
@PropertySource("classpath:environment.properties")
public class CommonAppConfiguration {

  @Value("${amazon.aws.region}")
  private String amzonAWSRegion;

  @Value("${amazon.aws.accesskey}")
  private String amazonAWSAccessKey;

  @Value("${amazon.aws.secretkey}")
  private String amazonAWSSecretKey;

  private ApplicationContext applicationContext;

  @Autowired
  public CommonAppConfiguration(ApplicationContext applicationContext) {
    super();
    this.applicationContext = applicationContext;
  }

  @Primary
  @Bean(name = "AWSCredentials")
  public AWSCredentialsProvider amazonAWSCredentials() {
    return new AWSCredentialsProvider() {

      @Override
      public void refresh() {

      }

      @Override
      public AWSCredentials getCredentials() {
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
      }
    };
  }

  @Bean
  public AmazonAutoScaling ec2AutoScalingClient() {
    return AmazonAutoScalingClient.builder().withCredentials(amazonAWSCredentials()).withRegion(amzonAWSRegion).build();
  }

  @Bean
  public AmazonEC2 ec2Client() {
    return AmazonEC2ClientBuilder.standard().withCredentials(amazonAWSCredentials()).withRegion(amzonAWSRegion).build();
  }

  @Bean(destroyMethod = "terminate")
  public ActorSystem actorSystem() throws IOException {
    ActorSystem system = ActorSystem.create("akka-ec2", load("akka-ec2-application.conf"));
    SPRING_EXTENSION_PROVIDER.get(actorSystem()).initialize(applicationContext);
    return system;
  }

}
