package com.jd.lobo.util;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.jd.lobo.cass.CassSessionFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by lutong on 7/27/16.
 */
public class CommentIngest {

	public static Session session = null;


	public static void main(String[] args) {
		CassSessionFactory.init(new String[]{"127.0.0.1"});
		String insertSql = "insert into lobo.comment (id, sku_id, spu_id, user_id, create_time, comment, replies, pic_path, score) " +
				"values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = null;

		session = CassSessionFactory.getSession("lobo");
		ps = session.prepare(insertSql);

		try {
			FileReader fileReader = new FileReader("src/main/resources/comment.csv");
			BufferedReader reader = new BufferedReader(fileReader);
			String line = null;
			List<String> picPath = new ArrayList<>();
			List<String> replies = new ArrayList<>();
			int score = 5;
			reader.readLine();
			Long id = 1l;
			while((line = reader.readLine()) != null){
				String[] fields = line.split(",");

				if(fields.length == 39) {
					Long skuId = Long.valueOf(fields[5]);
					Long spuId = Long.valueOf(fields[0]);
					Long userId = Long.valueOf(fields[6]);
					// String createTime = fields[25];
					Long createTime = System.currentTimeMillis();
					String comment = fields[9];
					BoundStatement bs = ps.bind(id++, skuId, spuId, userId, createTime + randomTime(), comment, replies, picPath, score);

					session.execute(bs);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			session.close();
		}
	}


	static long randomTime(){
		Random rand = new Random();
		int range = 3 * 24 * 3600 * 1000;
		long rst = rand.nextInt(range);
		if(rst % 2 == 0)
			rst = -rst;

		return rst;
	}

}
