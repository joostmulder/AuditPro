/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import com.auditpro.mobile_client.entities.ReorderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Interface required to share the current filter status.
 * @author Eric Ruck
 */
public interface FilterStatusProvider {

	/**
	 * Gets the current filter on reorder status.
	 * @return Current filter on reorder status
	 */
	ReorderStatus[] getFilterReorderStatus();

	/**
	 * Gets the current filter on product types.
	 * @return Current filter on product types
	 */
	ArrayList<String> getFilterProductTypes();

	/**
	 * Gets all of the possible product types.
	 * @return All possible product types
	 */
	List<String> getAllProductTypes();

	/**
	 * Sets the current filter.
	 * @param reorderStatuses New filter on reorder status
	 * @param productTypes New filter on product types
	 */
	void setFilterStatus(ReorderStatus[] reorderStatuses, ArrayList<String> productTypes);

	/**
	 * Gets the current filter in product brands.
	 * @return Current filter on product brands
	 */
	Set<String> getFilterBrands();

	/**
	 * Gets all of the available product brands.
	 * @return All product brands
	 */
	Set<String> getAllBrands();

	/**
	 * Sets the current brands filter.
	 * @param value New list of products in filter
	 */
	void setFilterBrands(Set<String> value);
}
