package com.chestnut.search.core;

import java.io.IOException;
import java.util.List;

import com.chestnut.search.domain.dto.SearchModelDTO;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;

public interface ISearchType {
	
	/**
	 * 检索类型
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * 添加索引
	 * 
	 * @param indexName
	 * @throws IOException 
	 * @throws ElasticsearchException 
	 */
	public boolean addIndex(SearchModelDTO si) throws ElasticsearchException, IOException;

	/**
	 * 删除索引
	 * 
	 * @param indexName
	 * @return 
	 * @throws IOException 
	 * @throws ElasticsearchException 
	 */
	public boolean deleteIndex(String indexName) throws ElasticsearchException, IOException;
	
	/**
	 * 添加索引文档
	 * @throws IOException 
	 * @throws ElasticsearchException 
	 */
	public void addDocument(String indexName, List<BaseDocument> docs) throws ElasticsearchException, IOException;
	
	/**
	 * 更新索引文档
	 */
	public void updateDocument(String indexName, List<BaseDocument> docs) throws ElasticsearchException, IOException;
	
	/**
	 * 删除索引文档
	 */
	public void deleteDocument(String indexName, List<String> documentIds) throws ElasticsearchException, IOException;
}
