package com.zhl.ccb.utils;

import java.io.InputStream;

import org.apache.http.HttpEntity;

public class HttpResponser {
	private int code;
	private String content;

	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

}
