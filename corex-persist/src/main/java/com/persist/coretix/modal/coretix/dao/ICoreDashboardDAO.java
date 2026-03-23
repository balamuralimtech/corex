package com.persist.coretix.modal.coretix.dao;

public interface ICoreDashboardDAO {

    public long fetchOrganizationCount();

    public long fetchBranchCount();

    public long fetchDepartmentCount();

    public long fetchDesignationCount();

    public long fetchCountryCount();

    public long fetchStateCount();

    public long fetchCityCount();

    public long fetchCurrencyCount();

    public long fetchRoleCount();

    public long fetchUserCount();

    public long fetchUserActivityCount();

}

