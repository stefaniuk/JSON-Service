package org.stefaniuk.json.service;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.AnnotatedField;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;
import org.codehaus.jackson.map.introspect.AnnotatedParameter;

public class UnmodifiedPropertyNamingStrategy extends PropertyNamingStrategy{

	@Override
	public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam,
			String defaultName) {
		return super.nameForConstructorParameter(config, ctorParam, defaultName);
	}

	@Override
	public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
		return super.nameForField(config, field, defaultName);
	}

	@Override
	public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
		if(method.getName().startsWith("get")){
			return method.getName().substring(3,4).toLowerCase()+method.getName().substring(4);
		}else if(method.getName().startsWith("is")){
			return method.getName().substring(2,3).toLowerCase()+method.getName().substring(3);
		}else{
			return defaultName;
		}
	}

	@Override
	public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
		if(method.getName().startsWith("set")){
			return method.getName().substring(3,1).toLowerCase()+method.getName().substring(4);
		}else{
			return defaultName;
		}	}

}
