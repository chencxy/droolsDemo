package com.cxy.demo.droolsDemo.pojo;

public class SimpleRisk {

	private String name;
	private int score;
	private String reason;
	
	public SimpleRisk(String name, int score, String reason) {
		super();
		this.name = name;
		this.score = score;
		this.reason = reason;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	
}
