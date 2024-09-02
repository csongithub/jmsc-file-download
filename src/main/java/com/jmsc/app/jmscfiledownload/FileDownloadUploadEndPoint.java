/**
 * 
 */
package com.jmsc.app.jmscfiledownload;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author anuhr
 *
 */
@RestController
@RequestMapping("/v1")
public class FileDownloadUploadEndPoint {

    @Value("${jmsc.server.url.download}")
    private String jmscServerUrl;

 
    @GetMapping("/download/{client_id}/{file_type}/{file_id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("client_id") Long clientId, 
			  									 @PathVariable("file_type") String fileType,
			  									 @PathVariable("file_id") Long fileId,
			  									 HttpServletRequest request) throws Exception {
    
    	
    	String requestTokenHeader = request.getHeader("Authorization");
		if (requestTokenHeader == null)
			requestTokenHeader = request.getHeader("Authorization".toLowerCase());
		
    	if(requestTokenHeader == null)
    		throw new RuntimeException("Unauthorized Request");
    	
    	String url = jmscServerUrl + clientId + "/" + fileType + "/" + fileId;
    	
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
