package com.gentics.mesh.search.verticle;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;

/**
 * Verticle which will execute the elasticsearch sync. 
 */
public class ElasticsearchSyncVerticle extends AbstractVerticle {

	private static final Logger log = LoggerFactory.getLogger(ElasticsearchSyncVerticle.class);

	@Override
	public void start() throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Starting verticle {" + getClass().getName() + "}");
		}
	}

}
