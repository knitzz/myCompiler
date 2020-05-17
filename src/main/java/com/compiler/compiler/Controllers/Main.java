package com.compiler.compiler.Controllers;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.Mimetypes;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class Main {
	//public AWSCredentials credentials = new BasicAWSCredentials("AKIA3XX4M6R4UU2YGP6X","K0CNmBSFUyMxyXNlo69YnByRTJ85foESaVy0VR8H");
	
	@Autowired
	public AmazonS3 s3client;
	   
	   @GetMapping("/test")
	   public String test() {
		   return "test passed";
	   }
	  
	   @CrossOrigin
	   @RequestMapping(value="/uploadFileToS3Bucket", method = RequestMethod.POST)
	   public String uf(@RequestParam("uploadingFiles") MultipartFile[] uploadingFiles,@RequestParam("uploadingCode") String code,@RequestParam("language") String language) throws IOException, InterruptedException{
		   System.out.println("uploadingcode "+code);
		   System.out.println("languagr "+language);
		   File file = null ;
		   String fileName="";
		   if(!"null".equalsIgnoreCase(code)) {
			   System.out.println("code "+code);
			   fileName=randomStringGenerator()+"."+language;
			   file=new File(System.getProperty("user.dir") +"/"+fileName);
			   FileWriter myWriter = new FileWriter(fileName);
			      myWriter.write(code);
			      myWriter.close();
			      try {
		            	s3client.putObject(
		            		    new PutObjectRequest( "knitzzmycompiler",fileName, file));
		            }
		            catch(Exception e){
		            	return e.getMessage();
		            }
		   }
		   else {
		   for(MultipartFile uploadedFile : uploadingFiles) {
			   fileName=uploadedFile.getOriginalFilename();
	             file = new File( System.getProperty("user.dir") +"/"+fileName);
	            
	            uploadedFile.transferTo(file);
	            try {
	            	s3client.putObject(
	            		    new PutObjectRequest( "knitzzmycompiler", uploadedFile.getOriginalFilename(), file));
	            }
	            catch(Exception e){
	            	return e.getMessage();
	            }
	            
		   }
		
		   }
			   
		   
	        ProcessBuilder processBuilder = new ProcessBuilder();
	        processBuilder.command("sh", "-c", "g++ "+fileName);
	        File ef=new File("compilationerror.txt");
	        ef.delete();
	        ef.createNewFile();
	        processBuilder.redirectError(ef);
	        Process process = processBuilder.start(); 
	        String str="";
	        int b1=process.waitFor();
	        if(b1==1) {
	        	 Scanner sc = new Scanner(ef); 
	        	    while (sc.hasNextLine()) 
	        	    	str=str+sc.nextLine();
	        	    sc.close();
	        	return "failed becuase of compilation error : \n"+str;
	        	}
	        
	        
	        
	        ProcessBuilder processBuilder2 = new ProcessBuilder();
	        processBuilder2.command("sh", "-c", "./a.out");
	        File ef2=new File("runtimeerror.txt");
	        ef2.delete();
	        ef2.createNewFile();
	        File of=new File("output.txt");
	        of.delete();
	        of.createNewFile();
	        processBuilder2.redirectError(ef2);
	        processBuilder2.redirectOutput(of);
	        Process p2=processBuilder2.start();
	        String str2="";
			boolean finished_in_time=p2.waitFor(5, TimeUnit.SECONDS);  // let the process run for 5 seconds
			p2.destroy();                     // tell the process to stop
			//p2.waitFor(10, TimeUnit.SECONDS); // give it a chance to stop
			p2.destroyForcibly();             // tell the OS to kill the process
			int b2=p2.waitFor();                     // the process is now dead
			System.out.println(b2);
			
			if(!finished_in_time)
				return "taking more time then it should, check your code for infinite loop or any user input required";
		     if(b2!=0) {
		    	str2="runtime error \n";
	        	 Scanner sc2 = new Scanner(ef2); 
	        	    while (sc2.hasNextLine()) 
	        	    	str2=str2+sc2.nextLine();
	        	    sc2.close();
	        	return str2;
	        	}
		     
	        	 Scanner sc3 = new Scanner(of); 
	        	    while (sc3.hasNextLine()) 
	        	    	str2=str2+sc3.nextLine();
	        	sc3.close();
	     
	        return str2;
	        
		   
		
	   }
	   
	   public String randomStringGenerator() {

		    int leftLimit = 97; // letter 'a'
		    int rightLimit = 122; // letter 'z'
		    int targetStringLength = 10;
		    Random random = new Random();
		    StringBuilder buffer = new StringBuilder(targetStringLength);
		    for (int i = 0; i < targetStringLength; i++) {
		        int randomLimitedInt = leftLimit + (int) 
		          (random.nextFloat() * (rightLimit - leftLimit + 1));
		        buffer.append((char) randomLimitedInt);
		    }
		    String generatedString = buffer.toString();
		    return generatedString;
	   }
	   
}
