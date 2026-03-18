/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.persist.coretix.modal.usermanagement.dao.impl;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.persist.coretix.modal.usermanagement.dao.IUserAdministrationDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


/**
 *
 * @author Pragadeesh
 */
@Named
public class UserAdministrationDAO  implements IUserAdministrationDAO{
    
private final Logger logger = Logger.getLogger(getClass());
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void addUserDetail(UserDetails entity) {
        logger.debug("inside UserAdministrationDAO addUserDetail");

        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();
        session.save(entity);
        trans.commit();
    }

    public void deleteUserDetail(UserDetails entity) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();
        session.delete(entity);
        trans.commit();
    }

    public void updateUserDetail(UserDetails entity) {
        logger.debug("inside CountryDAO updateCountry");
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();
        session.update(entity);
        trans.commit();
    }

    public UserDetails getUserDetail(int id) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();
logger.debug("User Id inside getUserDetail(int id):"+id);
        List<?> list = session
                .createQuery("from UserDetails where id=?").setParameter(0, id)
                .list();

        trans.commit();
        return (UserDetails) list.get(0);
    }

    public UserDetails getUserDetailEntityByUserName(String userName) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from UserDetails where user_name=?").setParameter(0, userName)
                .list();

        trans.commit();
        return (UserDetails) list.get(0);
    }
    
    public List<UserDetails> getUserDetailsList() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        @SuppressWarnings("unchecked")
        List<UserDetails> list = (List<UserDetails>) session.createQuery("from UserDetails").list();

        trans.commit();
        return list;
    }


    public void updateUserPassword(int userId, String newPassword) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        String hql = "update UserDetails set password = :newPassword where user_id = :userId";
        int updatedEntities = session.createQuery(hql)
                .setParameter("newPassword", newPassword)
                .setParameter("userId", userId)
                .executeUpdate();

        trans.commit();
    }

    public boolean isUserValid(String username, String password) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        List<?> list = session
                .createQuery("from UserDetails where user_name = :username and password = :password")
                .setParameter("username", username)
                .setParameter("password", password)
                .list();

        trans.commit();
        return !list.isEmpty();
    }

    public int getCountOfUsersLoggedOut() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        Long count = (Long) session.createQuery("select count(*) from UserDetails where status_id = 6").uniqueResult();

        trans.commit();
        return count.intValue();
    }

    public int getCountOfUsersLoggedIn() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        Long count = (Long) session.createQuery("select count(*) from UserDetails where status_id = 1").uniqueResult();

        trans.commit();
        return count.intValue();
    }

    public int getCountOfUsersNeverLoggedIn() {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        Long count = (Long) session.createQuery("select count(*) from UserDetails where status_id = 3").uniqueResult();

        trans.commit();
        return count.intValue();
    }

    public void updateUserStatus(int userId, int newStatus) {
        Session session = getSessionFactory().getCurrentSession();
        Transaction trans = session.beginTransaction();

        String hql = "update UserDetails set status_id = :newStatus, last_successful_login = current_timestamp(), updated_at = current_timestamp() where user_id = :userId";
        int updatedEntities = session.createQuery(hql)
                .setParameter("newStatus", newStatus)
                .setParameter("userId", userId)
                .executeUpdate();

        trans.commit();
    }

}

