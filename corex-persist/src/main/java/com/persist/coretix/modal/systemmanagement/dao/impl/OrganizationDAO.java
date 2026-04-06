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
package com.persist.coretix.modal.systemmanagement.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.systemmanagement.Organizations;
import com.persist.coretix.modal.systemmanagement.dao.IOrganizationDAO;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

import com.persist.coretix.modal.usermanagement.dao.IUserActivityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;

import java.sql.Timestamp;

@Named
public class OrganizationDAO implements IOrganizationDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(OrganizationDAO.class);
     
    @Inject
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public GeneralConstants addOrganization(Organizations organization) {
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Organizations o where o.organizationName = :name")
                    .setParameter("name", organization.getOrganizationName())
                    .uniqueResult();
            logger.debug("count : " + count);

            if (count != null && count > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (organization.getCreatedAt() == null) {
                organization.setCreatedAt(now);
            }
            organization.setUpdatedAt(now);

            session.save(organization);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public GeneralConstants updateOrganization(Organizations organization) {
        logger.debug("inside dao updateOrganization !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long countById = (Long) session.createQuery(
                            "select count(o) from Organizations o where o.id = :id")
                    .setParameter("id", organization.getId())
                    .uniqueResult();
            logger.debug("update count by ID: " + countById);
            if (countById == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long countByName = (Long) session.createQuery(
                            "select count(o) from Organizations o where o.organizationName = :name and o.id != :id")
                    .setParameter("name", organization.getOrganizationName())
                    .setParameter("id", organization.getId())
                    .uniqueResult();
            logger.debug("update count by name: " + countByName);
            if (countByName > 0) {
                return GeneralConstants.ENTRY_ALREADY_EXISTS;
            }

            Organizations existingOrganization = session.get(Organizations.class, organization.getId());
            if (existingOrganization == null) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            existingOrganization.setOrganizationName(organization.getOrganizationName());
            existingOrganization.setCountry(organization.getCountry());
            existingOrganization.setState(organization.getState());
            existingOrganization.setAddressLine1(organization.getAddressLine1());
            existingOrganization.setAddressLine2(organization.getAddressLine2());
            existingOrganization.setCity(organization.getCity());
            existingOrganization.setPostalCode(organization.getPostalCode());
            existingOrganization.setPhoneNumber(organization.getPhoneNumber());
            existingOrganization.setEmail(organization.getEmail());
            existingOrganization.setWebsite(organization.getWebsite());
            if (organization.getImage() != null && organization.getImage().length > 0) {
                existingOrganization.setImage(organization.getImage());
            }
            if (existingOrganization.getCreatedAt() == null) {
                existingOrganization.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            }
            existingOrganization.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            logger.debug("crossed and before update");
            session.update(existingOrganization);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public GeneralConstants deleteOrganization(Organizations organization) {
        logger.debug("inside dao deleteOrganization !!");
        Session session = null;
        Transaction trans = null;
        try {
            session = getSessionFactory().openSession();
            trans = session.beginTransaction();

            Long count = (Long) session.createQuery(
                            "select count(o) from Organizations o where o.id = :id")
                    .setParameter("id", organization.getId())
                    .uniqueResult();
            logger.debug("delete count : " + count);

            if (count == 0) {
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            session.delete(organization);
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (ConstraintViolationException e) {
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.ENTRY_IN_USE;
        } catch (Exception e) {
            logger.debug("Exception : " + e);
            if (trans != null) {
                trans.rollback();
            }
            return GeneralConstants.FAILED;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Organizations getOrganization(int id) {
        Session session = getSessionFactory().getCurrentSession();
        List<?> list = session
                .createQuery("from Organizations where id=?1")
                .setParameter(1, id)
                .list();

        return list.isEmpty() ? null : (Organizations) list.get(0);
    }

    public Organizations getOrganizationsEntityByOrganizationName(String organizationName) {
        Session session = getSessionFactory().getCurrentSession();
        List<?> list = session
                .createQuery("from Organizations where organization_name=?1")
                .setParameter(1, organizationName)
                .list();

        return list.isEmpty() ? null : (Organizations) list.get(0);
    }

    public List<Organizations> getOrganizationsList() {
        Session session = getSessionFactory().getCurrentSession();
        @SuppressWarnings("unchecked")
        List<Organizations> list = (List<Organizations>) session.createQuery("from Organizations").list();

        return list;
    }
}






