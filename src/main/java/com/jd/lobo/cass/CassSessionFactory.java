package com.jd.lobo.cass;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.FallthroughRetryPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.jd.lobo.config.LoboConstants;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class CassSessionFactory {

	private static Map<String, Session> sessions = null;
	private static Map<String, Session> standaloneSessions = new HashMap<String, Session>();
	private static Cluster cluster = null;

	@SuppressWarnings("unused")
	public static void init(String[] hosts, int fetchSize) {
		if (sessions != null) {
			return;
		}
		synchronized (CassSessionFactory.class) {
			if (sessions != null) {
				return;
			}

			PoolingOptions poolingOpts = new PoolingOptions();

			QueryOptions queryOptions = new QueryOptions();
			queryOptions.setFetchSize(fetchSize);

			SocketOptions socketOptions = new SocketOptions();
			Cluster.Builder cb = Cluster.builder().withLoadBalancingPolicy(new RoundRobinPolicy()).withQueryOptions(queryOptions).withPoolingOptions(poolingOpts)
					.withRetryPolicy(FallthroughRetryPolicy.INSTANCE).withProtocolVersion(ProtocolVersion.NEWEST_SUPPORTED);
			for (String h : hosts) {
				cb.addContactPoint(h);
			}

			cluster = cb.build();
			cluster.getConfiguration().getProtocolOptions().setCompression(ProtocolOptions.Compression.LZ4);

			sessions = new HashMap<String, Session>();
		}
	}

	public static void init(String[] hosts) {
		init(hosts, LoboConstants.DEFAULT_CASS_FETCH_SIZE);
	}

	public static Session getSession(String keyspace) {
		if (sessions.containsKey(keyspace)) {
			return sessions.get(keyspace);
		} else {
			Session session = cluster.newSession();
			sessions.put(keyspace, session);
			return session;
		}
	}

	// This is used to create a standalone new session, which uses different cass hosts
	public static Session getStandaloneSession(String[] hosts) {
		String mapKey = StringUtils.join(hosts, "-");
		if (standaloneSessions.containsKey(mapKey)) {
			return standaloneSessions.get(mapKey);
		}
		
		synchronized (CassSessionFactory.class) {
			Session newSession = getNewSession(hosts, LoboConstants.DEFAULT_CASS_FETCH_SIZE);
			standaloneSessions.put(mapKey, newSession);
			return newSession;
		}

	}

	public static Session getSessionWithSpecificFetchSize(String[] hosts, int fetchSize) {
		return getNewSession(hosts, fetchSize);
	}
	
	private static Session getNewSession(String[] hosts, int fetchSize) {
		PoolingOptions poolingOpts = new PoolingOptions();

		QueryOptions queryOptions = new QueryOptions();
		queryOptions.setFetchSize(fetchSize);

		Cluster.Builder cb = Cluster.builder().withLoadBalancingPolicy(new TokenAwarePolicy(new RoundRobinPolicy())).withQueryOptions(queryOptions).withPoolingOptions(poolingOpts)
				.withProtocolVersion(ProtocolVersion.NEWEST_SUPPORTED);
		for (String h : hosts) {
			cb.addContactPoint(h);
		}

		Cluster newCluster = cb.build();
		newCluster.getConfiguration().getProtocolOptions().setCompression(ProtocolOptions.Compression.LZ4);

		Session newSession = newCluster.newSession();
		return newSession;

	}
}
