package com.akkaec2.write;

import static akka.http.javadsl.ConnectHttp.toHost;
import static java.util.Collections.unmodifiableCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.services.autoscaling.AmazonAutoScaling;
import com.amazonaws.services.autoscaling.model.AutoScalingInstanceDetails;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesRequest;
import com.amazonaws.services.autoscaling.model.DescribeAutoScalingInstancesResult;
import com.amazonaws.services.ec2.AmazonEC2;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.OutgoingConnection;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Uri;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.Collections;

@Component
public class NodeDiscovery {

  private ActorSystem system;
  private ActorMaterializer materializer;
  private AmazonAutoScaling ec2AutoScalingClient;
  private AmazonEC2 amazonEC2;

  @Autowired
  public NodeDiscovery(ActorSystem system, AmazonAutoScaling ec2AutoScalingClient, AmazonEC2 amazonEC2) {
    super();
    this.system = system;
    this.materializer = ActorMaterializer.create(system);
    this.ec2AutoScalingClient = ec2AutoScalingClient;
    this.amazonEC2 = amazonEC2;
  }

  private final Flow<HttpRequest, HttpResponse, CompletionStage<OutgoingConnection>> connectionFlow() {
    return Http.get(system)
        .outgoingConnection(toHost(Uri.create("http://169.254.169.254/latest/meta-data/instance-id")));
  }

  private final CompletionStage<HttpResponse> httpResponse() {
    return Source.single(HttpRequest.create("/")).via(connectionFlow()).runWith(Sink.<HttpResponse> head(),
        materializer);
  }

  private String instanceId() {
    return httpResponse().toString();
  }

  private DescribeAutoScalingInstancesResult describeAutoScalingInstances(String instanceId) {
    return ec2AutoScalingClient
        .describeAutoScalingInstances()
        .withAutoScalingInstances(new AutoScalingInstanceDetails()
                                      .withInstanceId(instanceId));
  }

  private String groupName(DescribeAutoScalingInstancesResult autoScalingInstancesResult) {
    return autoScalingInstancesResult
        .getAutoScalingInstances()
        .get(0)
        .getAutoScalingGroupName();
  }
}
