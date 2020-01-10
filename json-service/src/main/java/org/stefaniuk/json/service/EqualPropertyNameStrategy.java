package org.stefaniuk.json.service;

import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.codehaus.jackson.map.introspect.AnnotatedMethod;

public class EqualPropertyNameStrategy extends PropertyNamingStrategy {
	@Override
	public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
		return method.getName().substring(3,4).toLowerCase()+method.getName().substring(4);
	}
	@Override
	public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
		return method.getName().substring(3,4).toLowerCase()+method.getName().substring(4);
	}

}