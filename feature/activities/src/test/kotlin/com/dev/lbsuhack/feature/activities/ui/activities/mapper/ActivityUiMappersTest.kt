package com.dev.lbsuhack.feature.activities.ui.activities.mapper

import com.dev.lbsuhack.core.domain.DomainError
import com.dev.lbsuhack.core.localization.text.UiText
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityCategory
import com.dev.lbsuhack.feature.activities.domain.ActivityScope
import com.dev.lbsuhack.feature.activities.domain.ActivityStatus
import com.dev.lbsuhack.feature.activities.domain.ActivityOperation
import com.dev.lbsuhack.feature.activities.store.activities.ActivityTab
import com.dev.lbsuhack.feature.activities.store.toUiText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityUiMappersTest {

    @Test
    fun categoryUiMappersCoverAllCategories() {
        for (category in ActivityCategory.entries) {
            assertNotNull(category.icon())
            assertTrue(category.labelRes() != 0)
        }
    }

    @Test
    fun statusLabelResCoversAllStatuses() {
        for (status in ActivityStatus.entries) {
            assertTrue(status.labelRes() != 0)
        }
    }

    @Test
    fun tabLabelResCoversAllTabs() {
        for (tab in ActivityTab.entries) {
            assertTrue(tab.labelRes() != 0)
        }
    }

    @Test
    fun scopeLabelResCoversAllScopes() {
        for (scope in ActivityScope.entries) {
            assertTrue(scope.labelRes() != 0)
        }
    }

    @Test
    fun domainErrorToUiTextMapsAllOperationsForForbidden() {
        assertEquals(
            UiText.Resource(R.string.activity_error_forbidden_create),
            DomainError.Forbidden.toUiText(ActivityOperation.CREATE),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_forbidden_vote),
            DomainError.Forbidden.toUiText(ActivityOperation.VOTE),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_forbidden_like),
            DomainError.Forbidden.toUiText(ActivityOperation.LIKE),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_forbidden_details),
            DomainError.Forbidden.toUiText(ActivityOperation.DETAILS),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_forbidden_list),
            DomainError.Forbidden.toUiText(ActivityOperation.LIST),
        )
    }

    @Test
    fun domainErrorToUiTextMapsAllErrorTypes() {
        assertEquals(
            UiText.Resource(R.string.activity_error_unauthorized),
            DomainError.Unauthorized.toUiText(ActivityOperation.LIST),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_network),
            DomainError.Network.toUiText(ActivityOperation.LIST),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_network),
            DomainError.Timeout.toUiText(ActivityOperation.LIST),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_not_found),
            DomainError.NotFound.toUiText(ActivityOperation.LIST),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_validation),
            DomainError.Validation.toUiText(ActivityOperation.LIST),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_unknown),
            DomainError.Unknown.toUiText(ActivityOperation.LIST),
        )
        assertEquals(
            UiText.Resource(R.string.activity_error_unknown),
            DomainError.Exception(RuntimeException()).toUiText(ActivityOperation.LIST),
        )
        assertEquals(
            UiText.Raw("Custom message"),
            DomainError.BusinessRule(message = "Custom message").toUiText(ActivityOperation.LIST),
        )
    }
}
