package com.jd.lobo.bean;

import java.util.List;

/**
 * Created by lutong on 7/27/16.
 */
public class CommentResult {
	public long spuId;

	public int number;

	public List<CommentBody> result;

	public long getSpuId() {
		return spuId;
	}

	public void setSpuId(long spuId) {
		this.spuId = spuId;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<CommentBody> getResult() {
		return result;
	}

	public void setResult(List<CommentBody> result) {
		this.result = result;
	}
}
