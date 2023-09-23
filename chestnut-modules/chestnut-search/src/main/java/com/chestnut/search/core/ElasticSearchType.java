package com.chestnut.search.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.chestnut.search.domain.dto.SearchModelDTO;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.mapping.FieldType;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ElasticSearchType implements ISearchType {

	public static final String TYPE = "ElasticSearch";

	private final ElasticsearchClient client;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean addIndex(SearchModelDTO si) throws ElasticsearchException, IOException {
		BooleanResponse b = client.indices().exists(e -> e.index(si.getName()));
		if (!b.value()) {
			Map<String, Property> properties = new HashMap<>();

			si.getFields().forEach(f -> {
				if (FieldType.Long.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.long_(lp -> lp.boost(f.getWeight()))));
				} else if (FieldType.LongRange.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.longRange(lrp -> lrp.boost(f.getWeight()))));
				} else if (FieldType.Integer.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.integer(ip -> ip.boost(f.getWeight()))));
				} else if (FieldType.IntegerRange.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.integerRange(ip -> ip.boost(f.getWeight()))));
				} else if (FieldType.Byte.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.byte_(bp -> bp.boost(f.getWeight()))));
				} else if (FieldType.Short.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.short_(sp -> sp.boost(f.getWeight()))));
				} else if (FieldType.Double.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.double_(dp -> dp.boost(f.getWeight()))));
				} else if (FieldType.DoubleRange.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(pb -> pb.doubleRange(drp -> drp.boost(f.getWeight()))));
				} else if (FieldType.Date.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property
							.of(pb -> pb.date(datep -> datep.format("yyyy-MM-dd HH:mm:ss").boost(f.getWeight()))));
				} else if (FieldType.Text.name().equalsIgnoreCase(f.getType())) {
					properties.put(f.getName(), Property.of(
							pb -> pb.text(tp -> tp.index(f.isIndex()).analyzer(f.getAnalyzer()).boost(f.getWeight()))));
				} else {
					properties.put(f.getName(), Property.of(pb -> pb.keyword(kp -> kp.boost(f.getWeight()))));
				}
			});
			CreateIndexResponse res = client.indices()
					.create(c -> c.index(si.getName()).mappings(mapping -> mapping.properties(properties)));
			return res.acknowledged();
		}
		return false;
	}

	@Override
	public boolean deleteIndex(String indexName) throws ElasticsearchException, IOException {
		DeleteIndexResponse delete = client.indices().delete(d -> d.index(indexName));
		return delete.acknowledged();
	}

	@Override
	public void addDocument(String indexName, List<BaseDocument> docs) throws ElasticsearchException, IOException {
		this.client.bulk(br -> br.operations(op -> {
			docs.forEach(doc -> op.create(c -> c.index(indexName).id(doc.getDocId()).document(doc)));
			return op;
		}));
	}

	@Override
	public void updateDocument(String indexName, List<BaseDocument> docs) throws ElasticsearchException, IOException {
		this.client.bulk(br -> br.operations(op -> {
			docs.forEach(doc -> op.update(up -> up.index(indexName).id(doc.getDocId())));
			return op;
		}));
	}

	@Override
	public void deleteDocument(String indexName, List<String> documentIds) throws ElasticsearchException, IOException {
		this.client.bulk(br -> br.operations(op -> {
			documentIds.forEach(docId -> op.delete(d -> d.index(indexName).id(docId)));
			return op;
		}));
	}
}
