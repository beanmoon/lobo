package com.jd.lobo.bean;

import com.google.gson.annotations.SerializedName;
import com.jd.lobo.config.LoboConstants;

/**
 * Created by lutong on 7/27/16.
 */
public class CommentRequstBody {
	@SerializedName("spu_id")
	public long spuId = 0l;

	public int page = 1;

	@SerializedName("page_size")
	public Integer pageSize = 10;

	@SerializedName("day_time")
	public long dayTime;

	public long getDayTime() {
		return dayTime;
	}

	public void setDayTime(long dayTime) {
		this.dayTime = dayTime;
	}

	public long getSpuId() {
		return spuId;
	}

	public void setSpuId(long spuId) {
		this.spuId = spuId;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public Integer getPageSize() {
		if(pageSize == null)
			return LoboConstants.DEFAULT_PAGE_SIZE;

		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
}
