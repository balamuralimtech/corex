/*
 * Copyright (c) 2026 company.name. All rights reserved.
 *
 * This software and its associated documentation are proprietary to company.name.
 * Unauthorized copying, distribution, modification, or use of this software,
 * via any medium, is strictly prohibited without prior written permission.
 *
 * This software is provided "as is", without warranty of any kind, express or implied,
 * including but not limited to the warranties of merchantability, fitness for a
 * particular purpose, and noninfringement. In no event shall the authors or copyright
 * holders be liable for any claim, damages, or other liability arising from the use
 * of this software.
 *
 * Author: Balamurali
 * Project: app.name
 */
package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.CoreDashboardTO;
import com.module.coretix.commonto.RoleUsageCountTO;
import com.module.coretix.commonto.UserActivitiesCountTO;
import com.module.coretix.commonto.UsersStatusCountTO;
import com.module.coretix.coretix.ICoreDashboardService;
import com.persist.coretix.modal.coretix.dao.ICoreDashboardDAO;
import com.persist.coretix.modal.usermanagement.dao.IRoleAdministrationDAO;
import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;
import com.persist.coretix.modal.usermanagement.dao.IUserAdministrationDAO;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Transactional(readOnly = true)
public class CoreDashboardService implements ICoreDashboardService {

    @Inject
    private ICoreDashboardDAO coreDashboardDAO;

    @Inject
    private IUserAdministrationDAO userAdministrationDAO;

    @Inject
    private IRoleAdministrationDAO roleAdministrationDAO;

    @Inject
    private IUserActivityDAO userActivityDAO;

    @Transactional(readOnly = false)
    public CoreDashboardTO fetchDashboardData() {
        CoreDashboardTO coreDashboardTO = new CoreDashboardTO();

        coreDashboardTO.setOrganizationCount(coreDashboardDAO.fetchOrganizationCount());
        coreDashboardTO.setBranchCount(coreDashboardDAO.fetchBranchCount());
        coreDashboardTO.setCountryCount(coreDashboardDAO.fetchCountryCount());
        coreDashboardTO.setStateCount(coreDashboardDAO.fetchStateCount());
        coreDashboardTO.setCityCount(coreDashboardDAO.fetchCityCount());
        coreDashboardTO.setCurrencyCount(coreDashboardDAO.fetchCurrencyCount());
        coreDashboardTO.setDepartmentCount(coreDashboardDAO.fetchDepartmentCount());
        coreDashboardTO.setDesignationCount(coreDashboardDAO.fetchDesignationCount());
        coreDashboardTO.setRoleCount(coreDashboardDAO.fetchRoleCount());
        coreDashboardTO.setUserCount(coreDashboardDAO.fetchUserCount());
        coreDashboardTO.setUserActivityCount(coreDashboardDAO.fetchUserActivityCount());

        coreDashboardTO.setLoginCount(getUserActivityDAO().getActivityTypeCounts().get("login"));
        coreDashboardTO.setLogoutCount(getUserActivityDAO().getActivityTypeCounts().get("logout"));
        coreDashboardTO.setAddCount(getUserActivityDAO().getActivityTypeCounts().get("add"));
        coreDashboardTO.setUpdateCount(getUserActivityDAO().getActivityTypeCounts().get("update"));
        coreDashboardTO.setDeleteCount(getUserActivityDAO().getActivityTypeCounts().get("delete"));

        coreDashboardTO.setUsersLoggedInCount(getUserAdministrationDAO().getCountOfUsersLoggedIn());
        coreDashboardTO.setUsersLoggedOutCount(getUserAdministrationDAO().getCountOfUsersLoggedOut());
        coreDashboardTO.setUsersNeverLoggedinCount(getUserAdministrationDAO().getCountOfUsersNeverLoggedIn());

        coreDashboardTO.setRolesNotUsedCount(getRoleAdministrationDAO().getCountOfRolesUsedAndNotUsed().get("notUsedRoles"));
        coreDashboardTO.setRolesUsedCount(getRoleAdministrationDAO().getCountOfRolesUsedAndNotUsed().get("usedRoles"));

        return coreDashboardTO;

    }

    public ICoreDashboardDAO getCoreDashboardDAO() {
        return coreDashboardDAO;
    }

    public void setCoreDashboardDAO(ICoreDashboardDAO coreDashboardDAO) {
        this.coreDashboardDAO = coreDashboardDAO;
    }

    public IUserAdministrationDAO getUserAdministrationDAO() {
        return userAdministrationDAO;
    }

    public void setUserAdministrationDAO(IUserAdministrationDAO userAdministrationDAO) {
        this.userAdministrationDAO = userAdministrationDAO;
    }

    public IRoleAdministrationDAO getRoleAdministrationDAO() {
        return roleAdministrationDAO;
    }

    public void setRoleAdministrationDAO(IRoleAdministrationDAO roleAdministrationDAO) {
        this.roleAdministrationDAO = roleAdministrationDAO;
    }

    public IUserActivityDAO getUserActivityDAO() {
        return userActivityDAO;
    }


}




