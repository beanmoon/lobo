package com.jd.lobo.util;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.jd.lobo.bean.CommentBody;
import com.jd.lobo.bean.CommentResult;
import com.jd.lobo.cass.CassSessionFactory;
import com.jd.lobo.config.LoboConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by lutong on 7/27/16.
 */
public class CommentFetcher {
	private Logger logger = LoggerFactory.getLogger(CommentFetcher.class);

	protected Session sessionUr = null;

	public String commentSql = "select * from lobo.comment where lucene = ?";
	public PreparedStatement ps = null;
	public Mapper<CommentBody> commentMapper = null;


	public CommentFetcher () {
		sessionUr = CassSessionFactory.getSession("lobo");
		ps = sessionUr.prepare(commentSql);
		commentMapper = new MappingManager(sessionUr).mapper(CommentBody.class);
	}

	public CommentResult fetchComment(long spuId){
		CommentResult commentResult = null;

		try {
			ResultSet rs = sessionUr.executeAsync(ps.bind(spuId)).get(LoboConstants.DFEAULT_CASS_FETCH_TIMEOUT, TimeUnit.MILLISECONDS);
			Result<CommentBody> result = commentMapper.map(rs);

			List<CommentBody> list = new ArrayList<>();
			Iterator<CommentBody> it = result.iterator();
			while(it.hasNext()){
				list.add(it.next());
			}

			commentResult.setSpuId(spuId);
			commentResult.setResult(list);
			commentResult.setNumber(list.size());
		} catch (TimeoutException e) {
			logger.error("timeout when execute query {}", ps.toString());
		} catch (Exception e) {
			logger.error("exception happened!");
		}


		return commentResult;
	}
}
