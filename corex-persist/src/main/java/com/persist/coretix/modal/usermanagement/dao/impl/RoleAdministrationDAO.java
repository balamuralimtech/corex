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
package com.persist.coretix.modal.usermanagement.dao.impl;

import com.persist.coretix.modal.usermanagement.RolePrivileges;
import com.persist.coretix.modal.usermanagement.Roles;
import com.persist.coretix.modal.usermanagement.dao.IRoleAdministrationDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author balamurali
 */
@Named
public class RoleAdministrationDAO implements IRoleAdministrationDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleAdministrationDAO.class);
     
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void addRole (Roles role) {
        logger.debug("inside dao save organization !!");
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();
        session.save(role);
        trans.commit();
    }

    public void updateRole(Roles role) {
        logger.debug("inside dao updateOrganization !!");
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();
        session.merge(role);
        trans.commit();
    }

    public void deleteRole(Roles role) {
        logger.debug("inside dao deleteOrganization !!");
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();
        Roles persistentRole = (Roles) session.get(Roles.class, role.getId());
        session.delete(persistentRole);
        trans.commit();
    }

    public Roles getRole(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from Roles where id=?1").setParameter(1, id)
                .list();

        trans.commit();
        return (Roles) list.get(0);
    }

    public List<RolePrivileges> getRolePrivilegesByModuleAndSubModule(int roleId, int moduleId, int subModuleId) {

        logger.debug("inside dao getRolePrivilegesByModuleAndSubModule !!");
        logger.debug("{}", roleId + ":" + moduleId + ":" + subModuleId);
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<RolePrivileges> list = session
                .createQuery("from RolePrivileges where roles.id = ?1 and moduleId = ?2 and submoduleId = ?3 and isSelected = true")
                .setParameter(1, roleId)
                .setParameter(2, moduleId)
                .setParameter(3, subModuleId)
                .list();

                trans.commit();
                return list;
    }

    public List<Integer> getModulesByRoleId(int roleId) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction transaction = null;
        List<Integer> distinctModules = null;

        try {
            transaction = session.beginTransaction();

            // HQL Query: Select distinct module IDs by role ID and order them
            // Correct field name `moduleId` (from entity, not column name)
            distinctModules = session
                    .createQuery("SELECT DISTINCT rp.moduleId FROM RolePrivileges rp WHERE rp.roles.id = :roleId and rp.privilegeId = 1 and rp.isSelected = 1 ORDER BY rp.moduleId")
                    .setParameter("roleId", roleId)
                    .list();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return distinctModules;
    }

    public List<Integer> getSubmodulesByRoleandModuleId(int roleId, int moduleId) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction transaction = null;
        List<Integer> distinctSubModules = null;

        try {
            transaction = session.beginTransaction();

            // HQL Query: Select distinct module IDs by role ID and order them
            // Correct field name `moduleId` (from entity, not column name)
            distinctSubModules = session
                    .createQuery("SELECT DISTINCT rp.submoduleId FROM RolePrivileges rp WHERE rp.roles.id = :roleId and rp.moduleId = :moduleId and rp.privilegeId = 1 and rp.isSelected = 1 ORDER BY rp.submoduleId")
                    .setParameter("roleId", roleId)
                    .setParameter("moduleId", moduleId)
                    .list();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return distinctSubModules;
    }
    
    public Roles getRoleEntityByRoleName(String roleName) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from Roles where roleName=?1").setParameter(1, roleName)
                .list();

        trans.commit();
        return (Roles) list.get(0);
    }
    
    public List<Roles> getRolesList() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<Roles> list = (List<Roles>) session.createQuery("from Roles").list();

        trans.commit();
        return list;
    }

    public Map<String, Integer> getCountOfRolesUsedAndNotUsed() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        // Query to get the count of distinct roles used in the UserDetails table
        Long usedRoles = (Long) session.createQuery("select count(distinct role) from UserDetails").uniqueResult();

        // Query to get the total count of roles in the Roles table
        Long totalRoles = (Long) session.createQuery("select count(*) from Roles").uniqueResult();

        // Calculating roles not used
        int notUsedRoles = totalRoles.intValue() - usedRoles.intValue();

        // Creating result map
        Map<String, Integer> result = new HashMap<>();
        result.put("usedRoles", usedRoles.intValue());
        result.put("notUsedRoles", notUsedRoles);

        trans.commit();
        return result;
    }
    
}





