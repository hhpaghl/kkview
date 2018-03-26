package cn.keking.web.controller;

import java.io.IOException;

import cn.keking.utils.AwsClient;
import cn.keking.utils.ZkSimple;

public class TestController {
    public static void main(String[] args) {
    	  /*  String hostPort = "10.21.1.150:2181,10.21.1.150:2182,10.21.1.150:2183";
    	    String zpath = "/";
    	    List <String> zooChildren = new ArrayList<String>();
    	    ZooKeeper zk = new ZooKeeper(hostPort, 2000, null);    
    	    zooChildren = zk.getChildren(zpath, false);
            System.out.println("Znodes of '/': ");
            for (String child: zooChildren) {
              //print the children
              System.out.println(child);
            }
    	*/
    	
    	String aa = "工作计划表.xls";
    	String number = "123.456";
    	System.out.println(number.split(".")[0]);
    	
    /*	ZkSimple ZkSimple = new ZkSimple();
	    	for(int i=2;i>1;i++){
	    		try {
					ZkSimple.getData();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}*/
    		 /*String accesskey = "GKI4XZE9MOUO1FFFL2YQ";
    	     String awsSecretKey ="p57mZbR8NZQ9nKBsUbviMrmBEpzTlPNxMWJgNg10";
    	     String bucketName  ="czqtesting";
    	     String endpoint ="218.107.10.86:28080";
    	     AwsClient awsClient  = new AwsClient(accesskey,awsSecretKey,bucketName,endpoint);
    	     try {
				awsClient.downFromS3("藏毒案件引用插件表.xlsx", "E:\\test\\藏毒案件引用插件表.xlsx");
			
			} catch (IOException e) {
				e.printStackTrace();
			}*/
	}
}
