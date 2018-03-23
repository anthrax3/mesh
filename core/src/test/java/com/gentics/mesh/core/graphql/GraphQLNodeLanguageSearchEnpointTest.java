package com.gentics.mesh.core.graphql;


import com.gentics.mesh.FieldUtil;
import com.gentics.mesh.core.rest.node.NodeCreateRequest;
import com.gentics.mesh.core.rest.schema.impl.SchemaReferenceImpl;
import com.gentics.mesh.core.rest.user.NodeReference;
import com.gentics.mesh.parameter.impl.NodeParametersImpl;
import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.MeshTestSetting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;

@MeshTestSetting(useElasticsearch = true, testSize = TestSize.FULL, startServer = true)
@RunWith(Parameterized.class)
public class GraphQLNodeLanguageSearchEnpointTest extends AbstractGraphQLSearchEndpointTest {
	@Parameterized.Parameters(name = "query={0}")
	public static List<String> paramData() {
		return Arrays.asList("node-elasticsearch-language-query");
	}

	public GraphQLNodeLanguageSearchEnpointTest(String queryName) {
		super(queryName);
	}

	@Before
	public void createNodes() {
		String parentNodeUuid = tx(() -> folder("2015").getUuid());
		Arrays.asList("de", "en").forEach(lang -> {
			NodeCreateRequest nodeCreateRequest = new NodeCreateRequest();
			nodeCreateRequest.setParentNode(new NodeReference().setUuid(parentNodeUuid));
			nodeCreateRequest.setSchema(new SchemaReferenceImpl().setName("content"));
			nodeCreateRequest.setLanguage(lang);
			nodeCreateRequest.getFields().put("slug", FieldUtil.createStringField("test" + lang));
			nodeCreateRequest.getFields().put("teaser", FieldUtil.createStringField("some teaser"));
			nodeCreateRequest.getFields().put("content", FieldUtil.createStringField("Blessed mealtime again!"));
			call(() -> client().createNode(PROJECT_NAME, nodeCreateRequest, new NodeParametersImpl().setLanguages(lang)));
		});
	}

	@Test
	public void testNodeQuery() throws Exception {
		super.testNodeQuery();
	}
}
