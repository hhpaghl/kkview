package cn.keking.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.qos.logback.classic.Logger;
import cn.keking.service.FileConverQueueTask;
import cn.keking.service.FilePreview;
import cn.keking.service.FilePreviewFactory;
import cn.keking.utils.AwsClient;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.ZkSimple;

/**
 * @author yudian-it
 */
@Controller
public class OnlinePreviewController {

    @Autowired
    FilePreviewFactory previewFactory;

    @Autowired
    RedissonClient redissonClient;
    
    @Autowired 
    DownloadUtils downloadUtils;
    
    @Value("${file.dir}")
    String fileDir;
   
 
    /**
     * @param url
     * @param model
     * @return
     */
    @RequestMapping(value = "onlinePreview", method = RequestMethod.GET)
    public String onlinePreview(String url, Model model, HttpServletRequest req) {
    
        req.setAttribute("fileKey", req.getParameter("fileKey"));
        FilePreview filePreview = previewFactory.get(url);
        return filePreview.filePreviewHandle(url, model);
    }

    @RequestMapping(value = "preview", method = RequestMethod.GET)
    public String  preview(String serverId, String fileName,Model model, HttpServletRequest req) throws IOException {
    	 Map<String,Object> map = new HashMap<String,Object>();
    	//String downlowUrl = "http://localhost:8082/filepath/gongzuojihuab.xls";
    	//String fileName = "gongzuojihuab.xls";
    	//String filetype = fileName.substring(fileName.lastIndexOf(".")+1);
    	
    	
    	 String accesskey = "GKI4XZE9MOUO1FFFL2YQ";
	     String awsSecretKey ="p57mZbR8NZQ9nKBsUbviMrmBEpzTlPNxMWJgNg10";
	     String bucketName  ="czqtesting";
	     String endpoint ="218.107.10.86:28080";
	 	 AwsClient awsClient = null;
	    try {
	    	//1.获取zookeeper中 配置的 访问S3服务的参数值
	    	ZkSimple ZkSimple = new ZkSimple();
	    	accesskey = ZkSimple.getValueByKey("/custom/s3/accesskey");
	    	awsSecretKey = ZkSimple.getValueByKey("/custom/s3/secretKey");
	    	bucketName = ZkSimple.getValueByKey("/custom/s3/bucket");
	    	endpoint =  ZkSimple.getValueByKey("/custom/s3/endpoint");
	    	awsClient  = new AwsClient(accesskey,awsSecretKey,bucketName,endpoint); //连接S3 服务
	    	//检查文件是否存在
	    	System.out.println(fileDir+"demo\\"+fileName);
			awsClient.downFromS3(serverId,fileDir+"demo\\"+fileName);
		} catch (Exception e) {
			map.put("code", 400);
			map.put("message","文件预览失败,请检查文件是否存在!");
			model.addAttribute("message", "文件预览失败,请检查文件是否存在!");
			//return  map;
		}
       // req.setAttribute("fileKey", req.getParameter("fileKey"));
      String viewUrl  ="http://localhost:8014/demo/"+fileName;
	    //String viewUrl  ="http://localhost:8014/onlineview/demo/"+fileName;
	    System.out.println("viewUrl>>>>>>"+viewUrl);
        // viewUrl  = fileDir+fileName;
	      File file =  new File(fileDir+fileName);
	      awsClient.uploadToS3(file,serverId);
	      FilePreview filePreview = previewFactory.get(viewUrl);
	      return filePreview.filePreviewHandle(viewUrl, model);	
      
    }

    
    
    /**
     * 多图片切换预览
     *
     * @param model
     * @param req
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "picturesPreview", method = RequestMethod.GET)
    public String picturesPreview(String urls, String currentUrl, Model model, HttpServletRequest req) throws UnsupportedEncodingException {
        // 路径转码
        String decodedUrl = URLDecoder.decode(urls, "utf-8");
        String decodedCurrentUrl = URLDecoder.decode(currentUrl, "utf-8");
        // 抽取文件并返回文件列表
        String[] imgs = decodedUrl.split("\\|");
        List imgurls = Arrays.asList(imgs);
        model.addAttribute("imgurls", imgurls);
        model.addAttribute("currentUrl",decodedCurrentUrl);
        return "picture";
    }


    /**
     * 根据url获取文件内容
     * 当pdfjs读取存在跨域问题的文件时将通过此接口读取
     *
     * @param urlPath
     * @param resp
     */
    @RequestMapping(value = "/getCorsFile", method = RequestMethod.GET)
    public void getCorsFile(String urlPath, HttpServletResponse resp) {
        InputStream inputStream = null;
        try {
            String strUrl = urlPath.trim();
            URL url = new URL(strUrl);
            //打开请求连接
            URLConnection connection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = httpURLConnection.getInputStream();
            byte[] bs = new byte[1024];
            int len;
            while (-1 != (len = inputStream.read(bs))) {
                resp.getOutputStream().write(bs, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    /**
     * 通过api接口入队
     * @param url 请编码后在入队
     */
    @GetMapping("/addTask")
    @ResponseBody
    public String addQueueTask(String url) {
        final RBlockingQueue<String> queue = redissonClient.getBlockingQueue(FileConverQueueTask.queueTaskName);
        queue.addAsync(url);
        return "success";
    }

}
