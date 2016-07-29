curl --data '{"spu_id":1, "page":1, "page_size":10}' "http://172.16.31.63:16010/lobo/comment"


Create KEYSPACE lobo WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

create table lobo.spusku (
    spu_id bigint,
    sku_id bigint,
    primary key(spu_id)
)WITH compression={'chunk_length_kb': '64', 'sstable_compression': 'LZ4Compressor'} AND caching = '{"keys":"NONE", "rows_per_partition":"ALL"}'
 AND speculative_retry = '5ms';


create table lobo.comment(
       id bigint,
       sku_id bigint,
       spu_id bigint,
       user_id bigint,
       create_time bigint,
       comment text,
       replies list<text>,
       pic_path list<text>,
       score int,
       lucene text,
       primary key(id)
) WITH compression = { 'sstable_compression' : 'LZ4Compressor', 'chunk_length_kb' : 64 }
     AND compaction={'class':'LeveledCompactionStrategy'}
     AND caching='{"keys":"NONE", "rows_per_partition":"ALL"}';

create table lobo.user{
       user_id bigint,
       user_loc_acct text,
       user_name text,
       register_time bigint,
       type int,
       level int,
       province_code int,
       city_code int,
       country_code int,

       primary key(user_id)
}

CREATE CUSTOM INDEX comment_index ON lobo.comment (lucene)
    USING 'com.stratio.cassandra.lucene.Index'
    WITH OPTIONS = {
    'refresh_seconds' : '10',
    'ram_buffer_mb'        : '64',
    'max_merge_mb'         : '32',
    'max_cached_mb'        : '512',
    'schema' : '{
    fields : {
        sku_id: {type:"bigint"},
	spu_id: {type:"bigint"},
	create_time: {type:"bigint"},
	score: {type:"integer"}
  }}'
};


select * from lobo.comment where lucene = '{query : {type:"match", field:"spu_id", value:1291931584}}' order by create_time limit 10;




curl --data '{"spu_id":1291931584, "page":1, "page_size":10}' "http://172.16.31.63:16010/lobo/comment"

curl --data '{"spu_id":1291931584, "page":1, "page_size":10, "day_time":1469650852658}' "http://172.16.31.63:16010/lobo/day/comment"
curl --data '{"spu_id":1291931584, "page":1, "page_size":10, "day_time":1469650852658}' "http://172.16.31.63:16010/lobo/count"