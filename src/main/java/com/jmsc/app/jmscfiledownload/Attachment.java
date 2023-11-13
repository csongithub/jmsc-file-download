package com.jmsc.app.jmscfiledownload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Attachment {


    private String id;
    private String fileName;
    private String fileType;
    private byte[] data;

    public Attachment(String id, String fileName, String fileType, byte[] data) {
    	this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.data = data;
    }
}
