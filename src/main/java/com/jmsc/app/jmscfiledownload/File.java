/**
 * 
 */
package com.jmsc.app.jmscfiledownload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author anuhr
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {
	
	private byte[] data;
	
	private String fileName;
	
	private String contentType;
}
