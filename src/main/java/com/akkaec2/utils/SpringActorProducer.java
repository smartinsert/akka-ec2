package com.akkaec2.utils;

import org.springframework.context.ApplicationContext;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;

public class SpringActorProducer implements IndirectActorProducer {
  
  private ApplicationContext applicationContext;

  private String beanActorName;

  public SpringActorProducer(ApplicationContext applicationContext, 
    String beanActorName) {
      this.applicationContext = applicationContext;
      this.beanActorName = beanActorName;
  }

  @Override
  public Actor produce() {
      return (Actor) applicationContext.getBean(beanActorName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends Actor> actorClass() {
      return (Class<? extends Actor>) applicationContext
        .getType(beanActorName);
  }
}
