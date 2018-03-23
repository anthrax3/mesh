package com.gentics.mesh.core.rest.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.gentics.mesh.core.rest.common.RestModel;

/**
 * Rest model POJO for a search status response.
 */
public class SearchStatusResponse implements RestModel {

	@JsonProperty(required = false)
	@JsonPropertyDescription("Flag which indicates whether a index synchronization is currently running.")
	boolean indexSyncRunning = false;

	public SearchStatusResponse() {
	}

	public boolean isIndexSyncRunning() {
		return indexSyncRunning;
	}

	public void setIndexSyncRunning(boolean indexSyncRunning) {
		this.indexSyncRunning = indexSyncRunning;
	}

}
