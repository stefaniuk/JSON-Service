package org.stefaniuk.json.mappers;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import org.codehaus.jackson.map.ser.std.SerializerBase;
import org.stefaniuk.json.mappers.types.DataURI;

public class DataUriMapper  {
	    
	    public static class DataURISerializer extends SerializerBase<DataURI>{

	    	public DataURISerializer() {
				super(DataURI.class);
			}
	    	
			@Override
			public void serialize(DataURI data, JsonGenerator jgen, SerializerProvider sp)
					throws IOException, JsonGenerationException {
				 jgen.writeString(data.toString());
			}
	    	
	    }
	    
	    public static class DataURIDeserializer extends StdDeserializer<DataURI>{
	    	public DataURIDeserializer() {
	    		this(null);
			}
	    	
	    	public DataURIDeserializer(Class<?> vc) { 
	            super(vc); 
	        }
	    	
	    	@Override
	        public DataURI deserialize(JsonParser jp, DeserializationContext ctxt) 
	          throws IOException, JsonProcessingException {
	            JsonNode node = jp.getCodec().readTree(jp);
	            return new DataURI(node.getTextValue());
	        }
	    }
}
