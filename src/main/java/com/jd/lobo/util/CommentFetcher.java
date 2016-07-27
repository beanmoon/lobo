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
import com.jd.lobo.cass.CassSessionFactory;
import com.jd.lobo.config.LoboConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by lutong on 7/27/16.
 */
public class CommentFetcher {
	private Logger logger = LoggerFactory.getLogger(CommentFetcher.class);
	public static Gson gson = new Gson();

	protected Session sessionUr = null;

	public String commentSql = "select token(id), id, sku_id, spu_id, user_id, create_time, comment, replies, pic_path, score" +
			" from lobo.comment where lucene = '{query : {type:\"match\", field:\"spu_id\", value: %s}}' limit ";
	public PreparedStatement ps = null;
	public Mapper<CommentBody> commentMapper = null;
	public Map<Long, List<CommentBody>> cacheMap = new HashMap<>();


	public CommentFetcher () {
		sessionUr = CassSessionFactory.getSession("lobo");
		ps = sessionUr.prepare(commentSql);
		commentMapper = new MappingManager(sessionUr).mapper(CommentBody.class);
	}

	public CommentResult fetchComment(CommentRequstBody requstBody){
		CommentResult commentResult = new CommentResult();

		try {
			List<CommentBody> list = new ArrayList<>();

			Long key = requstBody.getSpuId();
			int index = requstBody.getPage() * requstBody.getPageSize();
			if(cacheMap.containsKey(key) && cacheMap.get(key).size() < index){
				list = cacheMap.get(key).subList(index - requstBody.pageSize, index);
			} else {
				String query = String.format(commentSql, requstBody.spuId) + LoboConstants.DEFAULT_CASS_FETCH_SIZE;
				logger.info("query url: " + query);


				List<CommentBody> tmp = new ArrayList<>();
				ResultSet rs = sessionUr.executeAsync(query).get(LoboConstants.DFEAULT_CASS_FETCH_TIMEOUT, TimeUnit.MILLISECONDS);
				Iterator<Row> rows = rs.iterator();
				while(rows.hasNext()){
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
				cacheMap.put(requstBody.getSpuId(), tmp);
				list = tmp.subList(index - requstBody.pageSize, index);
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
}
