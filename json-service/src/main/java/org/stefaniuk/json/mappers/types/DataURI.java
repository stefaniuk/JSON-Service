package org.stefaniuk.json.mappers.types;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * As defined in http://tools.ietf.org/html/rfc2397
 *
 * @author Pere Joseph Rodríguez
 *
 */
public class DataURI  implements Serializable{

	private static final long serialVersionUID = 1L;
	
	
	protected String mediaType=null;
	protected String charset=null;
	protected byte[] data=null;
	
	public DataURI(){
		
	}
	
	public DataURI(String uri) throws UnsupportedEncodingException {
		parse(uri);
	}
	
	public void parse(String uri) throws UnsupportedEncodingException{
		String[] parts=uri.substring(uri.indexOf("data:")+5).split(",");
		if(parts.length==1){
			if(getCharset()==null)
				setData(parts[0].getBytes("ASCII"));
			else
				setData(parts[0].getBytes(getCharset()));
		}else if(parts.length>1  && parts[0].indexOf(";")!=-1){
			String newParts[]=new String[2];
			newParts[0]=parts[0];
			newParts[1]="";
			for (int i=1; i<parts.length;i++){
				newParts[1]+=parts[i];
			}
			processParts(newParts);
		}else if(parts.length>1  && parts[0].indexOf(";")==-1){
			String str_data = "";
			for (int i=1; i<parts.length;i++){
				str_data+=parts[i];
			}
			if(getCharset()==null)
				setData(str_data.getBytes("ASCII"));
			else
				setData(str_data.getBytes(getCharset()));
		}		
	}

	public String toString(){
		String output="data:";
		
		if(getMediaType()!=null)
			output+=getMediaType();
		
		if(getCharset()!=null){
			if(getMediaType()==null)
				output+="text/plain";
			output+=";"+getCharset();
		}
		if(!("ASCII".equals(getCharset())) ){
			if(getMediaType()!=null || getCharset()!=null)
				output+=";";
			try {
				output+="base64,"+encodeBase64(getData());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}else{
			if(getMediaType()!=null || getCharset()!=null)
				output+=",";
			try {
				output+=encodeUriString(getData());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return output;
	}

	protected String encodeUriString(byte[] data2) throws UnsupportedEncodingException {
		return new String(Base64.encodeBase64(data2,false,true),"ASCII");
	}

	protected String encodeBase64(byte[] data2) throws UnsupportedEncodingException {
		return new String(Base64.encodeBase64(data2,false,true),"ASCII");
	}

	protected byte[] decodeBase64(String string) throws UnsupportedEncodingException {
		return Base64.decodeBase64(string);
	}

	
	private void processParts(String[] parts) throws UnsupportedEncodingException {
		String[] subParts = parts[0].split(";");
		boolean isBase64=false;
		for (int i=0;i<subParts.length;i++){
			if("base64".equals(subParts[i])){
				isBase64=true;
			}else if(i==0){
				setMediaType(subParts[i]);
			}else  if(i==1){
				setCharset(subParts[i]);
			}
		}
		if(isBase64) 
			processBase64Data(parts[1]);
		else 
			setData(parts[1].getBytes(getCharset()));

	}

	private void processBase64Data(String string) throws UnsupportedEncodingException {
		setData(decodeBase64(string));
	}

	public String getMediaType() {
		return mediaType;
	}
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
}
