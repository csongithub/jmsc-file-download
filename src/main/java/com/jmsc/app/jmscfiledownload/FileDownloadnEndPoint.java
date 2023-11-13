package com.jmsc.app.jmscfiledownload;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/v1/file")
public class FileDownloadnEndPoint {
    
    private static Map<String, Attachment> db = new HashMap<String, Attachment>();

    @Value("${jmsc.server.url}")
    private String jmscServerUrl;

    @PostMapping("/upload")
    public ResponseData uploadFile(@RequestParam("file")MultipartFile file) throws Exception {
        Attachment attachment = null;
        String downloadURl = "";
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
             if(fileName.contains("..")) {
                 throw  new Exception("Filename contains invalid path sequence "
                 + fileName);
             }

             attachment= new Attachment(fileName,
             									  fileName,
             									  file.getContentType(),
             									  file.getBytes());
             db.put(fileName, attachment);
             

        } catch (Exception e) {
             throw new Exception("Could not save File: " + fileName);
        }

        return new ResponseData(attachment.getFileName(),
                downloadURl,
                file.getContentType(),
                file.getSize());
    }

    @GetMapping("/download_poc/{fileId}")
    public ResponseEntity<Resource> downloadFilePOC(@PathVariable String fileId) throws Exception {
        Attachment file = db.get(fileId);
        return  ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(new ByteArrayResource(file.getData()));
    }
    
    
    @GetMapping("/download/{client_id}/{directory_id}/{file_id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("client_id") Long clientId, 
			  									 @PathVariable("directory_id") Long directoryId,
			  									 @PathVariable("file_id") Long fileId,
			  									 HttpServletRequest request) throws Exception {
    
    	
    	String requestTokenHeader = request.getHeader("Authorization");
		if (requestTokenHeader == null)
			requestTokenHeader = request.getHeader("Authorization".toLowerCase());
		
    	if(requestTokenHeader == null)
    		throw new RuntimeException("Unauthorized Request");
    	
//    	String url = "http://localhost:8001/jmsc/api/v1/drive/download_file/" + clientId + "/" + directoryId + "/" + fileId;
    	String url = jmscServerUrl + clientId + "/" + directoryId + "/" + fileId;
    	
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", requestTokenHeader);
        	 
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//        System.out.println("Result - status ("+ response.getStatusCode() + ") has body: " + response.hasBody());
        
        if(!HttpStatus.OK.equals(response.getStatusCode()))
        	throw new RuntimeException("HTTP Error: " + response.getStatusCode());
        
  
        String fileJson = response.getBody(); 
        File file = new ObjectMapper().readValue(fileJson, File.class);
        
    	return  ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .body(new ByteArrayResource(file.getData()));
    }
}
