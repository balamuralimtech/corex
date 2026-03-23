/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.persist.coretix.modal.usermanagement.dao.impl;

import com.persist.coretix.modal.usermanagement.ModulePrivileges;
import com.persist.coretix.modal.usermanagement.dao.IModulePrivilegeDAO;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 *
 * @author balamurali
 */
@Named
public class ModulePrivilegeDAO implements IModulePrivilegeDAO {

    private static final Logger logger = LoggerFactory.getLogger(ModulePrivilegeDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void addModulePrivileges(List<ModulePrivileges> modulePrivileges) {
        logger.debug("inside dao save organization !!");
        for (ModulePrivileges modulePrivilege : modulePrivileges) {
            Session session = getSessionFactory().getCurrentSession();
            Transaction trans = session.beginTransaction();
            session.save(modulePrivilege);
            trans.commit();
        }

    }

    public void updateModulePrivileges(List<ModulePrivileges> modulePrivileges) {
        logger.debug("inside dao deleteModulePrivileges !!");
        for (ModulePrivileges modulePrivilege : modulePrivileges) {
            Session session = getSessionFactory().getCurrentSession();
            Transaction trans = session.beginTransaction();
            session.update(modulePrivilege);
            trans.commit();
        }
    }

    public void deleteModulePrivileges(List<ModulePrivileges> modulePrivileges) {
        logger.debug("inside dao deleteModulePrivileges !!");
        for (ModulePrivileges modulePrivilege : modulePrivileges) {
            Session session = getSessionFactory().getCurrentSession();
            Transaction trans = session.beginTransaction();
            session.delete(modulePrivilege);
            trans.commit();
        }
    }

    public void deleteAllUserActivities() {
        logger.debug("Inside DAO deleteAllUserActivities!!");

        Session session = getSessionFactory().getCurrentSession();
        Transaction transaction = null;

        try {
            // Start a transaction
            transaction = session.beginTransaction();

            // HQL to delete all records from ModulePrivileges
            String hql = "delete from ModulePrivileges";
            session.createQuery(hql).executeUpdate();

            // Native SQL query to reset auto-increment (MySQL specific)
            session.createSQLQuery("ALTER TABLE ModulePrivileges AUTO_INCREMENT = 1").executeUpdate();

            // Commit the transaction
            transaction.commit();

            logger.debug("All ModulePrivileges deleted and auto-increment reset!");
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();  // Rollback in case of an error
            }
            logger.error("Error in deleteAllUserActivities: " + e);
            throw e;  // Re-throw the exception after logging
        }
    }

    public ModulePrivileges getModulePrivilege(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from ModulePrivileges where id=?1").setParameter(1, id)
                .list();

        trans.commit();
        return (ModulePrivileges) list.get(0);
    }

    public List<ModulePrivileges> getModulePrivilegesList() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<ModulePrivileges> list = (List<ModulePrivileges>) session.createQuery("from ModulePrivileges").list();

        trans.commit();
        return list;
    }

}



