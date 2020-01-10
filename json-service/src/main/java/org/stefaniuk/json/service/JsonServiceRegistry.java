package org.stefaniuk.json.service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * JSON service registry.
 * </p>
 * <p>
 * A registry class that holds all classes provided by a developer that are
 * available to a JSON-RPC client. This is the main class that should be
 * utilised in a user code. Multiple instances can be created or the singleton
 * pattern could be used. The last one is probably more desirable in most use
 * cases.
 * </p>
 * <p>
 * A class can be registered/unregistered by passing a class name as an argument
 * of {@link #register(Class) register}/{@link #unregister(Class) unregister}
 * method. Instance of that class will be created by the service invoker object
 * only once, when a first call is made to any of the exposed methods or when
 * service mapping description is produced.
 * </p>
 * <p>
 * It is user's responsibility to pass incoming HTTP request to the registry
 * object for method to be executed. From inside of a Java servlet this can be
 * achieved by calling
 * {@link #handle(HttpServletRequest, HttpServletResponse, Class) handle} method
 * on the registry object itself:
 * </p>
 * 
 * <pre>
 * registry.handle(request, response, NameOfClass.class);
 * </pre>
 * 
 * <p>
 * From a controller (using Spring Framework) this can be done by calling static
 * method
 * {@link JsonServiceUtil#handle(JsonServiceRegistry, HttpServletRequest, HttpServletResponse, Class)
 * handle} from {@link JsonServiceUtil} class:
 * </p>
 * 
 * <pre>
 * JsonServiceUtil.handle(registry, request, response, NameOfClass.class);
 * </pre>
 * 
 * @author Daniel Stefaniuk
 * @version 1.0.0
 * @since 2010/09/20
 */
public class JsonServiceRegistry {

    private final Logger logger = LoggerFactory.getLogger(JsonServiceRegistry.class);

    /* Singleton object of JsonServiceRegistry. */
    private static JsonServiceRegistry INSTANCE = null;

    /**
     * This object provides functionality for conversion between Java objects
     * and JSON.
     */
    private static ObjectMapper mapper = new ObjectMapper();

    /** Collection of all the registered JSON-RPC classes. */
    private Map<String, JsonServiceInvoker> registry =
        Collections.synchronizedMap(new HashMap<String, JsonServiceInvoker>());

    /**
     * Constructor
     */
    public JsonServiceRegistry() {
    }

    /**
     * Singleton pattern provided out of the box.
     * 
     * @return Returns {@link JsonServiceRegistry} singleton object.
     */
    public static JsonServiceRegistry getInstance() {

        // enter synchronised block only if necessary
        if(INSTANCE == null) {
            synchronized(JsonServiceRegistry.class) {
                // needs to be check again due to possible race condition
                if(INSTANCE == null) {
                    INSTANCE = new JsonServiceRegistry();
                }
            }
        }

        return INSTANCE;
    }

    /**
     * Is this object a singleton?
     * 
     * @return
     */
    public boolean isSingleton() {

        return this == INSTANCE;
    }

    /**
     * Registers a class to make it available to a JSON-RPC client.
     * 
     * @param clazz Class
     * @return Returns {@link JsonServiceRegistry} object.
     */
    public JsonServiceRegistry register(Class<?> clazz) {

        String name = clazz.getName();
        if(!registry.containsKey(name)) {
            registry.put(name, new JsonServiceInvoker(clazz));
            logger.info("JSON-RPC registered class: " + name);
        }

        return this;
    }

    /**
     * Registers class by passing its instance to make it available to a
     * JSON-RPC client.
     * 
     * @param obj Instance of a class.
     * @return Returns {@link JsonServiceRegistry} object.
     */
    public JsonServiceRegistry register(Object obj) {

        String name = obj.getClass().getName();
        if(!registry.containsKey(name)) {
            registry.put(name, new JsonServiceInvoker(obj));
            logger.info("JSON-RPC registered class: " + name);
        }

        return this;
    }

    /**
     * Registers an array of classes to make them available to a JSON-RPC
     * client.
     * 
     * @param classes Array of classes.
     * @return Returns {@link JsonServiceRegistry} object.
     */
    public JsonServiceRegistry setRegistry(Class<?>[] classes) {

        registry.clear();
        for(Class<?> clazz: classes) {
            register(clazz);
        }

        return this;
    }

    /**
     * Unregisters class.
     * 
     * @param clazz Class
     * @return Returns {@link JsonServiceRegistry} object.
     */
    public JsonServiceRegistry unregister(Class<?> clazz) {

        String name = clazz.getName();
        if(registry.containsKey(name)) {
            registry.remove(name);
            logger.info("JSON-RPC unregistered class: " + name);
        }

        return this;
    }

    /**
     * Unregisters class by passing its instance.
     * 
     * @param obj Instance of a class.
     * @return Returns {@link JsonServiceRegistry} object.
     */
    public JsonServiceRegistry unregister(Object obj) {

        String name = obj.getClass().getName();
        if(registry.containsKey(name)) {
            registry.remove(name);
            logger.info("JSON-RPC unregistered class: " + name);
        }

        return this;
    }

    /**
     * Removes all JSON-RPC objects from the registry.
     * 
     * @return Returns {@link JsonServiceRegistry} object.
     */
    public JsonServiceRegistry clearRegistry() {

        registry.clear();
        logger.info("JSON-RPC unregistered all classes");

        return this;
    }

    /**
     * Looks up class in registry.
     * 
     * @param clazz Class
     * @return Returns {@link JsonServiceInvoker} object.
     */
    private JsonServiceInvoker lookup(Class<?> clazz) {

        // FIXME: throw "the" exception if class is not in the registry

        return registry.get(clazz.getName());
    }

    /**
     * Produces Service Mapping Description for a given JSON-RPC class.
     * 
     * @param clazz Class
     * @param os Output stream
     * @return Returns output stream.
     */
    public OutputStream getServiceMap(Class<?> clazz, OutputStream os) {

        try {
            mapper.writeValue(os, lookup(clazz).getServiceMap());
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
        }

        return os;
    }

    /**
     * Produces Service Mapping Description for a given JSON-RPC class.
     * 
     * @param clazz Class
     * @param response HTTP response
     * @return Returns output stream.
     */
    public OutputStream getServiceMap(Class<?> clazz, HttpServletResponse response) {

        OutputStream os = null;

        try {
            os = getServiceMap(clazz, response.getOutputStream());
        }
        catch(IOException e) {
            logger.error(e.getMessage(),e);
        }

        return os;
    }

    /**
     * Handles request as an input stream.
     * 
     * @param is Input stream
     * @param os Output stream
     * @param clazz Class
     * @return Returns output stream.
     */
    public OutputStream handle(InputStream is, OutputStream os, Class<?> clazz) {
    	JsonNode node = null;
    	JsonServiceInvoker invoker = null;
        try {
        	node = mapper.readValue(is, JsonNode.class);
        	invoker = lookup(clazz);
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(JsonServiceError.INVALID_REQUEST);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }
        try {
            handleNode(null, node , os, invoker);
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
            	JsonServiceError error = JsonServiceError.INVALID_REQUEST;
            	error.setMessage(e.getMessage());
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(error);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }

        return os;
    }

    /**
     * Handles request as an input stream.
     * 
     * @param is Input stream
     * @param os Output stream
     * @param clazz Class
     * @param method Method to call.
     * @param args Arguments passed to the method.
     * @return Returns output stream.
     */
    public OutputStream handle(InputStream is, OutputStream os, Class<?> clazz, String method, Object... args) {
    	JsonNode node = null;
    	JsonServiceInvoker invoker = null;
        try {
        	invoker = lookup(clazz);
        }catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(JsonServiceError.INVALID_REQUEST);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }

        try {
            handleNode(null, os, invoker, method, args);
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
            	JsonServiceError error = JsonServiceError.INVALID_REQUEST;
            	error.setMessage(e.getMessage());
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(error);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }

        return os;
    }

    /**
     * Handles HTTP request.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param clazz Class
     * @return Returns output stream.
     */
    public OutputStream handle(HttpServletRequest request, HttpServletResponse response, Class<?> clazz) {

        BufferedOutputStream bos = null;

        try {

            // make sure class is registered, so there is no need to do this manually
            register(clazz);

            // get output stream
            bos = new BufferedOutputStream(response.getOutputStream());

            // return SMD or call a method
            String method = request.getMethod();
            if(method.equals("GET")) {
                getServiceMap(clazz, bos);
            }
            else {
                handle(request, bos, clazz);
            }
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
        }

        return bos;
    }

    /**
     * Handles HTTP request.
     * 
     * @param request HTTP request
     * @param response HTTP response
     * @param obj Already instantiated object
     * @return Returns output stream.
     */
    public OutputStream handle(HttpServletRequest request, HttpServletResponse response, Object obj) {

        BufferedOutputStream bos = null;

        try {

            // make sure object is registered
            register(obj);

            // get output stream
            bos = new BufferedOutputStream(response.getOutputStream());

            // return SMD or call a method
            Class<?> clazz = obj.getClass();
            String method = request.getMethod();
            if(method.equals("GET")) {
                getServiceMap(clazz, bos);
            }
            else {
                handle(request, bos, clazz);
            }
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
        }

        return bos;
    }

    /**
     * Handles HTTP request.
     * 
     * @param request HTTP request
     * @param os Output stream
     * @param clazz Class
     * @return Returns output stream.
     */
    public OutputStream handle(HttpServletRequest request, OutputStream os, Class<?> clazz) {
       	JsonNode node = null;
    	JsonServiceInvoker invoker = null;
        try {
        	node = mapper.readValue(request.getInputStream(), JsonNode.class);
        	invoker = lookup(clazz);
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(JsonServiceError.INVALID_REQUEST);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }

        try {
            handleNode(request, node, os, invoker);
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
            	JsonServiceError error = JsonServiceError.INVALID_REQUEST;
            	Throwable t=null;
            	if(e instanceof InvocationTargetException){
            		t=((InvocationTargetException)e).getTargetException();
            	}else{
                	t=e;
            	}
            	error.setMessage(t.getMessage());
            	if(error.getData()==null){
	            	ByteArrayOutputStream str=new ByteArrayOutputStream();
	            	PrintWriter pw=new PrintWriter(str,true);
	                try {
	                	t.printStackTrace(pw);
	                	error.setData(URLEncoder.encode(str.toString(),"UTF-8"));
	                } catch (IOException e1) {
	        		}finally{
	        			if(pw!=null){try{pw.close();}catch(Exception e2){}}
	        			if(str!=null){try{str.close();}catch(Exception e2){}}
	        		}
            	}	
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(error);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }

        return os;
    }

    /**
     * Handles HTTP request.
     * 
     * @param request HTTP request
     * @param os Output stream
     * @param clazz Class
     * @param method Method to call.
     * @param args Arguments passed to the method.
     * @return Returns output stream.
     */
    public OutputStream handle(HttpServletRequest request, OutputStream os, Class<?> clazz, String method,
            Object... args) {
       	JsonServiceInvoker invoker = null;
        try {
        	invoker = lookup(clazz);
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(JsonServiceError.INVALID_REQUEST);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }
        
        try {
            handleNode(request, os, invoker, method, args);
        }
        catch(Exception e) {
            logger.error(e.getMessage(),e);
            // send "Invalid Request" response object
            try {
            	JsonServiceError error = JsonServiceError.INVALID_REQUEST;
            	if(e instanceof InvocationTargetException)
            		error.setMessage(((InvocationTargetException)e).getTargetException().getMessage());
            	else
                	error.setMessage(e.getMessage());
                ObjectNode response = JsonServiceUtil.getJsonServiceErrorNode(error);
                logger.debug("JSON-RPC response: " + response.toString());
                mapper.createObjectNode();
                mapper.writeValue(os, response);
            }
            catch(Exception ex) {
                logger.error(ex.getMessage(),ex);
            }
        }

        return os;
    }

    /**
     * Handles HTTP request.
     * 
     * @param request HTTP request
     * @param os Output stream
     * @param invoker This is the service invoker object.
     * @param method Method to call.
     * @param args Arguments passed to the method.
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws JsonServiceException
     */
    private void handleNode(HttpServletRequest request, OutputStream os, JsonServiceInvoker invoker, String method,
            Object... args) throws JsonGenerationException, JsonMappingException, IOException, IllegalAccessException,
            InvocationTargetException, JsonServiceException {

        handleObject(request, os, invoker, method, args);
    }

    /**
     * Handles HTTP request.
     * 
     * @param request HTTP request
     * @param requestNode HTTP request provided as {@link JsonNode}
     * @param os Output stream
     * @param invoker This is the service invoker object.
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws JsonServiceException
     */
    private void handleNode(HttpServletRequest request, JsonNode requestNode, OutputStream os,
            JsonServiceInvoker invoker) throws JsonGenerationException, JsonMappingException, IOException,
            IllegalAccessException, InvocationTargetException, JsonServiceException {

        if(requestNode.isObject()) {
            handleObject(request, ObjectNode.class.cast(requestNode), os, invoker);
        }
        else if(requestNode.isArray()) {
            handleArray(request, ArrayNode.class.cast(requestNode), os, invoker);
        }
        else {
            throw new JsonServiceException(JsonServiceError.INVALID_REQUEST);
        }
    }

    /**
     * Handles HTTP request.
     * 
     * @param request
     * @param requestNode
     * @param os
     * @param invoker
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws JsonServiceException
     */
    private void handleArray(HttpServletRequest request, ArrayNode requestNode, OutputStream os,
            JsonServiceInvoker invoker) throws JsonGenerationException, JsonMappingException, IOException,
            IllegalAccessException, InvocationTargetException, JsonServiceException {

        for(int i = 0; i < requestNode.size(); i++) {
            handleNode(request, requestNode.get(i), os, invoker);
        }
    }

    /**
     * Handles HTTP request.
     * 
     * @param request
     * @param os
     * @param invoker
     * @param method
     * @param args
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    private void handleObject(HttpServletRequest request, OutputStream os, JsonServiceInvoker invoker, String method,
            Object... args) throws IllegalAccessException, InvocationTargetException, JsonGenerationException,
            JsonMappingException, IOException {

        JsonNode responseNode = invoker.process(request, method, args);

        mapper.setPropertyNamingStrategy(new UnmodifiedPropertyNamingStrategy());
        mapper.writeValue(os, responseNode);
    }

    /**
     * Handles HTTP request.
     * 
     * @param request
     * @param requestNode
     * @param os
     * @param invoker
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    private void handleObject(HttpServletRequest request, ObjectNode requestNode, OutputStream os,
            JsonServiceInvoker invoker) throws IllegalAccessException, InvocationTargetException,
            JsonGenerationException, JsonMappingException, IOException {

        JsonNode responseNode = invoker.process(request, requestNode);
        
        mapper.setPropertyNamingStrategy(new UnmodifiedPropertyNamingStrategy());
        mapper.writeValue(os, responseNode);
    }

}
