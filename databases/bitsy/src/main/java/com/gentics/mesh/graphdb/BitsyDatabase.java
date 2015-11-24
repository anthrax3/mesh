package com.gentics.mesh.graphdb;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;

import com.gentics.mesh.etc.GraphStorageOptions;
import com.gentics.mesh.graphdb.model.MeshElement;
import com.gentics.mesh.graphdb.spi.AbstractDatabase;
import com.gentics.mesh.graphdb.spi.Database;
import com.gentics.mesh.graphdb.spi.TrxHandler;
import com.lambdazen.bitsy.BitsyGraph;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * OrientDB specific mesh graph database implementation.
 */
public class BitsyDatabase extends AbstractDatabase {

	private static final Logger log = LoggerFactory.getLogger(BitsyDatabase.class);

	private int maxRetry = 25;

	@Override
	public void stop() {
		Database.setThreadLocalGraph(null);
	}

	@Override
	public void init(GraphStorageOptions options, Vertx vertx) throws Exception {
		super.init(options, vertx);
		if (options != null && options.getParameters() != null && options.getParameters().get("maxTransactionRetry") != null) {
			this.maxRetry = options.getParameters().get("maxTransactionRetry").getAsInt();
			log.info("Using {" + this.maxRetry + "} transaction retries before failing");
		}
	}

	@Override
	public void clear() {

	}

	@Override
	public void start() throws Exception {
		BitsyGraph myGraph = new BitsyGraph(null);
		throw new NotImplementedException();

	}

	@Override
	public void addEdgeIndex(String label, String... extraFields) {

	}

	@Override
	public Iterator<Vertex> getVertices(Class<?> classOfVertex, String[] fieldNames, Object[] fieldValues) {
		throw new NotImplementedException();
	}

	@Override
	public void addEdgeType(String label, String... stringPropertyKeys) {

	}

	@Override
	public void addVertexType(Class<?> clazzOfVertex) {

	}

	@Override
	public void addEdgeIndexSource(String label) {

	}

	@Override
	public void addVertexIndex(Class<?> clazzOfVertices, String... fields) {

	}

	@Override
	public void reload(MeshElement element) {
	}

	@Override
	public Trx trx() {
		throw new NotImplementedException();
	}

	@Override
	public <T> Database trx(TrxHandler<Future<T>> txHandler, Handler<AsyncResult<T>> resultHandler) {
		throw new NotImplementedException();
	}

	@Override
	public NoTrx noTrx() {
		throw new NotImplementedException();
	}

	@Override
	public void backupGraph(String backupDirectory) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public void restoreGraph(String backupFile) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public void exportGraph(String outputDirectory) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public void importGraph(String importFile) throws IOException {
		throw new NotImplementedException();
	}

	@Override
	public Object createComposedIndexKey(Object... keys) {
		throw new NotImplementedException();
	}

	@Override
	public void setVertexType(Element element, Class<?> classOfVertex) {
		throw new NotImplementedException();
	}

}