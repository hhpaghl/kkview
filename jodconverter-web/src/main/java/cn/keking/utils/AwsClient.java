/**
 * 
 */
package cn.keking.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

/**
  * AwsClient
  * @Description: TODO
  * @author Mr.Yuan
  * @email yuanyong@gzsunrun.cn
  * @date  2017年7月7日 上午10:53:56
  * @version 3.0
  */
public class AwsClient {
	private AmazonS3  s3 ;
    private String awsAccessKey ;
    private String awsSecretKey ;
    private String bucketName ;
    
    public AwsClient(){
    	
    }
    
	public AwsClient(String awsAccessKey, String awsSecretKey, String bucketName, String endpoint) {
		super();
		this.awsAccessKey = awsAccessKey;
		this.awsSecretKey = awsSecretKey;
		this.bucketName = bucketName;
		init(awsAccessKey, awsSecretKey,endpoint);
	}

	
	public AmazonS3 getS3() {
		return s3;
	}

	public void setS3(AmazonS3 s3) {
		this.s3 = s3;
	}

	public String getAwsAccessKey() {
		return awsAccessKey;
	}

	public void setAwsAccessKey(String awsAccessKey) {
		this.awsAccessKey = awsAccessKey;
	}

	public String getAwsSecretKey() {
		return awsSecretKey;
	}

	public void setAwsSecretKey(String awsSecretKey) {
		this.awsSecretKey = awsSecretKey;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	private void init(String awsAccessKey, String awsSecretKey,String endpoint) {
		ClientConfiguration config = new ClientConfiguration();
		config.setProtocol(Protocol.HTTP);
		AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		s3 = new AmazonS3Client(credentials,config);
		s3.setEndpoint(endpoint);
	}
    
    /** 
    * @Title: uploadToS3 
    * @Description: 将文件上传至S3上并且返回url
    * @param @param tempFile 目标文件
    * @param @param remoteFileName 文件名
    * @param @return
    * @param @throws IOException    设定文件 
    * @return String    返回类型 
    * @throws 
    */
    public String uploadToS3(File tempFile, String remoteFileName) throws IOException {
        try {
            //上传文件
            s3.putObject(new PutObjectRequest(bucketName, remoteFileName, tempFile).withCannedAcl(CannedAccessControlList.PublicRead));
            //获取一个request
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(
                    bucketName, remoteFileName);
            //生成公用的url
            URL url = s3.generatePresignedUrl(urlRequest);
            System.out.println("=========URL=================" + url + "============URL=============");
            return url.toString();
        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
        } catch (AmazonClientException ace) {
        	ace.printStackTrace();
        }
        return null;
    }

     /** 
    * @Title: getContentFromS3 
    * @Description: 获取文件2进制流
    * @param @param remoteFileName
    * @param @throws IOException    设定文件 
    * @return S3ObjectInputStream    返回类型  数据流
    * @throws 
    */
    public S3ObjectInputStream getContentFromS3(String remoteFileName) throws IOException {
 		try {
 			GetObjectRequest request  = new GetObjectRequest(bucketName,remoteFileName);
 			S3Object object = s3.getObject(request);
 			S3ObjectInputStream inputStream = object.getObjectContent();
 			return inputStream;
 		} catch (Exception e) {
     	    e.printStackTrace();
 		} 
 		return null;
     }
    
    /**
     * S3文件下载
     * @param remoteFileName 文件名 [key]
     * @param path 文件存储路径
     */
    public void downFromS3(String remoteFileName,String path) throws IOException {
			GetObjectRequest request  = new GetObjectRequest(bucketName,remoteFileName);
			s3.getObject(request,new File(path));
    }
    
    /** 
    * @Title: getUrlFromS3 
    * @Description: 获取文件的url 
    * @param @param remoteFileName 文件名
    * @param @return
    * @param @throws IOException    设定文件 
    * @return String    返回类型 
    * @throws 
    */
    public String getUrlFromS3(String remoteFileName) throws IOException {
		try {
			GeneratePresignedUrlRequest httpRequest=new GeneratePresignedUrlRequest(bucketName, remoteFileName);
			String url=s3.generatePresignedUrl(httpRequest).toString();//临时链接
			return url;
		} catch (Exception e) {
    	    e.printStackTrace();
		} 
		return null;
    }
    /**
     * 验证s3上是否存在名称为bucketName的Bucket
     * @param s3
     * @param bucketName
     * @return
     */
    public static boolean checkBucketExists(AmazonS3 s3, String bucketName) {
        List<Bucket> buckets = s3.listBuckets();
        for (Bucket bucket : buckets) {
            if (Objects.equals(bucket.getName(), bucketName)) {
                return true;
            }
        }
        return false;
    }
    
    public  void delFromS3(String remoteFileName) throws IOException {
        try {
            s3.deleteObject(bucketName, remoteFileName);
        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
        } catch (AmazonClientException ace) {
        	ace.printStackTrace();
        }
    }
}
