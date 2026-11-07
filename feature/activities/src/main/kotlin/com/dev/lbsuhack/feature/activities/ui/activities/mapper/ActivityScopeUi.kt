package com.dev.lbsuhack.feature.activities.ui.activities.mapper

import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.domain.ActivityScope

internal fun ActivityScope.labelRes(): Int = when (this) {
    ActivityScope.CLASS -> R.string.create_activity_scope_class
    ActivityScope.GRADE -> R.string.create_activity_scope_grade
    ActivityScope.SCHOOL -> R.string.create_activity_scope_school
}
