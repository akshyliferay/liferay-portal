/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portlet.trash;

import com.liferay.portal.kernel.dao.orm.FinderCacheUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.transaction.Transactional;
import com.liferay.portal.kernel.trash.TrashHandler;
import com.liferay.portal.kernel.trash.TrashHandlerRegistryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.ClassedModel;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.WorkflowedModel;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.ServiceTestUtil;
import com.liferay.portal.util.TestPropsValues;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.service.AssetEntryLocalServiceUtil;
import com.liferay.portlet.trash.model.TrashEntry;
import com.liferay.portlet.trash.service.TrashEntryLocalServiceUtil;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Brian Wing Shun Chan
 * @author Eudaldo Alonso
 */
public abstract class BaseTrashHandlerTestCase {

	@Before
	public void setUp() {
		FinderCacheUtil.clearCache();
	}

	@Test
	@Transactional
	public void testTrashAndDeleteApproved() throws Exception {
		trashBaseModel(true, true);
	}

	@Test
	@Transactional
	public void testTrashAndDeleteDraft() throws Exception {
		trashBaseModel(false, true);
	}

	@Test
	@Transactional
	public void testTrashAndRestoreApproved() throws Exception {
		trashBaseModel(true, false);
	}

	@Test
	@Transactional
	public void testTrashAndRestoreDraft() throws Exception {
		trashBaseModel(false, false);
	}

	@Test
	@Transactional
	public void testTrashDuplicate() throws Exception {
		trashDuplicateBaseModel();
	}

	@Test
	@Transactional
	public void testTrashParentAndDeleteParent() throws Exception {
		trashParentBaseModel(true);
	}

	@Test
	@Transactional
	public void testTrashParentAndRestoreModel() throws Exception {
		trashParentBaseModel(false);
	}

	@Test
	@Transactional
	public void testTrashVersionAndDelete() throws Exception {
		trashVersionBaseModel(true);
	}

	@Test
	@Transactional
	public void testTrashVersionAndRestore() throws Exception {
		trashVersionBaseModel(false);
	}

	protected abstract BaseModel<?> addBaseModel(
			BaseModel<?> parentBaseModel, boolean approved,
			ServiceContext serviceContext)
		throws Exception;

	protected AssetEntry fetchAssetEntry(Class<?> clazz, long classPK)
		throws Exception {

		return AssetEntryLocalServiceUtil.fetchEntry(clazz.getName(), classPK);
	}

	protected AssetEntry fetchAssetEntry(ClassedModel classedModel)
		throws Exception {

		return fetchAssetEntry(
			classedModel.getModelClass(),
			(Long)classedModel.getPrimaryKeyObj());
	}

	protected Long getAssetClassPK(ClassedModel classedModel) {
		return (Long)classedModel.getPrimaryKeyObj();
	}

	protected abstract BaseModel<?> getBaseModel(long primaryKey)
		throws Exception;

	protected abstract Class<?> getBaseModelClass();

	protected String getBaseModelClassName() {
		Class<?> clazz = getBaseModelClass();

		return clazz.getName();
	}

	protected String getBaseModelName(ClassedModel classedModel) {
		return StringPool.BLANK;
	}

	protected abstract int getBaseModelsNotInTrashCount(
			BaseModel<?> parentBaseModel)
		throws Exception;

	protected BaseModel<?> getParentBaseModel(
			Group group, ServiceContext serviceContext)
		throws Exception {

		return group;
	}

	protected Class<?> getParentBaseModelClass() {
		return getBaseModelClass();
	}

	protected String getParentBaseModelClassName() {
		Class<?> clazz = getParentBaseModelClass();

		return clazz.getName();
	}

	protected abstract String getSearchKeywords();

	protected int getTrashEntriesCount(long groupId) throws Exception {
		return TrashEntryLocalServiceUtil.getEntriesCount(groupId);
	}

	protected long getTrashEntryClassPK(ClassedModel classedModel) {
		return (Long)classedModel.getPrimaryKeyObj();
	}

	protected WorkflowedModel getWorkflowedModel(ClassedModel baseModel)
		throws Exception {

		return (WorkflowedModel)baseModel;
	}

	protected boolean isAssetableModel() {
		return true;
	}

	protected boolean isAssetEntryVisible(ClassedModel classedModel)
		throws Exception {

		AssetEntry assetEntry = AssetEntryLocalServiceUtil.getEntry(
			classedModel.getModelClassName(), getAssetClassPK(classedModel));

		return assetEntry.isVisible();
	}

	protected boolean isBaseModelMoveableFromTrash() {
		return true;
	}

	protected boolean isBaseModelTrashName(ClassedModel classedModel) {
		String baseModelName = getBaseModelName(classedModel);

		return baseModelName.contains(StringPool.SLASH);
	}

	protected boolean isIndexableBaseModel() {
		return true;
	}

	protected boolean isInTrashFolder(ClassedModel classedModel)
		throws Exception {

		return false;
	}

	protected BaseModel<?> moveBaseModelFromTrash(
			ClassedModel classedModel, Group group,
			ServiceContext serviceContext)
		throws Exception {

		return getParentBaseModel(group, serviceContext);
	}

	protected abstract void moveBaseModelToTrash(long primaryKey)
		throws Exception;

	protected void moveParentBaseModelToTrash(long primaryKey)
		throws Exception {
	}

	protected int searchBaseModelsCount(Class<?> clazz, long groupId)
		throws Exception {

		Thread.sleep(1000 * TestPropsValues.JUNIT_DELAY_FACTOR);

		Indexer indexer = IndexerRegistryUtil.getIndexer(clazz);

		SearchContext searchContext = ServiceTestUtil.getSearchContext();

		searchContext.setGroupIds(new long[] {groupId});

		Hits results = indexer.search(searchContext);

		return results.getLength();
	}

	protected int searchTrashEntriesCount(
			String keywords, ServiceContext serviceContext)
		throws Exception {

		Thread.sleep(1000 * TestPropsValues.JUNIT_DELAY_FACTOR);

		Hits results = TrashEntryLocalServiceUtil.search(
			serviceContext.getCompanyId(), serviceContext.getScopeGroupId(),
			serviceContext.getUserId(), keywords, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);

		return results.getLength();
	}

	protected void trashBaseModel(boolean approved, boolean delete)
		throws Exception {

		Group group = ServiceTestUtil.addGroup();

		ServiceContext serviceContext = ServiceTestUtil.getServiceContext();

		serviceContext.setScopeGroupId(group.getGroupId());

		BaseModel<?> parentBaseModel = getParentBaseModel(
			group, serviceContext);

		int initialBaseModelsCount = getBaseModelsNotInTrashCount(
			parentBaseModel);
		int initialBaseModelsSearchCount = 0;
		int initialTrashEntriesCount = getTrashEntriesCount(group.getGroupId());
		int initialTrashEntriesSearchCount = 0;

		if (isIndexableBaseModel()) {
			initialBaseModelsSearchCount = searchBaseModelsCount(
				getBaseModelClass(), group.getGroupId());
			initialTrashEntriesSearchCount = searchTrashEntriesCount(
				getSearchKeywords(), serviceContext);
		}

		BaseModel<?> baseModel = addBaseModel(
			parentBaseModel, approved, serviceContext);

		Assert.assertEquals(
			initialBaseModelsCount + 1,
			getBaseModelsNotInTrashCount(parentBaseModel));

		if (isIndexableBaseModel()) {
			if (approved) {
				Assert.assertEquals(
					initialBaseModelsSearchCount + 1,
					searchBaseModelsCount(
						getBaseModelClass(), group.getGroupId()));
			}
			else {
				Assert.assertEquals(
					initialBaseModelsSearchCount,
					searchBaseModelsCount(
						getBaseModelClass(), group.getGroupId()));
			}

			Assert.assertEquals(
				initialTrashEntriesSearchCount,
				searchTrashEntriesCount(getSearchKeywords(), serviceContext));
		}

		Assert.assertEquals(
			initialTrashEntriesCount, getTrashEntriesCount(group.getGroupId()));

		WorkflowedModel workflowedModel = getWorkflowedModel(baseModel);

		int oldStatus = workflowedModel.getStatus();

		if (isAssetableModel()) {
			Assert.assertEquals(approved, isAssetEntryVisible(baseModel));
		}

		moveBaseModelToTrash((Long)baseModel.getPrimaryKeyObj());

		Assert.assertEquals(
			initialBaseModelsCount,
			getBaseModelsNotInTrashCount(parentBaseModel));
		Assert.assertEquals(
			initialTrashEntriesCount + 1,
			getTrashEntriesCount(group.getGroupId()));

		if (isIndexableBaseModel()) {
			Assert.assertEquals(
				initialBaseModelsSearchCount,
				searchBaseModelsCount(getBaseModelClass(), group.getGroupId()));
			Assert.assertEquals(
				initialTrashEntriesSearchCount + 1,
				searchTrashEntriesCount(getSearchKeywords(), serviceContext));
		}

		TrashEntry trashEntry = TrashEntryLocalServiceUtil.getEntry(
			getBaseModelClassName(), getTrashEntryClassPK(baseModel));

		Assert.assertEquals(
			getTrashEntryClassPK(baseModel),
			GetterUtil.getLong(trashEntry.getClassPK()));

		workflowedModel = getWorkflowedModel(
			getBaseModel((Long)baseModel.getPrimaryKeyObj()));

		Assert.assertEquals(
			WorkflowConstants.STATUS_IN_TRASH, workflowedModel.getStatus());

		if (isAssetableModel()) {
			Assert.assertFalse(isAssetEntryVisible(baseModel));
		}

		TrashHandler trashHandler = TrashHandlerRegistryUtil.getTrashHandler(
			getBaseModelClassName());

		if (delete) {
			trashHandler.deleteTrashEntry(getTrashEntryClassPK(baseModel));

			Assert.assertEquals(
				initialBaseModelsCount,
				getBaseModelsNotInTrashCount(parentBaseModel));

			if (isIndexableBaseModel()) {
				Assert.assertEquals(
					initialBaseModelsSearchCount,
					searchBaseModelsCount(
						getBaseModelClass(), group.getGroupId()));
				Assert.assertEquals(
					initialTrashEntriesSearchCount,
					searchTrashEntriesCount(
						getSearchKeywords(), serviceContext));
			}

			Assert.assertNull(fetchAssetEntry(baseModel));
		}
		else {
			trashHandler.restoreTrashEntry(getTrashEntryClassPK(baseModel));

			Assert.assertEquals(
				initialBaseModelsCount + 1,
				getBaseModelsNotInTrashCount(parentBaseModel));

			if (isIndexableBaseModel()) {
				if (approved) {
					Assert.assertEquals(
						initialBaseModelsSearchCount + 1,
						searchBaseModelsCount(
							getBaseModelClass(), group.getGroupId()));
				}
				else {
					Assert.assertEquals(
						initialBaseModelsSearchCount,
						searchBaseModelsCount(
							getBaseModelClass(), group.getGroupId()));
				}

				Assert.assertEquals(
					initialTrashEntriesSearchCount,
					searchTrashEntriesCount(
						getSearchKeywords(), serviceContext));
			}

			baseModel = getBaseModel((Long)baseModel.getPrimaryKeyObj());

			workflowedModel = getWorkflowedModel(baseModel);

			Assert.assertEquals(oldStatus, workflowedModel.getStatus());

			if (isAssetableModel()) {
				Assert.assertEquals(approved, isAssetEntryVisible(baseModel));
			}
		}
	}

	protected void trashDuplicateBaseModel() throws Exception {
		Group group = ServiceTestUtil.addGroup();

		ServiceContext serviceContext = ServiceTestUtil.getServiceContext();

		serviceContext.setScopeGroupId(group.getGroupId());

		BaseModel<?> parentBaseModel = getParentBaseModel(
			group, serviceContext);

		int initialBaseModelsCount = getBaseModelsNotInTrashCount(
			parentBaseModel);
		int initialTrashEntriesCount = getTrashEntriesCount(group.getGroupId());

		BaseModel<?> baseModel = addBaseModel(
			parentBaseModel, true, serviceContext);

		moveBaseModelToTrash((Long)baseModel.getPrimaryKeyObj());

		baseModel = getBaseModel((Long)baseModel.getPrimaryKeyObj());

		Assert.assertEquals(
			initialBaseModelsCount,
			getBaseModelsNotInTrashCount(parentBaseModel));
		Assert.assertEquals(
			initialTrashEntriesCount + 1,
			getTrashEntriesCount(group.getGroupId()));

		Assert.assertTrue(isBaseModelTrashName(baseModel));

		BaseModel<?> duplicateBaseModel = addBaseModel(
			parentBaseModel, true, serviceContext);

		moveBaseModelToTrash((Long)duplicateBaseModel.getPrimaryKeyObj());

		duplicateBaseModel = getBaseModel(
			(Long)duplicateBaseModel.getPrimaryKeyObj());

		Assert.assertEquals(
			initialBaseModelsCount,
			getBaseModelsNotInTrashCount(parentBaseModel));
		Assert.assertEquals(
			initialTrashEntriesCount + 2,
			getTrashEntriesCount(group.getGroupId()));

		Assert.assertTrue(isBaseModelTrashName(duplicateBaseModel));
	}

	protected void trashParentBaseModel(boolean delete) throws Exception {
		Group group = ServiceTestUtil.addGroup();

		ServiceContext serviceContext = ServiceTestUtil.getServiceContext();

		serviceContext.setScopeGroupId(group.getGroupId());

		BaseModel<?> parentBaseModel = getParentBaseModel(
			group, serviceContext);

		int initialBaseModelsCount = getBaseModelsNotInTrashCount(
			parentBaseModel);
		int initialTrashEntriesCount = getTrashEntriesCount(group.getGroupId());

		BaseModel<?> baseModel = addBaseModel(
			parentBaseModel, true, serviceContext);

		Assert.assertEquals(
			initialBaseModelsCount + 1,
			getBaseModelsNotInTrashCount(parentBaseModel));
		Assert.assertEquals(
			initialTrashEntriesCount, getTrashEntriesCount(group.getGroupId()));

		moveBaseModelToTrash((Long)baseModel.getPrimaryKeyObj());

		Assert.assertEquals(
			initialBaseModelsCount,
			getBaseModelsNotInTrashCount(parentBaseModel));
		Assert.assertEquals(
			initialTrashEntriesCount + 1,
			getTrashEntriesCount(group.getGroupId()));

		Assert.assertFalse(isInTrashFolder(baseModel));

		moveParentBaseModelToTrash((Long)parentBaseModel.getPrimaryKeyObj());

		Assert.assertEquals(
			initialTrashEntriesCount + 2,
			getTrashEntriesCount(group.getGroupId()));

		Assert.assertTrue(isInTrashFolder(baseModel));

		if (isBaseModelMoveableFromTrash()) {
			if (delete) {
				TrashHandler trashHandler =
					TrashHandlerRegistryUtil.getTrashHandler(
						getParentBaseModelClassName());

				trashHandler.deleteTrashEntry(
					(Long)parentBaseModel.getPrimaryKeyObj());

				Assert.assertEquals(
					initialBaseModelsCount,
					getBaseModelsNotInTrashCount(parentBaseModel));
				Assert.assertEquals(
					initialTrashEntriesCount + 1,
					getTrashEntriesCount(group.getGroupId()));
			}
			else {
				BaseModel<?> newParentBaseModel = moveBaseModelFromTrash(
					baseModel, group, serviceContext);

				Assert.assertEquals(
					initialBaseModelsCount + 1,
					getBaseModelsNotInTrashCount(newParentBaseModel));
				Assert.assertEquals(
					initialTrashEntriesCount + 1,
					getTrashEntriesCount(group.getGroupId()));
			}
		}
	}

	protected void trashVersionBaseModel(boolean delete) throws Exception {
		Group group = ServiceTestUtil.addGroup();

		ServiceContext serviceContext = ServiceTestUtil.getServiceContext();

		serviceContext.setScopeGroupId(group.getGroupId());

		BaseModel<?> parentBaseModel = getParentBaseModel(
			group, serviceContext);

		int initialBaseModelsCount = getBaseModelsNotInTrashCount(
			parentBaseModel);
		int initialBaseModelsSearchCount = 0;
		int initialTrashEntriesCount = getTrashEntriesCount(group.getGroupId());
		int initialTrashEntriesSearchCount = 0;

		if (isIndexableBaseModel()) {
			initialBaseModelsSearchCount = searchBaseModelsCount(
				getBaseModelClass(), group.getGroupId());
			initialTrashEntriesSearchCount = searchTrashEntriesCount(
				getSearchKeywords(), serviceContext);
		}

		BaseModel<?> baseModel = addBaseModel(
			parentBaseModel, true, serviceContext);

		baseModel = updateBaseModel(
			(Long)baseModel.getPrimaryKeyObj(), serviceContext);

		Assert.assertEquals(
			initialBaseModelsCount + 1,
			getBaseModelsNotInTrashCount(parentBaseModel));
		Assert.assertEquals(
			initialTrashEntriesCount, getTrashEntriesCount(group.getGroupId()));

		if (isIndexableBaseModel()) {
			Assert.assertEquals(
				initialBaseModelsSearchCount + 1,
				searchBaseModelsCount(getBaseModelClass(), group.getGroupId()));
			Assert.assertEquals(
				initialTrashEntriesSearchCount,
				searchTrashEntriesCount(getSearchKeywords(), serviceContext));
		}

		if (isAssetableModel()) {
			Assert.assertTrue(isAssetEntryVisible(baseModel));
		}

		moveBaseModelToTrash((Long)baseModel.getPrimaryKeyObj());

		Assert.assertEquals(
			initialBaseModelsCount,
			getBaseModelsNotInTrashCount(parentBaseModel));
		Assert.assertEquals(
			initialTrashEntriesCount + 1,
			getTrashEntriesCount(group.getGroupId()));

		if (isIndexableBaseModel()) {
			Assert.assertEquals(
				initialBaseModelsSearchCount,
				searchBaseModelsCount(getBaseModelClass(), group.getGroupId()));
			Assert.assertEquals(
				initialTrashEntriesSearchCount + 1,
				searchTrashEntriesCount(getSearchKeywords(), serviceContext));
		}

		if (isAssetableModel()) {
			Assert.assertFalse(isAssetEntryVisible(baseModel));
		}

		TrashHandler trashHandler = TrashHandlerRegistryUtil.getTrashHandler(
			getBaseModelClassName());

		if (delete) {
			trashHandler.deleteTrashEntry(getTrashEntryClassPK(baseModel));

			Assert.assertEquals(
				initialBaseModelsCount,
				getBaseModelsNotInTrashCount(parentBaseModel));
			Assert.assertEquals(
				initialTrashEntriesCount,
				getTrashEntriesCount(group.getGroupId()));

			if (isIndexableBaseModel()) {
				Assert.assertEquals(
					initialBaseModelsSearchCount,
					searchBaseModelsCount(
						getBaseModelClass(), group.getGroupId()));
				Assert.assertEquals(
					initialTrashEntriesSearchCount,
					searchTrashEntriesCount(
						getSearchKeywords(), serviceContext));
			}

			Assert.assertNull(fetchAssetEntry(baseModel));
		}
		else {
			trashHandler.restoreTrashEntry(getTrashEntryClassPK(baseModel));

			Assert.assertEquals(
				initialBaseModelsCount + 1,
				getBaseModelsNotInTrashCount(parentBaseModel));
			Assert.assertEquals(
				initialTrashEntriesCount,
				getTrashEntriesCount(group.getGroupId()));

			if (isIndexableBaseModel()) {
				Assert.assertEquals(
					initialBaseModelsSearchCount + 1,
					searchBaseModelsCount(
						getBaseModelClass(), group.getGroupId()));
				Assert.assertEquals(
					initialTrashEntriesSearchCount,
					searchTrashEntriesCount(
						getSearchKeywords(), serviceContext));
			}

			if (isAssetableModel()) {
				Assert.assertTrue(isAssetEntryVisible(baseModel));
			}
		}
	}

	protected BaseModel<?> updateBaseModel(
			long primaryKey, ServiceContext serviceContext)
		throws Exception {

		return getBaseModel(primaryKey);
	}

}