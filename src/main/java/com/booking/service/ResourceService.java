package com.booking.service;

import java.util.List;

import com.booking.dto.request.ResourceRequest;
import com.booking.dto.response.ResourceResponse;

public interface ResourceService {

	public ResourceResponse createResource(ResourceRequest request);

	public ResourceResponse getResourceById(Long id);

	public List<ResourceResponse> getAllResources();

	public ResourceResponse updateResource(
	            Long id,
	            ResourceRequest request);
	 public void deleteResource(Long id);


}
