package com.jd.lobo.bean;

import com.datastax.driver.mapping.annotations.Table;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by lutong on 7/27/16.
 */
@Table(keyspace = "lobo", name = "comment")
public class CommentBody {
	public long id;

	@SerializedName("sku_id")
	public long skuId;

	@SerializedName("spu_id")
	public long spuId;

	@SerializedName("user_id")
	public Long userId;

	@SerializedName("create_time")
	public Long createTime;

	public String comment;

	public List<String> replies;

	@SerializedName("pic_path")
	public List<String> picPath;

	public int score;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	public List<String> getReplies() {
		return replies;
	}

	public void setReplies(List<String> replies) {
		this.replies = replies;
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

class Reply {
	@SerializedName("user_id")
	public Long userId;

	@SerializedName("create_time")
	public Long createTime;

	public String reply;

	@SerializedName("pic_path")
	public List<String> picPath;

	public int score;

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

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
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
