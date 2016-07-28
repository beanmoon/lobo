package com.jd.lobo.cass;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CassQueryBuilder {

	private static Logger logger = LoggerFactory.getLogger(CassQueryBuilder.class);
	private static long daytime = 24 * 3600 * 1000l;

	public String buildQueryWithFiltering(String tableName, Long spuId, Long createTime){
		return  buildQueryWithFiltering(tableName, spuId, createTime, createTime + daytime);
	}

	public String buildQueryWithFiltering(String tableName, Long spuId, Long lowerTime, Long upperTime){
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		List<StringBuilder> headFilters = new ArrayList<StringBuilder>();
		if(spuId != null){
			StringBuilder spuIdFilter = buildMatchFilter("spu_id", spuId);
			if(spuIdFilter.length() > 0)
				headFilters.add(spuIdFilter);
		}

		if (lowerTime != null) {
			StringBuilder createTimeFilter = buildRangeFilter("create_time", lowerTime, upperTime);
			if (createTimeFilter.length() > 0) {
				headFilters.add(createTimeFilter);
			}
		}
		
		StringBuilder headFilter = buildMustFilter(headFilters);
		String query;
		if (headFilter.length() > 0) {
			sb.append("filter : ");
			sb.append(headFilter);
			sb.append("}");
			//String query = String.format("select * from %s where lucene='%s' limit 300", tableName, sb.toString());
			query = String.format("select token(id), id, sku_id, spu_id, user_id, create_time, comment, replies, pic_path, score " +
					"from %s where lucene='%s'", tableName, sb.toString());
		} else {
			query = String.format("select token(id), id, sku_id, spu_id, user_id, create_time, comment, replies, pic_path, score " +
					"from %s", tableName);
		}
		
		logger.debug("Activity pool reload query: {}", query);
		
		return query;
	}

	protected StringBuilder buildMatchFilter(String fieldName, Long value) {
		StringBuilder sb = new StringBuilder();

		if (StringUtils.isNotBlank(fieldName) && value != null) {
			sb.append(String.format("{ type : \"match\", field:\"%s\", value: %s}", fieldName, value));
		}

		return sb;
	}

	protected StringBuilder buildRangeFilter(String fieldName, Long lower, Long upper) {
		StringBuilder sb = new StringBuilder();

		sb.append(String.format("{ type : \"range\", field:\"%s\", lower:%s, upper: %s }", fieldName, lower, upper));

		return sb;
	}

	protected StringBuilder buildMustFilter(StringBuilder... subFilters) {
		StringBuilder sb = new StringBuilder();

		if (subFilters != null && subFilters.length > 0) {
			sb.append("{ type : \"boolean\", must:[");
			sb.append(StringUtils.join(subFilters, ","));
			sb.append("]}");
		}
		return sb;
	}

	protected StringBuilder buildMustFilter(List<StringBuilder> subFilters) {
		return buildMustFilter(subFilters.toArray(new StringBuilder[subFilters.size()]));
	}
}
