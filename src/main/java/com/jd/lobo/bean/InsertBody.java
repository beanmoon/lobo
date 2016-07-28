package com.jd.lobo.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lutong on 7/28/16.
 */
public class InsertBody {
	@SerializedName("sku_id")
	public long skuId;

	@SerializedName("spu_id")
	public long spuId;

	@SerializedName("user_id")
	public Long userId;

	@SerializedName("create_time")
	public Long createTime;

	public String comment;

	@SerializedName("pic_path")
	public List<String> picPath;

	public int score;

	public long getSkuId() {
		return skuId;
	}

	public void setSkuId(long skuId) {
		this.skuId = skuId;
	}

	public long getSpuId() {
		return spuId;
	}

	public void setSpuId(long spuId) {
		this.spuId = spuId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<String> getPicPath() {
		return picPath;
	}

	public void setPicPath(List<String> picPath) {
		this.picPath = picPath;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
}
