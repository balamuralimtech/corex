package com.module.coretix.coretix;

import com.module.coretix.commonto.CoreDashboardTO;
import com.module.coretix.commonto.RoleUsageCountTO;
import com.module.coretix.commonto.UserActivitiesCountTO;
import com.module.coretix.commonto.UsersStatusCountTO;

public interface ICoreDashboardService {
    public CoreDashboardTO fetchDashboardData();

}
