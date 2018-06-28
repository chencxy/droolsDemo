package com.cxy.demo.droolsDemo.pojo;

import java.util.HashMap;

public class SimpleKV extends HashMap<String, Object>{

	private static final long serialVersionUID = 2168642522119693074L;
	
	public SimpleKV(String key, String value) {
		super();
		super.put(key, value);
	}
	
}
