package com.jd.lobo.util;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.google.gson.Gson;
import com.jd.lobo.bean.CommentBody;
import com.jd.lobo.bean.CommentRequstBody;
import com.jd.lobo.bean.CommentResult;
import com.jd.lobo.cass.CassQueryBuilder;
import com.jd.lobo.cass.CassSessionFactory;
import com.jd.lobo.config.LoboConstants;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by lutong on 7/27/16.
 */
public class CommentFetcher {
	private Logger logger = LoggerFactory.getLogger(CommentFetcher.class);
	public static Gson gson = new Gson();
	public static CassQueryBuilder cqb = new CassQueryBuilder();
	public static long daytime = 24 * 3600 * 1000l;
	protected Session sessionUr = null;

	public String commentSql = "select token(id), id, sku_id, spu_id, user_id, create_time, comment, replies, pic_path, score" +
			" from lobo.comment where lucene = '{query : {type:\"match\", field:\"spu_id\", value: %s}}' limit ";
	public PreparedStatement ps = null;
	public Mapper<CommentBody> commentMapper = null;
	public Map<Long, List<CommentBody>> cacheMap = new HashMap<>();
	public Map<String, List<CommentBody>> dayCommentCacheMap = new HashMap<>();


	public CommentFetcher() {
		sessionUr = CassSessionFactory.getSession("lobo");
		ps = sessionUr.prepare(commentSql);
		commentMapper = new MappingManager(sessionUr).mapper(CommentBody.class);
		PropertyConfigurator.configure("log4j.properties");
	}

	public CommentResult fetchComment(CommentRequstBody requstBody) {
		CommentResult commentResult = new CommentResult();

		try {
			List<CommentBody> list = new ArrayList<>();

			Long key = requstBody.getSpuId();
			int index = requstBody.getPage() * requstBody.getPageSize();
			int pageSize = requstBody.getPageSize();
			if (cacheMap.containsKey(key) && cacheMap.get(key).size() >= index) {
				list = cacheMap.get(key).subList(index - requstBody.pageSize, index);
			} else {
				// String query = String.format(commentSql, requstBody.spuId) + LoboConstants.DEFAULT_CASS_FETCH_SIZE;
				String query = cqb.buildQueryWithFiltering("lobo.comment", key);
				logger.info("query url: " + query);

				List<CommentBody> tmp = new ArrayList<>();
				ResultSet rs = sessionUr.executeAsync(query).get(LoboConstants.DFEAULT_CASS_FETCH_TIMEOUT, TimeUnit.MILLISECONDS);
				Iterator<Row> rows = rs.iterator();
				while (rows.hasNext()) {
					Row row = rows.next();
					CommentBody body = new CommentBody();
					body.setSpuId(row.getLong("spu_id"));
					body.setComment(row.getString("comment"));
					body.setCreateTime(row.getLong("create_time"));
					body.setId(row.getLong("id"));
					body.setScore(row.getInt("score"));
					body.setSkuId(row.getLong("sku_id"));
					body.setUserId(row.getLong("user_id"));
					tmp.add(body);
				}

				/*
				Result<CommentBody> result = commentMapper.map(rs);

				Iterator<CommentBody> it = result.iterator();
				while (it.hasNext()) {
					CommentBody commentBody = it.next();
					logger.info("commentBody: " + gson.toJson(commentBody));
					tmp.add(it.next());
				}
				*/
				if(cacheMap.containsKey(key)){
					List<CommentBody> oriList = cacheMap.get(key);
					oriList.addAll(tmp);
					cacheMap.put(key, oriList);
					logger.info("list size of spuId {} expanded to {}", key, oriList.size());
				} else {
					cacheMap.put(requstBody.getSpuId(), tmp);
				}

				list = tmp.subList(index - pageSize, index < tmp.size() ? index : tmp.size());
			}
			commentResult.setSpuId(requstBody.spuId);
			commentResult.setResult(list);
			commentResult.setNumber(list.size());
		} catch (TimeoutException e) {
			logger.error("timeout when execute query {}", ps.toString());
		} catch (Exception e) {
			logger.error("exception happened!", e);
		}


		return commentResult;
	}

	public CommentResult fetchCommentByDay(CommentRequstBody requstBody) {
		CommentResult commentResult = new CommentResult();
		List<CommentBody> list = new ArrayList<>();

		try {
			String key = requstBody.getSpuId() + "-" + requstBody.getDayTime();
			Long spuId = requstBody.getSpuId();
			int pageSize = requstBody.getPageSize();
			int index = requstBody.getPage() * pageSize;
			long daytime = requstBody.getDayTime();

			if (dayCommentCacheMap.containsKey(key) && dayCommentCacheMap.get(key).size() >= index) {
				list = dayCommentCacheMap.get(key).subList(index - pageSize, index);
			} else {
				String query = cqb.buildQueryWithFiltering("lobo.comment", spuId, daytime);
				logger.info("query url: " + query);

				ResultSet rs = sessionUr.executeAsync(query).get(LoboConstants.DFEAULT_CASS_FETCH_TIMEOUT, TimeUnit.MILLISECONDS);
				Iterator<Row> rows = rs.iterator();
				List<CommentBody> tmp = new ArrayList<>();
				while (rows.hasNext()) {
					Row row = rows.next();
					CommentBody body = new CommentBody();
					body.setSpuId(row.getLong("spu_id"));
					body.setComment(row.getString("comment"));
					body.setCreateTime(row.getLong("create_time"));
					body.setId(row.getLong("id"));
					body.setScore(row.getInt("score"));
					body.setSkuId(row.getLong("sku_id"));
					body.setUserId(row.getLong("user_id"));
					tmp.add(body);
				}

				if(dayCommentCacheMap.containsKey(key)){
					List<CommentBody> oriList = dayCommentCacheMap.get(key);
					oriList.addAll(tmp);
					dayCommentCacheMap.put(key, oriList);
					logger.info("list size of spuId {} expanded to {}", key, oriList.size());
				} else {
					dayCommentCacheMap.put(key, tmp);
				}

				list = tmp.subList(index - pageSize, index < tmp.size() ? index : tmp.size());
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		}
		commentResult.setSpuId(requstBody.spuId);
		commentResult.setResult(list);
		commentResult.setNumber(list.size());

		return commentResult;
	}

	public Map<Integer, Integer> fetchCountByMonth(CommentRequstBody requstBody) {
		Map<Integer, Integer> rstMap = new LinkedHashMap<>();
		Map<Long, Integer> tmpMap = new HashMap<>();
		int dayCount = 0;

		try {
			Long key = requstBody.getSpuId();
			long createTime = requstBody.getDayTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(createTime));
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			createTime = calendar.getTimeInMillis();
			dayCount = calendar.getActualMaximum(Calendar.DATE);

			String query = cqb.buildQueryWithFiltering("lobo.comment", key, createTime, createTime + daytime * dayCount);
			logger.info("query url: " + query);

			ResultSet rs = sessionUr.executeAsync(query).get(LoboConstants.DFEAULT_CASS_FETCH_TIMEOUT, TimeUnit.MILLISECONDS);
			Iterator<Row> rows = rs.iterator();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			while (rows.hasNext()) {
				Row row = rows.next();
				Date curDate = new Date(row.getLong("create_time"));
				cal.setTime(curDate);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);

				long timeInLong = cal.getTimeInMillis();

				if(tmpMap.containsKey(timeInLong)){
					tmpMap.put(timeInLong, tmpMap.get(timeInLong) + 1);
				} else {
					tmpMap.put(timeInLong, 1);
				}
			}


			for(int i = 0; i < dayCount; i++){
				if(tmpMap.containsKey(createTime + daytime * i)){
					rstMap.put(i + 1, tmpMap.get(createTime + daytime * i));
				} else {
					rstMap.put(i + 1, 0);
				}
			}

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		} catch (TimeoutException e1) {
			e1.printStackTrace();
		}

		return rstMap;
	}
}
